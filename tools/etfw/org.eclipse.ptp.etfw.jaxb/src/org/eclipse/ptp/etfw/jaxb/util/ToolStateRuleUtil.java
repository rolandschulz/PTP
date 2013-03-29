/*******************************************************************************
 * Copyright (c) 2009, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.jaxb.util;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.jaxb.data.ToolStateRuleType;

public class ToolStateRuleUtil {

	public static boolean evaluate(ILaunchConfiguration configuration, ToolStateRuleType toolStateRuleType) {

		if (toolStateRuleType.getNot() != null) {
			boolean result = ToolStateRuleUtil.evaluate(configuration, toolStateRuleType.getNot());
			return result;
		}

		if (toolStateRuleType.getAnd() != null) {
			boolean result = ToolStateRuleUtil.evaluate(configuration, toolStateRuleType.getAnd());
			return result;
		}

		if (toolStateRuleType.getOr() != null) {
			boolean result = ToolStateRuleUtil.evaluate(configuration, toolStateRuleType.getOr());
			return result;
		}

		if (toolStateRuleType.getAttribute() != null) {
			boolean result = ToolStateRuleUtil.evaluate(configuration, toolStateRuleType.getAttribute(), toolStateRuleType.getValue());
			return result;
		}

		return true;
	}

	public static boolean evaluate(ILaunchConfiguration configuration, ToolStateRuleType.Not toolStateRuleTypeNot) {
		boolean result = !ToolStateRuleUtil.evaluate(configuration, toolStateRuleTypeNot.getRule());
		return result;
	}

	public static boolean evaluate(ILaunchConfiguration configuration, ToolStateRuleType.And toolStateRuleTypeAnd) {
		List<ToolStateRuleType> list = toolStateRuleTypeAnd.getRule();
		for (ToolStateRuleType rule : list) {
			if (!ToolStateRuleUtil.evaluate(configuration, rule)) {
				return false;
			}
		}
		return true;
	}

	public static boolean evaluate(ILaunchConfiguration configuration, ToolStateRuleType.Or toolStateRuleTypeOr) {
		List<ToolStateRuleType> list = toolStateRuleTypeOr.getRule();
		for (ToolStateRuleType rule : list) {
			if (ToolStateRuleUtil.evaluate(configuration, rule)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean evaluate(ILaunchConfiguration configuration, String ruleAttribute, String ruleValue) {
		/* Check if there is a value in the rule */
		if (ruleValue != null) {
			/*
			 * There is a value in the rule, so check if there is a value in the launch configuration for this attribute and if
			 * the values are or are not equal
			 */
			try {
				Map attrs = configuration.getAttributes();
				for (Object key : attrs.keySet()) {
					if (key instanceof String && ((String)key).endsWith("x")) {
					}
				}
				String configurationValue = configuration.getAttribute(ruleAttribute, (String) null);
				if (configurationValue != null) {
					/* Check if the value does or does not match with the value in the launch configuration */
					boolean result = ruleValue.equals(configurationValue);
					return result;
				}
			} catch (CoreException e) {
				// Ignore
			}
		} else {
			/*
			 * There is no value in the rule, so check if there is a value in the launch configuration for this attribute, that
			 * is, if the attribute is or is not defined
			 */
			try {
				String value = configuration.getAttribute(ruleAttribute, (String) null);
				if (value != null) {
					/* Value is defined in the launch configuration, that is, the attribute is defined */
					return true;
				}
			} catch (CoreException e) {
				// Ignore
			}
		}
		/* Value is not defined in the launch configuration or there was an exception */
		return false;
	}

}
