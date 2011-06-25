/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.handlers;

import java.util.Map;

import org.eclipse.ptp.rm.jaxb.control.ui.utils.ControlStateRule;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateRuleType;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * This untyped listener is added to the widgets on which the composed control
 * depends for its state.<br>
 * 
 * The listener is constructed on the basis of an event rule which is passed in
 * specifying these dependencies. The events here are limited to activation and
 * visibility for all widgets, and selection/deselection for buttons.
 * 
 * @author arossi
 * 
 */
public class ControlStateListener implements Listener {

	public enum Action {
		ENABLE, DISABLE, SHOW, HIDE, SELECT, DESELECT, NONE;
	};

	private final ControlStateRule rule;
	private final Control target;
	private final Action action;

	/**
	 * @param target
	 *            control on which to set state
	 * @param rule
	 *            conditions under which to take action
	 * @param action
	 *            state setting to change on target
	 * @param map
	 *            control id to control
	 */
	public ControlStateListener(Control target, ControlStateRuleType rule, Action action, Map<String, Control> map) {
		this.target = target;
		this.action = action;
		this.rule = new ControlStateRule(rule, map, this);
	}

	/*
	 * Upon receipt of the event, calls {@link #setState()} (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.swt.widgets.Listener#handleEvent(org.eclipse.swt.widgets.
	 * Event)
	 */
	public void handleEvent(Event event) {
		setState();
	}

	/**
	 * State of the controls in the rule is reevaluated and the state of the
	 * target is set accordingly.
	 */
	public void setState() {
		if (rule.evaluate()) {
			switch (action) {
			case ENABLE:
				target.setEnabled(true);
				break;
			case DISABLE:
				target.setEnabled(false);
				break;
			case SHOW:
				target.setVisible(true);
				break;
			case HIDE:
				target.setVisible(false);
				break;
			case SELECT:
				if (target instanceof Button) {
					((Button) target).setSelection(true);
				}
				break;
			case DESELECT:
				if (target instanceof Button) {
					((Button) target).setSelection(false);
				}
				break;
			}
		} else {
			switch (action) {
			case ENABLE:
				target.setEnabled(false);
				break;
			case DISABLE:
				target.setEnabled(true);
				break;
			case SHOW:
				target.setVisible(false);
				break;
			case HIDE:
				target.setVisible(true);
				break;
			case SELECT:
				if (target instanceof Button) {
					((Button) target).setSelection(false);
				}
				break;
			case DESELECT:
				if (target instanceof Button) {
					((Button) target).setSelection(true);
				}
				break;
			}
		}
	}
}
