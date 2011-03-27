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

	public TargetImpl(String uuid, Target target) {
		this.uuid = uuid;
		ref = target.getRef();
		type = target.getType();
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
	}

	/*
	 * Target matches are OR'd.
	 */
	public synchronized boolean doMatch(StringBuffer segment) throws Throwable {
		for (MatchImpl m : matches) {
			int tail = m.doMatch(segment.toString());
			if (tail > 0) {
				segment.delete(0, tail);
				return true;
			}
		}

		return false;
	}

	public Object getTarget(IAssign assign) throws CoreException {
		Object target = null;
		if (ref != null) {
			RMVariableMap vmap = RMVariableMap.getActiveInstance();
			String name = vmap.getString(uuid, ref);
			target = vmap.getVariables().get(name);
			if (target == null) {
				throw CoreExceptionUtils.newException(Messages.StreamParserNoSuchVariableError + name, null);
			}
		} else {
			target = targets.get(assign.getIndex());
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

	public synchronized void postProcess() throws Throwable {
		Map<String, Object> hash = new HashMap<String, Object>();
		String name = null;
		Property lastP = null;
		Attribute lastA = null;

		Iterator<Object> i = targets.iterator();

		if (i.hasNext()) {
			if (PROPERTY.equals(type)) {
				lastP = (Property) i.next();
				name = lastP.getName();
				if (name != null) {
					hash.put(name, lastP);
					lastP = null;
				}
			} else if (ATTRIBUTE.equals(type)) {
				lastA = (Attribute) i.next();
				name = lastA.getName();
				if (name != null) {
					hash.put(name, lastA);
					lastA = null;
				}
			}
		}

		while (i.hasNext()) {
			if (PROPERTY.equals(type)) {
				Property current = (Property) i.next();
				name = current.getName();
				if (name != null) {
					Property prev = (Property) hash.get(name);
					if (prev != null) {
						merge(prev, current);
					} else {
						if (lastP != null) {
							merge(lastP, current);
							hash.put(name, lastP);
							lastP = null;
						} else {
							hash.put(name, current);
						}
					}
				} else {
					if (lastP != null) {
						merge(lastP, current);
					} else {
						lastP = current;
					}
				}
			} else if (ATTRIBUTE.equals(type)) {
				Attribute current = (Attribute) i.next();
				name = current.getName();
				if (name != null) {
					Attribute prev = (Attribute) hash.get(name);
					if (prev != null) {
						merge(prev, current);
					} else {
						if (lastA != null) {
							merge(lastA, current);
							hash.put(name, lastA);
							lastA = null;
						} else {
							hash.put(name, current);
						}
					}
				} else {
					if (lastP != null) {
						merge(lastA, current);
					} else {
						lastA = current;
					}
				}
			}
		}

		if (lastP != null) {
			throw new Throwable(Messages.StreamParserNamelessPropertyWarning);
		}
		if (lastA != null) {
			throw new Throwable(Messages.StreamParserNamelessAttributeWarning);
		}

		Map<String, Object> vmap = RMVariableMap.getActiveInstance().getDiscovered();

		for (Object o : hash.values()) {
			if (PROPERTY.equals(type)) {
				Property p = (Property) o;
				name = p.getName();
				for (TestImpl t : tests) {
					t.setTarget(p);
					t.doTest();
				}
			} else if (ATTRIBUTE.equals(type)) {
				Attribute a = (Attribute) o;
				name = a.getName();
				for (TestImpl t : tests) {
					t.setTarget(a);
					t.doTest();
				}
			}
			vmap.put(name, o);
		}

		hash.clear();
		targets.clear();
	}

	private void merge(Attribute a, Attribute aa) {
		String s = aa.getChoice();
		if (null == a.getName()) {
			a.setName(aa.getName());
		}
		if (null != s) {
			a.setChoice(s);
		}
		s = aa.getDefault();
		if (null != s) {
			a.setDefault(s);
		}
		s = aa.getDescription();
		if (null != s) {
			a.setDescription(s);
		}
		s = aa.getTooltip();
		if (null != s) {
			a.setTooltip(s);
		}
		s = aa.getStatus();
		if (null != s) {
			a.setStatus(s);
		}
		s = aa.getType();
		if (null != s) {
			a.setType(s);
		}

		Object o = aa.getValue();
		if (null != o) {
			a.setValue(o);
		}

		Integer i = aa.getMax();
		if (null != i) {
			a.setMax(i);
		}

		i = aa.getMin();
		if (null != i) {
			a.setMin(i);
		}
	}

	private void merge(Property p, Property pp) {
		if (null != p.getName()) {
			p.setName(pp.getName());
		}
		Object o = pp.getValue();
		if (null != o) {
			p.setValue(o);
		}

		String s = pp.getDefault();
		if (null != s) {
			p.setDefault(s);
		}
	}
}
