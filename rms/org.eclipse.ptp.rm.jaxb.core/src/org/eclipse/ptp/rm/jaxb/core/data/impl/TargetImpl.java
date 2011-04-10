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
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Match;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Target;
import org.eclipse.ptp.rm.jaxb.core.data.Test;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

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

	public TargetImpl(String uuid, Target target) {
		this.uuid = uuid;
		ref = target.getRef();
		type = target.getType();
		matchAll = target.isMatchAll();
		matches = new ArrayList<MatchImpl>();
		List<Match> mdata = target.getMatch();
		for (Match m : mdata) {
			matches.add(new MatchImpl(uuid, m, this));
		}
		tests = new ArrayList<TestImpl>();
		List<Test> tdata = target.getTest();
		for (Test t : tdata) {
			tests.add(new TestImpl(uuid, t));
		}
		targets = new ArrayList<Object>();
		selected = false;
	}

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
					Property p = new Property();
					target = p;
					targets.add(target);
				} else if (ATTRIBUTE.equals(type)) {
					Attribute ja = new Attribute();
					target = ja;
					targets.add(target);
				} else {
					throw CoreExceptionUtils.newException(Messages.StreamParserMissingTargetType + ref, null);
				}
			}
		}

		return target;
	}

	public boolean isSelected() {
		return selected;
	}

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
					dmap.put(((Property) t).getName(), t);
				} else if (ATTRIBUTE.equals(type)) {
					dmap.put(((Attribute) t).getName(), t);
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

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	private void merge(Attribute previous, Attribute current) throws Throwable {
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

	private void merge(Property previous, Property current) throws Throwable {
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

	private void mergeAttributes(List<Object> targets) throws Throwable {
		Map<String, Attribute> hash = new HashMap<String, Attribute>();
		for (Iterator<Object> i = targets.iterator(); i.hasNext();) {
			Attribute current = (Attribute) i.next();
			String name = current.getName();
			if (current.getName() == null) {
				// may be an artifact of end-of-stream; just throw it out
				i.remove();
				continue;
			}
			Attribute previous = hash.get(name);
			if (previous != null) {
				merge(previous, current);
				i.remove();
			} else {
				hash.put(name, current);
			}
		}
	}

	private void mergeProperties(List<Object> targets) throws Throwable {
		Map<String, Property> hash = new HashMap<String, Property>();
		for (Iterator<Object> i = targets.iterator(); i.hasNext();) {
			Property current = (Property) i.next();
			String name = current.getName();
			if (current.getName() == null) {
				// may be an artifact of end-of-stream; just throw it out
				i.remove();
				continue;
			}
			Property previous = hash.get(name);
			if (previous != null) {
				merge(previous, current);
				i.remove();
			} else {
				hash.put(name, current);
			}
		}
	}
}
