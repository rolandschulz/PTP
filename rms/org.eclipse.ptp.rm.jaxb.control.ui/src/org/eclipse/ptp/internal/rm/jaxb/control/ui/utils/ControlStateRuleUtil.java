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
	 * @param controlStateRuleType
	 * @param buttonMap
	 * @param sources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType controlStateRuleType, Map<String, Button> buttonMap, Set<Button> sources)
			throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources controlStateRuleType=" + controlStateRuleType + " buttonMap="
					+ buttonMap + " sources=" + sources);

		if (controlStateRuleType.getNot() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getNot(), buttonMap, sources);
		}

		if (controlStateRuleType.getAnd() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getAnd(), buttonMap, sources);
		}

		if (controlStateRuleType.getOr() != null) {
			ControlStateRuleUtil.addSources(controlStateRuleType.getOr(), buttonMap, sources);
		}

		if (controlStateRuleType.getButton() != null) {
			String buttonKey = controlStateRuleType.getButton();
			Button button = buttonMap.get(buttonKey);
			if (button != null) {
				sources.add(button);
			} else {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_0, controlStateRuleType.getButton()));
			}
		}

		if (controlStateRuleType.getAttribute() != null) {
			String attribute = controlStateRuleType.getAttribute();
			if (attribute == null) {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_1, controlStateRuleType.getAttribute()));
			}
			String value = controlStateRuleType.getValue();
			if (value == null) {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_2, controlStateRuleType.getAttribute()));
			}
		}

	}

	/**
	 * @param controlStateRuleTypeNot
	 * @param buttonMap
	 * @param sources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.Not controlStateRuleTypeNot, Map<String, Button> buttonMap,
			Set<Button> sources) throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources controlStateRuleTypeNot=" + controlStateRuleTypeNot
					+ " buttonMap=" + buttonMap + " sources=" + sources);

		ControlStateRuleUtil.addSources(controlStateRuleTypeNot.getRule(), buttonMap, sources);

	}

	/**
	 * @param controlStateRuleTypeAnd
	 * @param buttonMap
	 * @param sources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.And controlStateRuleTypeAnd, Map<String, Button> buttonMap,
			Set<Button> sources) throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources controlStateRuleTypeAnd=" + controlStateRuleTypeAnd
					+ " buttonMap=" + buttonMap + " sources=" + sources);

		List<ControlStateRuleType> list = controlStateRuleTypeAnd.getRule();
		for (ControlStateRuleType rule : list) {
			ControlStateRuleUtil.addSources(rule, buttonMap, sources);
		}

	}

	/**
	 * @param controlStateRuleTypeOr
	 * @param buttonMap
	 * @param sources
	 * @throws CoreException
	 */
	public static void addSources(ControlStateRuleType.Or controlStateRuleTypeOr, Map<String, Button> buttonMap, Set<Button> sources)
			throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.addSources controlStateRuleTypeOr=" + controlStateRuleTypeOr
					+ " buttonMap=" + buttonMap + " sources=" + sources);

		List<ControlStateRuleType> list = controlStateRuleTypeOr.getRule();
		for (ControlStateRuleType rule : list) {
			ControlStateRuleUtil.addSources(rule, buttonMap, sources);
		}

	}

	/**
	 * @param controlStateRuleType
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType controlStateRuleType, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate controlStateRuleType=" + controlStateRuleType
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap);

		if (controlStateRuleType.getNot() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getNot(), buttonMap, varianbleMap);
			return result;
		}

		if (controlStateRuleType.getAnd() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getAnd(), buttonMap, varianbleMap);
			return result;
		}

		if (controlStateRuleType.getOr() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getOr(), buttonMap, varianbleMap);
			return result;
		}

		if (controlStateRuleType.getButton() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getButton(), buttonMap,
					controlStateRuleType.isSelected());
			return result;
		}

		if (controlStateRuleType.getAttribute() != null) {
			boolean result = ControlStateRuleUtil.evaluate(controlStateRuleType.getAttribute(), varianbleMap,
					controlStateRuleType.getValue());
			return result;
		}

		return true;
	}

	/**
	 * @param controlStateRuleTypeNot
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.Not controlStateRuleTypeNot, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate controlStateRuleTypeNot=" + controlStateRuleTypeNot
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap);

		boolean result = !ControlStateRuleUtil.evaluate(controlStateRuleTypeNot.getRule(), buttonMap, varianbleMap);
		return result;

	}

	/**
	 * @param controlStateRuleTypeAnd
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.And controlStateRuleTypeAnd, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate controlStateRuleTypeAnd=" + controlStateRuleTypeAnd
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap);

		List<ControlStateRuleType> list = controlStateRuleTypeAnd.getRule();
		for (ControlStateRuleType rule : list) {
			if (!ControlStateRuleUtil.evaluate(rule, buttonMap, varianbleMap)) {
				return false;
			}
		}
		return true;

	}

	/**
	 * @param controlStateRuleTypeOr
	 * @param buttonMap
	 * @param varianbleMap
	 * @return
	 */
	public static boolean evaluate(ControlStateRuleType.Or controlStateRuleTypeOr, Map<String, Button> buttonMap,
			IVariableMap varianbleMap) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate controlStateRuleTypeOr=" + controlStateRuleTypeOr
					+ " buttonMap=" + buttonMap + " varianbleMap=" + varianbleMap);

		List<ControlStateRuleType> list = controlStateRuleTypeOr.getRule();
		for (ControlStateRuleType rule : list) {
			if (ControlStateRuleUtil.evaluate(rule, buttonMap, varianbleMap)) {
				return true;
			}
		}
		return false;

	}

	/**
	 * @param ruleButton
	 * @param buttonMap
	 * @param ruleSelected
	 * @return
	 */
	public static boolean evaluate(String ruleButton, Map<String, Button> buttonMap, boolean ruleSelected) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate ruleButton=" + ruleButton
					+ " buttonMap=" + buttonMap + " ruleSelected=" + ruleSelected);

		Button button = buttonMap.get(ruleButton);
		if (button != null) {
			return ruleSelected == button.getSelection();
		}
		return false;

	}

	/**
	 * @param ruleAttribute
	 * @param varianbleMap
	 * @param ruleValue
	 * @return
	 */
	public static boolean evaluate(String ruleAttribute, IVariableMap varianbleMap, String ruleValue) {

		if (DEBUG)
			System.out.println("Enter ControlStateRuleUtil.evaluate ruleAttribute=" + ruleAttribute
					+ " varianbleMap=" + varianbleMap + " ruleValue=" + ruleValue);

		/* Check if there is a value in the rule */
		if (ruleValue != null) {
			/*
			 * There is a value in the rule, so check if there is a value in the variable map for this attribute and if
			 * the values are or are not equal
			 */
			AttributeType attributeType = varianbleMap.get(ruleAttribute);
			if (attributeType != null && attributeType.getValue() != null) {
				/* Check if the value does or does not match with the value in the launch configuration */
				boolean result = ruleValue.equals(attributeType.getValue().toString());
				return result;
			}
		} else {
			/*
			 * There is no value in the rule, so check if there is a value in the variable map for this attribute, that
			 * is, if the attribute is or is not defined
			 */
			AttributeType attributeType = varianbleMap.get(ruleAttribute);
			if (attributeType != null) {
				/* Value is defined in the launch configuration, that is, the attribute is defined */
				return true;
			}
		}
		/* Value is not defined in the launch configuration or there was an exception */
		return false;
	}

}
