/*******************************************************************************
 * Copyright (c) 2013 Brian Watt.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 	
 * Contributors: 
 * Brian D. Watt - design and implementation
 ******************************************************************************/

package org.eclipse.ptp.internal.rm.jaxb.control.ui.utils;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateRuleType;
import org.eclipse.swt.widgets.Button;

public class ControlStateRuleUtil {

	private static final boolean DEBUG = false;

	/**
	 * Search the ControlStateRule for button-elements, look them up to find their associated SWT button and add them to the set of
	 * source buttons. If a not-element, an and-element or an or-element are encountered then descend into their elements and
	 * continue searching for buttons. If an attribute-element is encountered just check it, but otherwise do nothing with it.
	 * 
	 * @param controlStateRuleType
	 * @param buttonMap
	 * @param buttonSources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType controlStateRuleType, Map<String, Button> buttonMap,
			Set<Button> buttonSources)
			throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources controlStateRuleType=" + controlStateRuleType + " buttonMap=" //$NON-NLS-1$ //$NON-NLS-2$
					+ buttonMap + " buttonSources=" + buttonSources); //$NON-NLS-1$

		if (controlStateRuleType.getNot() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getNot(), buttonMap, buttonSources);
		}

		if (controlStateRuleType.getAnd() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getAnd(), buttonMap, buttonSources);
		}

		if (controlStateRuleType.getOr() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getOr(), buttonMap, buttonSources);
		}

		if (controlStateRuleType.getButton() != null) {
			String buttonKey = controlStateRuleType.getButton();
			Button button = buttonMap.get(buttonKey);
			if (button != null) {
				buttonSources.add(button);
			} else {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRuleUtil_0, controlStateRuleType.getButton()));
			}
		}

		if (controlStateRuleType.getAttribute() != null) {
			String attribute = controlStateRuleType.getAttribute();
			if (attribute == null) {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRuleUtil_1, controlStateRuleType.getAttribute()));
			}
			String value = controlStateRuleType.getValue();
			if (value == null) {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRuleUtil_2, controlStateRuleType.getAttribute()));
			}
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateRuleUtil.addSources buttonSources=" + buttonSources); //$NON-NLS-1$

	}

	/**
	 * Search a not-element by descending into its element and adding buttons.
	 * 
	 * @param controlStateRuleTypeNot
	 * @param buttonMap
	 * @param buttonSources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.Not controlStateRuleTypeNot, Map<String, Button> buttonMap,
			Set<Button> buttonSources) throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources Not controlStateRuleTypeNot=" + controlStateRuleTypeNot //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " sources=" + buttonSources); //$NON-NLS-1$ //$NON-NLS-2$

		ControlStateRuleUtil.addSources(controlStateRuleTypeNot.getRule(), buttonMap, buttonSources);

		if (DEBUG)
			System.out.println("Exit1 ControlStateRuleUtil.addSources Not buttonSources=" + buttonSources); //$NON-NLS-1$

	}

	/**
	 * Search an and-element by descending into its list of elements and adding buttons.
	 * 
	 * @param controlStateRuleTypeAnd
	 * @param buttonMap
	 * @param buttonSources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.And controlStateRuleTypeAnd, Map<String, Button> buttonMap,
			Set<Button> buttonSources) throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources And controlStateRuleTypeAnd=" + controlStateRuleTypeAnd //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " sources=" + buttonSources); //$NON-NLS-1$ //$NON-NLS-2$

		List<ControlStateRuleType> list = controlStateRuleTypeAnd.getRule();
		for (ControlStateRuleType rule : list) {
			ControlStateRuleUtil.addSources(rule, buttonMap, buttonSources);
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateRuleUtil.addSources And buttonSources=" + buttonSources); //$NON-NLS-1$

	}

	/**
	 * Search an or-element by descending into its list of elements and adding buttons.
	 * 
	 * @param controlStateRuleTypeOr
	 * @param buttonMap
	 * @param buttonSources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.Or controlStateRuleTypeOr, Map<String, Button> buttonMap,
			Set<Button> buttonSources)
			throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources Or controlStateRuleTypeOr=" + controlStateRuleTypeOr //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " buttonSources=" + buttonSources); //$NON-NLS-1$ //$NON-NLS-2$

		List<ControlStateRuleType> list = controlStateRuleTypeOr.getRule();
		for (ControlStateRuleType rule : list) {
			ControlStateRuleUtil.addSources(rule, buttonMap, buttonSources);
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateRuleUtil.addSources Or buttonSources=" + buttonSources); //$NON-NLS-1$

	}

	/**
	 * Evaluate the ControlStateRule and return a boolean result. If a not-element, an and-element or an or-element are encountered
	 * then descend into their elements and evaluate them. If a button-element is encountered then evaluate it. If an
	 * attribute-element is encountered then evaluate it too.
	 * 
	 * @param controlStateRuleType
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType controlStateRuleType, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate controlStateRuleType=" + controlStateRuleType //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap); //$NON-NLS-1$ //$NON-NLS-2$

		if (controlStateRuleType.getNot() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getNot(), buttonMap, varianbleMap);

			if (DEBUG)
				System.out.println("Exit1 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

			return result;
		}

		if (controlStateRuleType.getAnd() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getAnd(), buttonMap, varianbleMap);

			if (DEBUG)
				System.out.println("Exit2 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

			return result;
		}

		if (controlStateRuleType.getOr() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getOr(), buttonMap, varianbleMap);

			if (DEBUG)
				System.out.println("Exit3 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

			return result;
		}

		if (controlStateRuleType.getButton() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getButton(), buttonMap,
					controlStateRuleType.isSelected());

			if (DEBUG)
				System.out.println("Exit4 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

			return result;
		}

		if (controlStateRuleType.getAttribute() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getAttribute(), varianbleMap,
					controlStateRuleType.getValue());

			if (DEBUG)
				System.out.println("Exit5 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

			return result;
		}
		boolean result = true;

		if (DEBUG)
			System.out.println("Exit6 ControlStateRuleUtil.evaluate result=" + result); //$NON-NLS-1$

		return result;
	}

	/**
	 * Evaluate a not-element by descending into its element, evaluating it and returning the NOT (!) of the result.
	 * 
	 * @param controlStateRuleTypeNot
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.Not controlStateRuleTypeNot, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate Not controlStateRuleTypeNot=" + controlStateRuleTypeNot //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap); //$NON-NLS-1$ //$NON-NLS-2$

		boolean result = !ControlStateRuleUtil.evaluate(controlStateRuleTypeNot.getRule(), buttonMap, varianbleMap);

		if (DEBUG)
			System.out.println("Exit1 ControlStateRuleUtil.evaluate Not result=" + result); //$NON-NLS-1$

		return result;

	}

	/**
	 * Evaluate an and-element by descending into its list of elements, evaluating them and returning the AND (&&) of the results.
	 * 
	 * @param controlStateRuleTypeAnd
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.And controlStateRuleTypeAnd, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate And controlStateRuleTypeAnd=" + controlStateRuleTypeAnd //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap); //$NON-NLS-1$ //$NON-NLS-2$

		List<ControlStateRuleType> list = controlStateRuleTypeAnd.getRule();
		for (ControlStateRuleType rule : list) {
			if (!ControlStateRuleUtil.evaluate(rule, buttonMap, varianbleMap)) {
				boolean result = false;

				if (DEBUG)
					System.out.println("Exit1 ControlStateRuleUtil.evaluate And result=" + result); //$NON-NLS-1$

				return result;
			}
		}
		boolean result = true;

		if (DEBUG)
			System.out.println("Exit2 ControlStateRuleUtil.evaluate And result=" + result); //$NON-NLS-1$

		return result;

	}

	/**
	 * Evaluate an or-element by descending into its list of elements, evaluating them and returning the OR (||) of the results.
	 * 
	 * @param controlStateRuleTypeOr
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.Or controlStateRuleTypeOr, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate Or controlStateRuleTypeOr=" + controlStateRuleTypeOr //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap); //$NON-NLS-1$ //$NON-NLS-2$

		List<ControlStateRuleType> list = controlStateRuleTypeOr.getRule();
		for (ControlStateRuleType rule : list) {
			if (ControlStateRuleUtil.evaluate(rule, buttonMap, varianbleMap)) {
				boolean result = true;

				if (DEBUG)
					System.out.println("Exit1 ControlStateRuleUtil.evaluate Or result=" + result); //$NON-NLS-1$

				return result;
			}
		}
		boolean result = false;

		if (DEBUG)
			System.out.println("Exit2 ControlStateRuleUtil.evaluate Or result=" + result); //$NON-NLS-1$

		return result;

	}

	/**
	 * Evaluate a button-element by getting the button, getting its selection and comparing it with the selected-element value.
	 * 
	 * @param ruleButton
	 * @param buttonMap
	 * @param ruleSelected
	 * @return
	 */
	public static boolean evaluate(String ruleButton, Map<String, Button> buttonMap, boolean ruleSelected) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate Button ruleButton=" + ruleButton //$NON-NLS-1$
					+ " buttonMap=" + buttonMap + " ruleSelected=" + ruleSelected); //$NON-NLS-1$ //$NON-NLS-2$

		Button button = buttonMap.get(ruleButton);
		if (button != null) {
			boolean result = ruleSelected == button.getSelection();

			if (DEBUG)
				System.out.println("Exit1 ControlStateRuleUtil.evaluate Button result=" + result); //$NON-NLS-1$

			return result;
		}
		boolean result = false;

		if (DEBUG)
			System.out.println("Exit2 ControlStateRuleUtil.evaluate Button result=" + result); //$NON-NLS-1$

		return result;

	}

	/**
	 * Evaluate an attribute-element by getting the attribute, getting its value and comparing it with the value-element value.
	 * 
	 * @param ruleAttribute
	 * @param variableMap
	 * @param ruleValue
	 * @return
	 */
	public static boolean evaluate(String ruleAttribute, IVariableMap variableMap, String ruleValue) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate Attribute attribute=" + ruleAttribute //$NON-NLS-1$
					+ " varianbleMap=" + variableMap + " value=" + ruleValue); //$NON-NLS-1$ //$NON-NLS-2$

		/* Check if there is a value in the rule */
		if (ruleValue != null) {
			/*
			 * There is a value in the rule, so check if there is a value in the variable map for this attribute and if
			 * the values are or are not equal
			 */
			AttributeType attributeType = variableMap.get(ruleAttribute);
			if (attributeType != null && attributeType.getValue() != null) {
				/* Check if the value does or does not match with the value in the launch configuration */
				boolean result = ruleValue.equals(attributeType.getValue().toString());

				if (DEBUG)
					System.out.println("Exit1 ControlStateRuleUtil.evaluate Attribute result=" + result); //$NON-NLS-1$

				return result;
			}
		} else {
			/*
			 * There is no value in the rule, so check if there is a value in the variable map for this attribute, that
			 * is, if the attribute is or is not defined
			 */
			AttributeType attributeType = variableMap.get(ruleAttribute);
			if (attributeType != null) {
				/* Value is defined in the launch configuration, that is, the attribute is defined */
				boolean result = true;

				if (DEBUG)
					System.out.println("Exit2 ControlStateRuleUtil.evaluate Attribute result=" + result); //$NON-NLS-1$

				return result;
			}
		}
		/* Value is not defined in the launch configuration or there was an exception */
		boolean result = false;

		if (DEBUG)
			System.out.println("Exit3 ControlStateRuleUtil.evaluate Attribute result=" + result); //$NON-NLS-1$

		return result;
	}

}
