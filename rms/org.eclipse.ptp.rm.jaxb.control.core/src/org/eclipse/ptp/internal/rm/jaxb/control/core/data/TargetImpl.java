/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.core.data;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign;
import org.eclipse.ptp.internal.rm.jaxb.control.core.IMatchable;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.core.utils.DebuggingLogger;
import org.eclipse.ptp.internal.rm.jaxb.control.core.variables.RMVariableMap;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;
import org.eclipse.ptp.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AddType;
import org.eclipse.ptp.rm.jaxb.core.data.AppendType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.MatchType;
import org.eclipse.ptp.rm.jaxb.core.data.PutType;
import org.eclipse.ptp.rm.jaxb.core.data.SetType;
import org.eclipse.ptp.rm.jaxb.core.data.TargetType;
import org.eclipse.ptp.rm.jaxb.core.data.TestType;
import org.eclipse.ptp.rm.jaxb.core.data.ThrowType;

/**
 * Wrapper implementation. A target contains any number of matches, with their associated actions, along with tests for conditional
 * actions based on values of the target fields. <br>
 * <br>
 * There are two modes to matching. The default is to treat the matches as logically OR'd (like a SAT; <code>matchAll</code> =
 * false). When the latter is set to true, the matches are taken as logically ANDed.<br>
 * <br>
 * The target can be a reference to a pre-existent attribute in the resource manager environment, or can be constructed when the
 * match occurs. Dynamically constructed targets are added to a list during the tokenization, and then upon termination are merged
 * according to attribute name, which is treated as a unique identifier. <br>
 * <br>
 * Tests are applied at the end of the tokenization. <br>
 * 
 * @author arossi
 * 
 */
public class TargetImpl implements IMatchable {

	private final IVariableMap rmVarMap;
	private final String uuid;
	private final String ref;
	private final List<MatchImpl> matches;
	private final List<TestImpl> tests;
	private final List<AttributeType> targets;
	private final boolean matchAll;
	private final boolean allowOverwrites;
	private IAssign defaultAction;
	private AttributeType refTarget;
	private boolean selected;

	/**
	 * Wraps the Property or Attribute to be acted upon.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can be <code>null</code>).
	 * @param target
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public TargetImpl(String uuid, TargetType target, IVariableMap rmVarMap) {
		this.rmVarMap = rmVarMap;
		this.uuid = uuid;
		ref = target.getRef();
		matchAll = target.isMatchAll();
		allowOverwrites = target.isAllowOverwrites();
		matches = new ArrayList<MatchImpl>();
		List<MatchType> mdata = target.getMatch();
		for (MatchType m : mdata) {
			matches.add(new MatchImpl(uuid, m, this, rmVarMap));
		}
		tests = new ArrayList<TestImpl>();
		List<TestType> tdata = target.getTest();
		for (TestType t : tdata) {
			tests.add(new TestImpl(uuid, t, rmVarMap));
		}

		TargetType.Else defAction = target.getElse();
		if (defAction != null) {
			AddType add = defAction.getAdd();
			AppendType append = defAction.getAppend();
			PutType put = defAction.getPut();
			SetType set = defAction.getSet();
			ThrowType toThrow = defAction.getThrow();
			if (add != null) {
				defaultAction = new AddImpl(uuid, add, rmVarMap);
			} else if (append != null) {
				defaultAction = new AppendImpl(uuid, append, rmVarMap);
			} else if (put != null) {
				defaultAction = new PutImpl(uuid, put, rmVarMap);
			} else if (set != null) {
				defaultAction = new SetImpl(uuid, set, rmVarMap);
			} else if (toThrow != null) {
				defaultAction = new ThrowImpl(uuid, toThrow, rmVarMap);
			}
		}
		targets = new ArrayList<AttributeType>();
		selected = false;
	}

	/**
	 * Applies the matches in order. If <code>matchAll</code> is in effect, already matched expressions are skipped until they are
	 * reset; the first match causes a return of this method.<br>
	 * <br>
	 * Upon match, the head of the segment up to the last character of the match is deleted.
	 * 
	 * @param segment
	 *            the current part of the stream to match
	 * @return whether a successful match was found on this target
	 * @throws CoreException
	 */
	public synchronized boolean doMatch(StringBuffer segment) throws StreamParserException {
		int matched = 0;
		boolean match = false;

		for (MatchImpl m : matches) {
			if (matchAll && m.getMatched()) {
				matched++;
				continue;
			}
			int tail = m.doMatch(segment.toString());
			match = m.getMatched();
			if (match) {
				if (tail >= 0) {
					segment.delete(0, tail);
				}
				matched++;
				selected = m.getMoveToTop();
				break;
			}
		}
		if (!matchAll || matched == matches.size()) {
			for (MatchImpl m : matches) {
				m.reset();
			}
		}

		return match;
	}

	/**
	 * Get the target object for the given assign task.<br>
	 * <br>
	 * This method is called by the Match on its target parent. If the target is a reference to an existing object in the
	 * environment, this is then returned; else, the index counter for the assign task is retrieved, indicating where in the list of
	 * constructed targets it last was (the assumption is that an assign action is applied once to any given attribute), and this
	 * object is returned if it exists; in the case that the index is equal to or greater than the size of the list, a new target
	 * object is constructed and added to the list.
	 * 
	 * @param assign
	 *            action to be applied to target
	 * @return the appropriate target for this action
	 * @throws CoreException
	 */
	public AttributeType getTarget(IAssign assign) throws StreamParserException {
		if (refTarget != null) {
			return refTarget;
		}
		AttributeType target = null;
		if (ref != null) {
			String name = rmVarMap.getString(uuid, ref);
			target = rmVarMap.get(name);
			if (target == null) {
				throw new StreamParserException(Messages.StreamParserNoSuchVariableError + name);
			}
			refTarget = target;
		} else {
			int i = assign.getIndex();
			if (i < targets.size()) {
				target = targets.get(i);
			}
			if (target == null) {
				target = new AttributeType();
				targets.add(target);
			}
		}

		return target;
	}

	/**
	 * @return whether this target has been selected for promotion to the head of the list
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Called upon completion of tokenization.<br>
	 * <br>
	 * First merges any constructed targets, then applies the tests to all targets.
	 * 
	 * @throws StreamParserException
	 */
	public synchronized void postProcess() throws StreamParserException {
		if (refTarget == null) {
			if (targets.isEmpty() && defaultAction != null) {
				/*
				 * No match succeeded but there is a default action. Perform it.
				 */
				defaultAction.setTarget(getTarget(defaultAction));
				defaultAction.assign(null);
			} else {
				DebuggingLogger.getLogger().logPropertyInfo(Messages.TargetImpl_2 + targets.size() + Messages.TargetImpl_3);
				mergeAttributes(targets);
				if (rmVarMap instanceof RMVariableMap) {
					Map<String, AttributeType> dmap = ((RMVariableMap) rmVarMap).getDiscovered();
					for (AttributeType t : targets) {
						runTests(t);
						AttributeType a = t;
						DebuggingLogger.getLogger().logPropertyInfo(
								Messages.TargetImpl_6 + a.getName() + JAXBCoreConstants.CM + JAXBCoreConstants.SP + a.getValue());
						dmap.put(a.getName(), a);
					}
				}
				targets.clear();
			}
		} else {
			runTests(refTarget);
			refTarget = null;
		}
	}

	/**
	 * @param selected
	 *            whether this target should be promoted to the head of the list
	 */
	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	/**
	 * Takes two attributes with the same name and merges the rest of their fields, such that non-<code>null</code> values replace
	 * <code>null</code>. An attempt to merge two non-<code>null</code> fields implies that there was an error in the tokenization
	 * logic, and an exception is thrown.
	 * 
	 * @param previous
	 *            Attribute
	 * @param current
	 *            Attribute
	 * @throws StreamParserException
	 */
	private void merge(AttributeType previous, AttributeType current) throws StreamParserException {
		previous.setValue(mergeObject(previous.getValue(), current.getValue()));
		previous.setDefault(mergeString(previous.getDefault(), current.getDefault()));
		previous.setType(mergeString(previous.getType(), current.getType()));
		previous.setReadOnly(mergeBoolean(previous.isReadOnly(), current.isReadOnly()));
		previous.setVisible(mergeBoolean(previous.isVisible(), current.isVisible()));
		previous.setStatus(mergeString(previous.getStatus(), current.getStatus()));
		previous.setMax(mergeInteger(previous.getMax(), current.getMax()));
		previous.setMin(mergeInteger(previous.getMin(), current.getMin()));
		previous.setDescription(mergeString(previous.getDescription(), current.getDescription()));
		previous.setTooltip(mergeString(previous.getTooltip(), current.getTooltip()));
		previous.setChoice(mergeString(previous.getChoice(), current.getChoice()));
	}

	/**
	 * Attributes are hashed against their name; attributes with the same name are merged into a single object. Nameless attributes
	 * may occur from an empty line at the end of the stream, and are simply discarded.
	 * 
	 * @param targets
	 *            list of targets constructed during tokenization
	 * @throws StreamParserException
	 */
	private void mergeAttributes(List<AttributeType> targets) throws StreamParserException {
		Map<String, AttributeType> hash = new HashMap<String, AttributeType>();
		for (Iterator<AttributeType> i = targets.iterator(); i.hasNext();) {
			AttributeType current = i.next();
			String name = current.getName();
			if (current.getName() == null) {
				/*
				 * may be an artifact of end-of-stream; just throw it out
				 */
				i.remove();
				continue;
			}
			AttributeType previous = hash.get(name);
			if (previous != null) {
				merge(previous, current);
				i.remove();
			} else {
				hash.put(name, current);
			}
		}
	}

	/**
	 * Checks if overwrites are allowed.
	 * 
	 * @param b0
	 * @param b1
	 * @return merged value
	 */
	private Boolean mergeBoolean(Boolean b0, Boolean b1) {
		if (allowOverwrites) {
			return b1;
		}
		if (!b0) {
			return b1;
		}
		return b0;
	}

	/**
	 * Checks if overwrites are allowed.
	 * 
	 * @param i0
	 * @param i1
	 * @return merged value
	 * @throws StreamParserException
	 *             if duplicate and overwrites not allowed.
	 */
	private Integer mergeInteger(Integer i0, Integer i1) throws StreamParserException {
		if (i0 == null) {
			return i1;
		} else if (i1 != null) {
			if (allowOverwrites) {
				return i1;
			}
			throw new StreamParserException(Messages.StreamParserInconsistentPropertyWarning + i0 + JAXBControlConstants.CM
					+ JAXBControlConstants.SP + i1);
		}
		return i0;
	}

	/**
	 * Checks if overwrites are allowed. If object is <code>Collection</code> or <code>Map</code>, the merge will proceed anyway.
	 * 
	 * @param v0
	 * @param v1
	 * @return merged value
	 * @throws StreamParserException
	 *             if duplicate and overwrites not allowed.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private Object mergeObject(Object v0, Object v1) throws StreamParserException {
		if (v0 == null) {
			return v1;
		} else if (v1 != null) {
			if (v0 instanceof Collection && v1 instanceof Collection) {
				((Collection) v0).addAll((Collection) v1);
			} else if (v0 instanceof Map && v1 instanceof Map) {
				((Map) v0).putAll((Map) v1);
			} else if (allowOverwrites) {
				return v1;
			}
			throw new StreamParserException(Messages.StreamParserInconsistentPropertyWarning + v0 + JAXBControlConstants.CM
					+ JAXBControlConstants.SP + v1);
		}
		return v0;
	}

	/**
	 * Checks if overwrites are allowed.
	 * 
	 * @param s0
	 * @param s1
	 * @return merged value
	 * @throws StreamParserException
	 *             if duplicate and overwrites not allowed.
	 */
	private String mergeString(String s0, String s1) throws StreamParserException {
		if (s0 == null) {
			return s1;
		} else if (s1 != null) {
			if (allowOverwrites) {
				return s1;
			}
			throw new StreamParserException(Messages.StreamParserInconsistentPropertyWarning + s0 + JAXBControlConstants.CM
					+ JAXBControlConstants.SP + s1);
		}
		return s0;
	}

	/**
	 * Runs all the tests on the given target. If none succeed, and the default action is defined, the latter is applied.
	 * 
	 * @param target
	 * @throws StreamParserException
	 */
	private void runTests(AttributeType target) throws StreamParserException {
		boolean any = false;
		boolean testSuccess = false;
		for (TestImpl test : tests) {
			test.setTarget(target);
			testSuccess = test.doTest();
			any = any || testSuccess;
		}
		if (!any && defaultAction != null) {
			defaultAction.setTarget(target);
			/*
			 * These will be using only preassigned values, so the tokens[] param is null
			 */
			defaultAction.assign(null);
		}
	}
}
