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

import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.data.Test;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

public class TestImpl implements IJAXBNonNLSConstants {

	private static final short sEQ = 0;
	private static final short sLT = 1;
	private static final short sGT = 2;
	private static final short sLE = 3;
	private static final short sGE = 4;
	private static final short sAND = 5;
	private static final short sOR = 6;
	private static final short sNOT = 7;

	private final String uuid;
	private final short op;
	private final String set;
	private final List<String> values;
	private List<TestImpl> children;

	private Object target;

	public TestImpl(String uuid, Test test) {
		this.uuid = uuid;
		op = getOp(test.getOp());
		set = test.getSet();
		values = test.getValue();
		List<Test> tests = test.getTest();
		if (!tests.isEmpty()) {
			children = new ArrayList<TestImpl>();
			for (Test t : tests) {
				children.add(new TestImpl(uuid, t));
			}
		}
	}

	public boolean doTest() throws Throwable {
		boolean result = false;
		validate(op);
		switch (op) {
		case sEQ:
			result = evaluateEquals(values.get(0), values.get(1));
			break;
		case sLT:
			result = evaluateLessThan(values.get(0), values.get(1));
			break;
		case sGT:
			result = evaluateLessThan(values.get(1), values.get(0));
			break;
		case sLE:
			result = evaluateLessThanOrEquals(values.get(0), values.get(1));
			break;
		case sGE:
			result = evaluateLessThanOrEquals(values.get(1), values.get(0));
			break;
		case sNOT:
			return !children.get(0).doTest();
		case sAND:
			result = true;
			for (TestImpl t : children) {
				result = result && t.doTest();
				if (!result) {
					break;
				}
			}
			break;
		case sOR:
			result = false;
			for (TestImpl t : children) {
				result = result || t.doTest();
				if (result) {
					break;
				}
			}
			break;
		}

		if (set != null && target != null) {
			AbstractAssign.set(target, set, new Object[] { result });
		}

		return result;
	}

	public void setTarget(Object target) {
		this.target = target;
		if (children != null) {
			for (TestImpl t : children) {
				t.setTarget(target);
			}
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int evaluateComparable(String string1, String string2) throws Throwable {
		Object value1 = getValue(string1);
		Object value2 = getValue(string2);
		if (value1 == null || value2 == null) {
			return 1;
		}
		if (!(value1 instanceof Comparable) || !(value2 instanceof Comparable)) {
			return 1;
		}
		Comparable c1 = (Comparable) value1;
		Comparable c2 = (Comparable) value2;
		return c1.compareTo(c2);
	}

	private boolean evaluateEquals(String string1, String string2) throws Throwable {
		Object value1 = getValue(string1);
		Object value2 = getValue(string2);
		if (value1 == null) {
			return value2 == null;
		}
		return value1.equals(value2);
	}

	private boolean evaluateLessThan(String string1, String string2) throws Throwable {
		return evaluateComparable(string1, string2) < 0;
	}

	private boolean evaluateLessThanOrEquals(String string1, String string2) throws Throwable {
		return evaluateComparable(string1, string2) <= 0;
	}

	private String getOp(short op) {
		if (sEQ == op) {
			return xEQ;
		}
		if (sLT == op) {
			return xLT;
		}
		if (sGT == op) {
			return xGT;
		}
		if (sLE == op) {
			return xLE;
		}
		if (sGE == op) {
			return xGE;
		}
		if (sAND == op) {
			return AND;
		}
		if (sOR == op) {
			return OR;
		}
		if (sNOT == op) {
			return NOT;
		}
		return EQ;
	}

	private short getOp(String op) {
		if (xEQ.equalsIgnoreCase(op)) {
			return sEQ;
		}
		if (xLT.equalsIgnoreCase(op)) {
			return sLT;
		}
		if (xGT.equalsIgnoreCase(op)) {
			return sGT;
		}
		if (xLE.equalsIgnoreCase(op)) {
			return sLE;
		}
		if (xGE.equalsIgnoreCase(op)) {
			return sGE;
		}
		if (AND.equalsIgnoreCase(op)) {
			return sAND;
		}
		if (OR.equalsIgnoreCase(op)) {
			return sOR;
		}
		if (NOT.equalsIgnoreCase(op)) {
			return sNOT;
		}
		return sEQ;
	}

	private Object getValue(String expression) throws Throwable {
		if (expression.startsWith(THIS)) {
			if (target == null) {
				return null;
			}
			String field = expression.substring(5);
			return AbstractAssign.get(target, field);
		} else if (expression.indexOf(OPENV) >= 0) {
			expression = RMVariableMap.getActiveInstance().getString(uuid, expression);
			return RMVariableMap.getActiveInstance().getString(uuid, expression);
		} else {
			if (TRUE.equalsIgnoreCase(expression)) {
				return true;
			}
			if (FALSE.equalsIgnoreCase(expression)) {
				return false;
			}
			try {
				if (expression.indexOf(DOT) >= 0) {
					return new Double(expression);
				}
				return new Integer(expression);
			} catch (NumberFormatException nfe) {
				return expression;
			}
		}
	}

	private void validate(short op) throws Throwable {
		switch (op) {
		case sEQ:
		case sLT:
		case sGT:
		case sLE:
		case sGE:
			if (values == null || values.size() != 2) {
				throw CoreExceptionUtils.newException(Messages.MalformedExpressionError + getOp(op), null);
			}
			break;
		case sNOT:
			if (children == null || children.size() != 1) {
				throw CoreExceptionUtils.newException(Messages.MalformedExpressionError + getOp(op), null);
			}
			break;
		case sAND:
		case sOR:
			if (children == null || children.size() <= 1) {
				throw CoreExceptionUtils.newException(Messages.MalformedExpressionError + getOp(op), null);
			}
			break;
		}
	}
}
