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
				m.clear();
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
			target = vmap.getVariables().get(name);
			if (target == null) {
				throw CoreExceptionUtils.newException(Messages.StreamParserNoSuchVariableError + name, null);
			}
			refTarget = target;
		} else {
			int i = assign.getIndex();
			if (i < targets.size()) {
				target = targets.get(assign.getIndex());
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
				partitionProperties(targets);
			} else if (ATTRIBUTE.equals(type)) {
				partitionAttributes(targets);
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

	private void mergeAttributes(List<?>[] fields) throws Throwable {
		if (!checkFields(fields)) {
			throw new Throwable(Messages.StreamParserInconsistentAttributeWarning);
		}
		targets.clear();
		int numAttributes = fields[4].size();
		for (int i = 0; i < numAttributes; i++) {
			Attribute a = new Attribute();
			if (fields[0] != null && fields[0].size() > i) {
				a.setValue(fields[0].get(i));
			}
			if (fields[1] != null && fields[1].size() > i) {
				a.setChoice((String) fields[1].get(i));
			}
			if (fields[2] != null && fields[2].size() > i) {
				a.setDefault((String) fields[2].get(i));
			}
			if (fields[3] != null && fields[3].size() > i) {
				a.setDescription((String) fields[3].get(i));
			}
			if (fields[4] != null && fields[4].size() > i) {
				a.setName((String) fields[4].get(i));
			}
			if (fields[5] != null && fields[5].size() > i) {
				a.setStatus((String) fields[5].get(i));
			}
			if (fields[6] != null && fields[6].size() > i) {
				a.setTooltip((String) fields[6].get(i));
			}
			if (fields[7] != null && fields[7].size() > i) {
				a.setType((String) fields[7].get(i));
			}
			if (fields[8] != null && fields[8].size() > i) {
				a.setMax((Integer) fields[8].get(i));
			}
			if (fields[9] != null && fields[9].size() > i) {
				a.setMin((Integer) fields[9].get(i));
			}
			if (fields[10] != null && fields[10].size() > i) {
				a.setReadOnly((Boolean) fields[10].get(i));
			}
			if (fields[11] != null && fields[11].size() > i) {
				a.setVisible((Boolean) fields[11].get(i));
			}
			String name = a.getName();
			if (name != null && !ZEROSTR.equals(name)) {
				targets.add(a);
			}
		}
	}

	private void mergeProperties(List<?>[] fields) throws Throwable {
		if (!checkFields(fields)) {
			throw new Throwable(Messages.StreamParserInconsistentPropertyWarning);
		}
		targets.clear();
		int numProperties = fields[1].size();

		for (int i = 0; i < numProperties; i++) {
			Property p = new Property();
			if (fields[0] != null && fields[0].size() > i) {
				p.setValue(fields[0].get(i));
			}
			if (fields[1] != null && fields[1].size() > i) {
				p.setName((String) fields[1].get(i));
			}
			if (fields[2] != null && fields[2].size() > i) {
				p.setDefault((String) fields[2].get(i));
			}
			String name = p.getName();
			if (name != null && !ZEROSTR.equals(name)) {
				targets.add(p);
			}
		}
	}

	private void partitionAttributes(List<Object> targets) throws Throwable {
		List<?>[] fields = new ArrayList<?>[12];
		initializeFields(fields);

		for (Object o : targets) {
			Attribute a = (Attribute) o;
			Object[] values = new Object[] { a.getValue(), a.getChoice(), a.getDefault(), a.getDescription(), a.getName(),
					a.getStatus(), a.getTooltip(), a.getType(), a.getMax(), a.getMin(), a.isReadOnly(), a.isVisible() };
			addToFields(fields, values);
		}

		mergeAttributes(fields);
	}

	private void partitionProperties(List<Object> targets) throws Throwable {
		List<?>[] fields = new ArrayList<?>[3];
		initializeFields(fields);

		for (Object o : targets) {
			Property p = (Property) o;
			Object[] values = new Object[] { p.getValue(), p.getName(), p.getDefault() };
			addToFields(fields, values);
		}

		mergeProperties(fields);
	}

	@SuppressWarnings("unchecked")
	private static void addToFields(List<?>[] fields, Object[] values) {
		for (int i = 0; i < values.length; i++) {
			switch (i) {
			case 0:
				List<Object> l2 = (List<Object>) fields[i];
				l2.add(values[i]);
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				List<String> l1 = (List<String>) fields[i];
				l1.add((String) values[i]);
				break;
			case 8:
			case 9:
				List<Integer> l3 = (List<Integer>) fields[i];
				l3.add((Integer) values[i]);
				break;
			case 10:
			case 11:
				List<Boolean> l4 = (List<Boolean>) fields[i];
				l4.add((Boolean) values[i]);
				break;
			}
		}
	}

	private static boolean checkFields(List<?>[] fields) {
		int i = 0;
		int sz = 0;
		for (; i < fields.length; i++) {
			if (fields[i] != null) {
				sz = fields[i].size();
				break;
			}
		}
		i++;
		for (; i < fields.length; i++) {
			if (fields[i] != null) {
				if (sz != fields[i].size()) {
					return false;
				}
			}
		}
		return true;
	}

	private static void initializeFields(List<?>[] fields) {
		for (int i = 0; i < fields.length; i++) {
			switch (i) {
			case 0:
				fields[i] = new ArrayList<Object>();
				break;
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
			case 7:
				fields[i] = new ArrayList<String>();
				break;
			case 8:
			case 9:
				fields[i] = new ArrayList<Integer>();
				break;
			case 10:
			case 11:
				fields[i] = new ArrayList<Boolean>();
				break;
			}
		}
	}
}
