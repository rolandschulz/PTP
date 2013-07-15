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
package org.eclipse.ptp.internal.etfw.jaxb.util;

import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.internal.etfw.jaxb.data.ToolStateRuleType;

public class ToolStateRuleUtil {

	public static boolean evaluate(ToolStateRuleType toolStateRuleType, ILaunchConfiguration configuration) {

		if (toolStateRuleType.getNot() != null) {
			boolean result = ToolStateRuleUtil.evaluate(toolStateRuleType.getNot(), configuration);
			return result;
		}

		if (toolStateRuleType.getAnd() != null) {
			boolean result = ToolStateRuleUtil.evaluate(toolStateRuleType.getAnd(), configuration);
			return result;
		}

		if (toolStateRuleType.getOr() != null) {
			boolean result = ToolStateRuleUtil.evaluate(toolStateRuleType.getOr(), configuration);
			return result;
		}

		if (toolStateRuleType.getAttribute() != null) {
			boolean result = ToolStateRuleUtil.evaluate(toolStateRuleType.getAttribute(), configuration, toolStateRuleType.getValue());
			return result;
		}

		return true;
	}

	public static boolean evaluate(ToolStateRuleType.Not toolStateRuleTypeNot, ILaunchConfiguration configuration) {
		boolean result = !ToolStateRuleUtil.evaluate(toolStateRuleTypeNot.getRule(), configuration);
		return result;
	}

	public static boolean evaluate(ToolStateRuleType.And toolStateRuleTypeAnd, ILaunchConfiguration configuration) {
		List<ToolStateRuleType> list = toolStateRuleTypeAnd.getRule();
		for (ToolStateRuleType rule : list) {
			if (!ToolStateRuleUtil.evaluate(rule, configuration)) {
				return false;
			}
		}
		return true;
	}

	public static boolean evaluate(ToolStateRuleType.Or toolStateRuleTypeOr, ILaunchConfiguration configuration) {
		List<ToolStateRuleType> list = toolStateRuleTypeOr.getRule();
		for (ToolStateRuleType rule : list) {
			if (ToolStateRuleUtil.evaluate(rule, configuration)) {
				return true;
			}
		}
		return false;
	}
	
	public static boolean evaluate(String attribute, ILaunchConfiguration configuration,  String value) {
		/* Check if there is a value in the rule */
		if (value != null) {
			/*
			 * There is a value in the rule, so check if there is a value in the launch configuration for this attribute and if
			 * the values are or are not equal
			 */
			try {
				String configurationValue = configuration.getAttribute(attribute, (String) null);
				if (configurationValue != null) {
					/* Check if the value does or does not match with the value in the launch configuration */
					boolean result = value.equals(configurationValue);
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
				String configurationValue = configuration.getAttribute(attribute, (String) null);
				if (configurationValue != null) {
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
