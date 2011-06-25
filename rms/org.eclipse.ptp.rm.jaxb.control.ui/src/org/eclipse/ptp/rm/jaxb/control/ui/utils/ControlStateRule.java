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
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;

/**
 * Implementation of the rule defined against source states.
 * 
 * @author arossi
 * 
 */
public class ControlStateRule {

	public enum EventType {
		VISIBLE, ENABLED, SELECTED, NONE;

		public static EventType get(String type) {
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
	private EventType eventType;

	/**
	 * Composes the rule; also wires the source with the target listener.
	 * 
	 * @param rule
	 *            JAXB element
	 * @param map
	 *            index of Widget controls
	 */
	public ControlStateRule(ControlStateRuleType rule, Map<String, Control> map, ControlStateListener listener) {
		ControlStateRuleType type = rule.getNot();
		if (type != null) {
			not = new ControlStateRule(type, map, listener);
		} else {
			List<ControlStateRuleType> list = rule.getAnd();
			if (!list.isEmpty()) {
				and = new ArrayList<ControlStateRule>();
				for (ControlStateRuleType t : list) {
					and.add(new ControlStateRule(t, map, listener));
				}
			} else {
				list = rule.getOr();
				if (!list.isEmpty()) {
					or = new ArrayList<ControlStateRule>();
					for (ControlStateRuleType t : list) {
						or.add(new ControlStateRule(t, map, listener));
					}
				} else {
					control = map.get(rule.getSource());
					if (control != null) {
						eventType = EventType.get(rule.getEventType());
						control.addListener(SWT.Activate, listener);
						control.addListener(SWT.Deactivate, listener);
						control.addListener(SWT.Show, listener);
						control.addListener(SWT.Hide, listener);
						if (control instanceof Button) {
							control.addListener(SWT.Selection, listener);
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
	 * @return
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
		switch (eventType) {
		case ENABLED:
			return control.getEnabled();
		case VISIBLE:
			return control.getVisible();
		case SELECTED:
			if (control instanceof Button) {
				return ((Button) control).getSelection();
			}
			break;
		}
		return false;
	}
}
