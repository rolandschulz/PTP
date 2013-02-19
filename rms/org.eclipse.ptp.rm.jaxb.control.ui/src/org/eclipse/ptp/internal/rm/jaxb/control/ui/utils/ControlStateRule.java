/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.utils;

import java.util.ArrayList;
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

/**
 * Implementation of the rule defined against button selection.
 * 
 * @author arossi
 * 
 */
public class ControlStateRule {

	private ControlStateRule not;
	private List<ControlStateRule> and;
	private List<ControlStateRule> or;
	private Button button;
	private boolean selected;
	private String attribute;
	private String value;
	private final IVariableMap varMap;

	/**
	 * Composes the rule; adds each button source to the set which the listener will subscribe to for events.
	 * 
	 * @param rule
	 *            JAXB element
	 * @param map
	 *            index of Widget controls
	 * @param sources
	 *            set of Buttons which are the sources in this rule
	 */
	public ControlStateRule(ControlStateRuleType rule, Map<String, Button> map, Set<Button> sources, IVariableMap varMap)
			throws CoreException {
		this.varMap = varMap;
		ControlStateRuleType.Not not = rule.getNot();
		if (not != null) {
			this.not = new ControlStateRule(not.getRule(), map, sources, varMap);
		} else {
			ControlStateRuleType.And and = rule.getAnd();
			if (and != null) {
				List<ControlStateRuleType> list = and.getRule();
				if (!list.isEmpty()) {
					this.and = new ArrayList<ControlStateRule>();
					for (ControlStateRuleType t : list) {
						this.and.add(new ControlStateRule(t, map, sources, varMap));
					}
				}
			} else {
				ControlStateRuleType.Or or = rule.getOr();
				if (or != null) {
					List<ControlStateRuleType> list = or.getRule();
					if (!list.isEmpty()) {
						this.or = new ArrayList<ControlStateRule>();
						for (ControlStateRuleType t : list) {
							this.or.add(new ControlStateRule(t, map, sources, varMap));
						}
					}
				} else {
					if (rule.getButton() != null) {
						button = map.get(rule.getButton());
						if (button == null) {
							throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_0, rule.getButton()));
						}
						selected = rule.isSelected();
						sources.add(button);
					} else if (rule.getAttribute() != null) {
						attribute = rule.getAttribute();
						if (attribute == null) {
							throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_1, rule.getAttribute()));
						}
						value = rule.getValue();
						if (value == null) {
							throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateRule_2, rule.getAttribute()));
						}
					}
				}
			}
		}
	}

	/**
	 * Triggered when an event is received. Recursively evaluates all clauses. Bottoms out in the check of the button selection.
	 * 
	 * @return whether the rule is satisfied
	 */
	public boolean evaluate() {
		if (not != null) {
			return !not.evaluate();
		}
		if (and != null) {
			for (ControlStateRule rule : and) {
				if (!rule.evaluate()) {
					return false;
				}
			}
			return true;
		}
		if (or != null) {
			for (ControlStateRule rule : or) {
				if (rule.evaluate()) {
					return true;
				}
			}
			return false;
		}
		if (attribute != null) {
			AttributeType attr = varMap.get(attribute);
			String attrVal = null;
			if (attr != null && attr.getValue() != null) {
				attrVal = attr.getValue().toString();
			}
			return value.equals(attrVal);
		}
		return selected == button.getSelection();
	}
}
