/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.data.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.rm.jaxb.core.IAssign;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IMatchable;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.MatchType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.TargetType;
import org.eclipse.ptp.rm.jaxb.core.data.TestType;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Wrapper implementation. A target contains any number of matches, with their
 * associated actions, along with tests for conditional actions based on values
 * of the target fields. <br>
 * <br>
 * There are two modes to matching. The default is to treat the matches as
 * logically OR'd (like a SAT; <code>matchAll</code> = false). When the latter
 * is set to true, the matches are taken as logically ANDed.<br>
 * <br>
 * The target can be a reference to a pre-existent Property or Attribute in the
 * resource manager environment, or can be constructed when the match occurs.
 * Dynamically constructed targets are added to a list during the tokenization,
 * and then upon termination are merged according to property or attribute name,
 * which is treated as a unique identifier. <br>
 * <br>
 * Tests are applied at the end of the tokenization. <br>
 * 
 * @author arossi
 * 
 */
public class TargetImpl implements IMatchable, IJAXBNonNLSConstants {

	private final String uuid;
	private final String ref;
	private final String type;
	private final List<MatchImpl> matches;
	private final List<TestImpl> tests;
	private final List<Object> targets;
	private Object refTarget;
	private final boolean matchAll;
	private boolean selected;

	/**
	 * Wraps the Property or Attribute to be acted upon.
	 * 
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param target
	 *            JAXB data element
	 */
	public TargetImpl(String uuid, TargetType target) {
		this.uuid = uuid;
		ref = target.getRef();
		type = target.getType();
		matchAll = target.isMatchAll();
		matches = new ArrayList<MatchImpl>();
		List<MatchType> mdata = target.getMatch();
		for (MatchType m : mdata) {
			matches.add(new MatchImpl(uuid, m, this));
		}
		tests = new ArrayList<TestImpl>();
		List<TestType> tdata = target.getTest();
		for (TestType t : tdata) {
			tests.add(new TestImpl(uuid, t));
		}
		targets = new ArrayList<Object>();
		selected = false;
	}

	/**
	 * Applies the matches in order. If <code>matchAll</code> is in effect,
	 * already matched expressions are skipped until they are reset; the first
	 * match causes a return of this method.<br>
	 * <br>
	 * Upon match, the head of the segment up to the last character of the match
	 * is deleted.
	 * 
	 * @param segment
	 *            the current part of the stream to match
	 * @return whether a successful match was found on this target
	 * @throws CoreException
	 */
	public synchronized boolean doMatch(StringBuffer segment) throws Throwable {
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
				segment.delete(0, tail);
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
	 * This method is called by the Match on its target parent. If the target is
	 * a reference to an existing object in the environment, this is then
	 * returned; else, the index counter for the assign task is retrieved,
	 * indicating where in the list of constructed targets it last was (the
	 * assumption is that an assign action is applied once to any given Property
	 * or Attribute), and this object is returned if it exists; in the case that
	 * the index is equal to or greater than the size of the list, a new target
	 * object is constructed and added to the list.
	 * 
	 * @param assign
	 *            action to be applied to target
	 * @return the appropriate target for this action
	 * @throws CoreException
	 */
	public Object getTarget(IAssign assign) throws CoreException {
		if (refTarget != null) {
			return refTarget;
		}
		Object target = null;
		if (ref != null) {
			RMVariableMap vmap = RMVariableMap.getActiveInstance();
			String name = vmap.getString(uuid, ref);
			target = vmap.get(name);
			if (target == null) {
				throw CoreExceptionUtils.newException(Messages.StreamParserNoSuchVariableError + name, null);
			}
			refTarget = target;
		} else {
			int i = assign.getIndex();
			if (i < targets.size()) {
				target = targets.get(i);
			}
			if (target == null) {
				if (PROPERTY.equals(type)) {
					PropertyType p = new PropertyType();
					target = p;
					targets.add(target);
				} else if (ATTRIBUTE.equals(type)) {
					AttributeType ja = new AttributeType();
					target = ja;
					targets.add(target);
				} else {
					throw CoreExceptionUtils.newException(Messages.StreamParserMissingTargetType + ref, null);
				}
			}
		}

		return target;
	}

	/**
	 * @return whether this target has been selected for promotion to the head
	 *         of the list
	 */
	public boolean isSelected() {
		return selected;
	}

	/**
	 * Called upon completion of tokenization.<br>
	 * <br>
	 * First merges any constructed targets, then applies the tests to all
	 * targets.
	 * 
	 * @throws Throwable
	 */
	public synchronized void postProcess() throws Throwable {
		if (refTarget == null) {
			if (PROPERTY.equals(type)) {
				mergeProperties(targets);
			} else if (ATTRIBUTE.equals(type)) {
				mergeAttributes(targets);
			}
			Map<String, Object> dmap = RMVariableMap.getActiveInstance().getDiscovered();
			for (Object t : targets) {
				for (TestImpl test : tests) {
					test.setTarget(t);
					test.doTest();
				}
				if (PROPERTY.equals(type)) {
					dmap.put(((PropertyType) t).getName(), t);
				} else if (ATTRIBUTE.equals(type)) {
					dmap.put(((AttributeType) t).getName(), t);
				}
			}
			targets.clear();
		} else {
			for (TestImpl test : tests) {
				test.setTarget(refTarget);
				test.doTest();
			}
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
	 * Takes two attributes with the same name and merges the rest of their
	 * fields, such that non-<code>null</code> values replace <code>null</code>.
	 * An attempt to merge two non-<code>null</code> fields implies that there
	 * was an error in the tokenization logic, and an exception is thrown.
	 * 
	 * @param previous
	 *            Attribute
	 * @param current
	 *            Attribute
	 * @throws Throwable
	 */
	private void merge(AttributeType previous, AttributeType current) throws Throwable {
		Object v0 = previous.getValue();
		Object v1 = current.getValue();
		if (v0 == null) {
			previous.setValue(v1);
		} else if (v1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + v0 + CM + SP + v1);
		}

		String s0 = previous.getDefault();
		String s1 = current.getDefault();
		if (s0 == null) {
			previous.setDefault(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		s0 = previous.getType();
		s1 = current.getType();
		if (s0 == null) {
			previous.setType(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		s0 = previous.getStatus();
		s1 = current.getStatus();
		if (s0 == null) {
			previous.setStatus(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		boolean b0 = previous.isReadOnly();
		boolean b1 = current.isReadOnly();
		if (!b0) {
			previous.setReadOnly(b1);
		}

		b0 = previous.isVisible();
		b1 = current.isVisible();
		if (!b0) {
			previous.setVisible(b1);
		}

		Integer i0 = previous.getMax();
		Integer i1 = current.getMax();
		if (i0 == null) {
			previous.setMax(i1);
		} else if (i1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + i0 + CM + SP + i1);
		}

		i0 = previous.getMin();
		i1 = current.getMin();
		if (i0 == null) {
			previous.setMin(i1);
		} else if (i1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + i0 + CM + SP + i1);
		}

		s0 = previous.getDescription();
		s1 = current.getDescription();
		if (s0 == null) {
			previous.setDescription(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		s0 = previous.getChoice();
		s1 = current.getChoice();
		if (s0 == null) {
			previous.setChoice(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		s0 = previous.getTooltip();
		s1 = current.getTooltip();
		if (s0 == null) {
			previous.setTooltip(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}
	}

	/**
	 * Takes two properties with the same name and merges the rest of their
	 * fields, such that non-<code>null</code> values replace <code>null</code>.
	 * An attempt to merge two non-<code>null</code> fields implies that there
	 * was an error in the tokenization logic, and an exception is thrown.
	 * 
	 * @param previous
	 *            Property
	 * @param current
	 *            Property
	 * @throws Throwable
	 */
	private void merge(PropertyType previous, PropertyType current) throws Throwable {
		Object v0 = previous.getValue();
		Object v1 = current.getValue();
		if (v0 == null) {
			previous.setValue(v1);
		} else if (v1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + v0 + CM + SP + v1);
		}

		String s0 = previous.getDefault();
		String s1 = current.getDefault();
		if (s0 == null) {
			previous.setDefault(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		s0 = previous.getType();
		s1 = current.getType();
		if (s0 == null) {
			previous.setType(s1);
		} else if (s1 != null) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning + s0 + CM + SP + s1);
		}

		boolean b0 = previous.isReadOnly();
		boolean b1 = current.isReadOnly();
		if (!b0) {
			previous.setReadOnly(b1);
		}

		b0 = previous.isVisible();
		b1 = current.isVisible();
		if (!b0) {
			previous.setVisible(b1);
		}
	}

	/**
	 * Attributes are hashed against their name; attributes with the same name
	 * are merged into a single object. Nameless attributes may occur from an
	 * empty line at the end of the stream, and are simply discarded.
	 * 
	 * @param targets
	 *            list of targets constructed during tokenization
	 * @throws Throwable
	 */
	private void mergeAttributes(List<Object> targets) throws Throwable {
		Map<String, AttributeType> hash = new HashMap<String, AttributeType>();
		for (Iterator<Object> i = targets.iterator(); i.hasNext();) {
			AttributeType current = (AttributeType) i.next();
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
	 * Properties are hashed against their name; properties with the same name
	 * are merged into a single object. Nameless properties may occur from an
	 * empty line at the end of the stream, and are simply discarded.
	 * 
	 * @param targets
	 *            list of targets constructed during tokenization
	 * @throws Throwable
	 */
	private void mergeProperties(List<Object> targets) throws Throwable {
		Map<String, PropertyType> hash = new HashMap<String, PropertyType>();
		for (Iterator<Object> i = targets.iterator(); i.hasNext();) {
			PropertyType current = (PropertyType) i.next();
			String name = current.getName();
			if (current.getName() == null) {
				/*
				 * may be an artifact of end-of-stream; just throw it out
				 */
				i.remove();
				continue;
			}
			PropertyType previous = hash.get(name);
			if (previous != null) {
				merge(previous, current);
				i.remove();
			} else {
				hash.put(name, current);
			}
		}
	}
}
