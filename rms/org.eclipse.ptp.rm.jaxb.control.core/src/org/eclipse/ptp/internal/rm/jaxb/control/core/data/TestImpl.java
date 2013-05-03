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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rm.jaxb.control.core.IAssign;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.core.exceptions.StreamParserException;
import org.eclipse.ptp.internal.rm.jaxb.control.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.TestType;
import org.eclipse.ptp.rm.jaxb.core.data.TestType.Else;

/**
 * Wrapper implementation. A test consists of an if/else condition to be checked
 * in relation to the passed in values, plus a set of assignment actions to take
 * in the case of success. The values can be constants, references to be
 * resolved, or the current value of the target field (indicated by
 * "#fieldName").<br>
 * <br>
 * There are 8 allowable operators, indicated by the following strings: <br>
 * <br>
 * COMPARISON:<br>
 * EQ (=)<br>
 * LT (<)<br>
 * LE (<=)<br>
 * GT (>)<br>
 * GE (>=)<br>
 * <br>
 * LOGICAL:<br>
 * AND<br>
 * OR<br>
 * NOT<br>
 * <br>
 * In the case of a logical operator, the Test will delegate to its embedded
 * Test children, and then apply the operator to the results.<br>
 * <br>
 * To indicate an alternative action in the case the test fails, "<else>" is
 * used.
 * 
 * @author arossi
 * 
 */
public class TestImpl {

	private static final short sEQ = 0;
	private static final short sLT = 1;
	private static final short sGT = 2;
	private static final short sLE = 3;
	private static final short sGE = 4;
	private static final short sAND = 5;
	private static final short sOR = 6;
	private static final short sNOT = 7;

	private final IVariableMap rmVarMap;
	private final String uuid;
	private final short op;
	private final List<String> values;
	private List<TestImpl> children;
	private List<IAssign> ifcond;
	private List<IAssign> elsecond;

	private AttributeType target;

	/**
	 * @param uuid
	 *            unique id associated with this resource manager operation (can
	 *            be <code>null</code>).
	 * @param test
	 *            JAXB data element
	 * @param rmVarMap
	 *            resource manager environment
	 */
	public TestImpl(String uuid, TestType test, IVariableMap rmVarMap) {
		this.rmVarMap = rmVarMap;
		this.uuid = uuid;
		op = getOp(test.getOp());
		values = test.getValue();
		List<TestType> tests = test.getTest();
		if (!tests.isEmpty()) {
			children = new ArrayList<TestImpl>();
			for (TestType t : tests) {
				children.add(new TestImpl(uuid, t, rmVarMap));
			}
		}

		List<Object> listif = test.getAddOrAppendOrPut();
		if (!listif.isEmpty()) {
			ifcond = new ArrayList<IAssign>();
			for (Object o : listif) {
				AbstractAssign.add(uuid, o, ifcond, rmVarMap);
			}
		}

		Else listelse = test.getElse();
		if (listelse != null) {
			elsecond = new ArrayList<IAssign>();
			for (Object o : listelse.getAddOrAppendOrPut()) {
				AbstractAssign.add(uuid, o, elsecond, rmVarMap);
			}
		}
	}

	/**
	 * Applies the test.
	 * 
	 * @return whether the test succeeded or not.
	 * @throws StreamParserException
	 */
	public boolean doTest() throws StreamParserException {
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

		if (target != null) {
			if (result) {
				doAssign(ifcond);
			} else {
				doAssign(elsecond);
			}
		}
		return result;
	}

	/**
	 * The parent target to which to apply the actions associated with the test
	 * in case of success.
	 * 
	 * @param target
	 */
	public void setTarget(AttributeType target) {
		this.target = target;
		if (children != null) {
			for (TestImpl t : children) {
				t.setTarget(target);
			}
		}
	}

	/**
	 * Applies the assignments to the target.
	 * 
	 * @param assign
	 *            list of assignment actions.
	 * @throws Throwable
	 */
	private void doAssign(List<IAssign> assign) throws StreamParserException {
		if (assign != null) {
			for (IAssign a : assign) {
				a.setTarget(target);
				/*
				 * These will be using only preassigned values, so the tokens[]
				 * param is null
				 */
				a.assign(null);
			}
		}
	}

	/**
	 * Auxiliary. Applies <code>compareTo</code> to <code>Comparable</code> objects. Strings are first converted to boolean or
	 * integers if
	 * appropriate.
	 * 
	 * @param string1
	 *            to be compared
	 * @param string2
	 *            to be compared
	 * @return -1,0 or 1
	 * @throws CoreException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private int evaluateComparable(String string1, String string2) throws StreamParserException {
		Object value1 = AbstractAssign.normalizedValue(target, uuid, string1, true, rmVarMap);
		Object value2 = AbstractAssign.normalizedValue(target, uuid, string2, true, rmVarMap);
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

	/**
	 * Applies <code>equals</code> to objects. Strings are first converted to
	 * boolean or integers if appropriate.
	 * 
	 * @param string1
	 *            to be compared
	 * @param string2
	 *            to be compared
	 * @return whether the two values are equal
	 * @throws CoreException
	 */
	private boolean evaluateEquals(String string1, String string2) throws StreamParserException {
		Object value1 = AbstractAssign.normalizedValue(target, uuid, string1, true, rmVarMap);
		Object value2 = AbstractAssign.normalizedValue(target, uuid, string2, true, rmVarMap);
		if (value1 == null) {
			return value2 == null;
		}
		return value1.equals(value2);
	}

	/**
	 * Delegates to {@link #evaluateComparable(String, String)}
	 * 
	 * @param string1
	 *            to be compared
	 * @param string2
	 *            to be compared
	 * @return true if {@link #evaluateComparable(String, String)} returns -1.
	 * @throws CoreException
	 */
	private boolean evaluateLessThan(String string1, String string2) throws StreamParserException {
		return evaluateComparable(string1, string2) < 0;
	}

	/**
	 * Delegates to {@link #evaluateComparable(String, String)}
	 * 
	 * @param string1
	 *            to be compared
	 * @param string2
	 *            to be compared
	 * @return true if {@link #evaluateComparable(String, String)} returns -1 or
	 *         0.
	 * @throws CoreException
	 */
	private boolean evaluateLessThanOrEquals(String string1, String string2) throws StreamParserException {
		return evaluateComparable(string1, string2) <= 0;
	}

	/**
	 * Translates operator to string equivalent.
	 * 
	 * @param op
	 * @return string equivalent
	 */
	private String getOp(short op) {
		if (sEQ == op) {
			return JAXBControlConstants.xEQ;
		}
		if (sLT == op) {
			return JAXBControlConstants.xLT;
		}
		if (sGT == op) {
			return JAXBControlConstants.xGT;
		}
		if (sLE == op) {
			return JAXBControlConstants.xLE;
		}
		if (sGE == op) {
			return JAXBControlConstants.xGE;
		}
		if (sAND == op) {
			return JAXBControlConstants.AND;
		}
		if (sOR == op) {
			return JAXBControlConstants.OR;
		}
		if (sNOT == op) {
			return JAXBControlConstants.NOT;
		}
		return JAXBControlConstants.EQ;
	}

	/**
	 * Translates string representation of operator to numerical value.
	 * 
	 * @param op
	 *            string representation
	 * @return short value
	 */
	private short getOp(String op) {
		if (JAXBControlConstants.xEQ.equalsIgnoreCase(op)) {
			return sEQ;
		}
		if (JAXBControlConstants.xLT.equalsIgnoreCase(op)) {
			return sLT;
		}
		if (JAXBControlConstants.xGT.equalsIgnoreCase(op)) {
			return sGT;
		}
		if (JAXBControlConstants.xLE.equalsIgnoreCase(op)) {
			return sLE;
		}
		if (JAXBControlConstants.xGE.equalsIgnoreCase(op)) {
			return sGE;
		}
		if (JAXBControlConstants.AND.equalsIgnoreCase(op)) {
			return sAND;
		}
		if (JAXBControlConstants.OR.equalsIgnoreCase(op)) {
			return sOR;
		}
		if (JAXBControlConstants.NOT.equalsIgnoreCase(op)) {
			return sNOT;
		}
		return sEQ;
	}

	/**
	 * Checks that the number of values matches the operator.
	 * 
	 * @param op
	 * @throws Throwable
	 *             if number of values is incorrect
	 */
	private void validate(short op) throws StreamParserException {
		switch (op) {
		case sEQ:
		case sLT:
		case sGT:
		case sLE:
		case sGE:
			if (values == null || values.size() != 2) {
				throw new StreamParserException(Messages.MalformedExpressionError + getOp(op));
			}
			break;
		case sNOT:
			if (children == null || children.size() != 1) {
				throw new StreamParserException(Messages.MalformedExpressionError + getOp(op));
			}
			break;
		case sAND:
		case sOR:
			if (children == null || children.size() <= 1) {
				throw new StreamParserException(Messages.MalformedExpressionError + getOp(op));
			}
			break;
		}
	}
}
