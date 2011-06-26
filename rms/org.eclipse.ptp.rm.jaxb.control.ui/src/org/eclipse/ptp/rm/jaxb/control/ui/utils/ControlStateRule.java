/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateRuleType;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of the rule defined against source states.
 * 
 * @author arossi
 * 
 */
public class ControlStateRule {

	public enum ControlState {
		VISIBLE, ENABLED, SELECTED, NONE;

		public static ControlState get(String type) {
			if ("VISIBLE".equals(type)) { //$NON-NLS-1$
				return VISIBLE;
			}
			if ("ENABLED".equals(type)) { //$NON-NLS-1$
				return ENABLED;
			}
			if ("SELECTED".equals(type)) { //$NON-NLS-1$
				return SELECTED;
			}
			return NONE;
		}
	};

	private ControlStateRule not;
	private List<ControlStateRule> and;
	private List<ControlStateRule> or;
	private Control control;
	private ControlState state;

	/**
	 * Composes the rule; also wires the source with the target listener.
	 * 
	 * @param rule
	 *            JAXB element
	 * @param map
	 *            index of Widget controls
	 * @param listener
	 *            target listener on which to set this rule
	 * @param sources
	 *            dependencies held by the rule
	 */
	public ControlStateRule(ControlStateRuleType rule, Map<String, Control> map, ControlStateListener listener,
			List<Control> sources) {
		ControlStateRuleType.Not not = rule.getNot();
		if (not != null) {
			this.not = new ControlStateRule(not.getRule(), map, listener, sources);
		} else {
			ControlStateRuleType.And and = rule.getAnd();
			if (and != null) {
				List<ControlStateRuleType> list = and.getRule();
				if (!list.isEmpty()) {
					this.and = new ArrayList<ControlStateRule>();
					for (ControlStateRuleType t : list) {
						this.and.add(new ControlStateRule(t, map, listener, sources));
					}
				}
			} else {
				ControlStateRuleType.Or or = rule.getOr();
				if (or != null) {
					List<ControlStateRuleType> list = or.getRule();
					if (!list.isEmpty()) {
						this.or = new ArrayList<ControlStateRule>();
						for (ControlStateRuleType t : list) {
							this.or.add(new ControlStateRule(t, map, listener, sources));
						}
					}
				} else {
					control = map.get(rule.getSource());
					sources.add(control);
					state = ControlState.get(rule.getState());
					for (String trigger : rule.getTrigger()) {
						Control t = map.get(trigger);
						if (t != null && t instanceof Button) {
							((Button) t).addSelectionListener(listener);
						}
					}
				}
			}
		}
	}

	/**
	 * Triggered when an event is received. Recursively evaluates all clauses.
	 * Bottoms out in the check of the control state indicated by the eventType
	 * element.
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
		switch (state) {
		case ENABLED:
			return control.getEnabled();
		case VISIBLE:
			return control.getVisible();
		case SELECTED:
			if (control instanceof Button) {
				return ((Button) control).getSelection();
			}
		}
		return false;
	}
}
