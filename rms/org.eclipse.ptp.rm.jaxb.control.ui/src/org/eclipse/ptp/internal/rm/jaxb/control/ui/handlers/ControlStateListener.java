/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.ControlStateRule;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateRuleType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TypedListener;

/**
 * The listener is constructed on the basis of an event rule; it is added to the buttons on which the composed rule depends for its
 * state.<br>
 * 
 * @author arossi
 * 
 */
public class ControlStateListener implements SelectionListener {

	public enum Action {
		ENABLE, DISABLE, SHOW, HIDE, NONE;
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
	public ControlStateListener(Control target, ControlStateRuleType rule, Action action, Map<String, Button> map,
			IVariableMap varMap) throws CoreException {
		this.target = target;
		this.action = action;
		Set<Button> sources = new HashSet<Button>();
		this.rule = new ControlStateRule(rule, map, sources, varMap);
		for (Button b : sources) {
			b.addSelectionListener(this);
		}
	}

	/**
	 * Checks for cyclical dependencies by looking at the targets getting their listeners. The check is only done for listeners
	 * associated with buttons as targets.
	 */
	public void findCyclicalDependecies(Set<Button> buttons) throws CoreException {
		if (target instanceof Button) {
			if (buttons.contains(target)) {
				throw CoreExceptionUtils.newException(NLS.bind(Messages.ControlStateListener_0, target) + buttons);
			}
			Button b = (Button) target;
			buttons.add(b);
			Listener[] listeners = b.getListeners(SWT.Selection);
			for (Listener listener : listeners) {
				if (listener instanceof TypedListener) {
					Object swtListener = ((TypedListener) listener).getEventListener();
					if (swtListener instanceof ControlStateListener) {
						((ControlStateListener) swtListener).findCyclicalDependecies(buttons);
					}
				}
			}
		}
	}

	/**
	 * State of the controls in the rule is reevaluated and the state of the target is set accordingly.
	 */
	public void setState() {
		synchronized (ControlStateListener.class) {
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
					/* Showing means that the control's area is used, that is, 
					 * it is included by the layout manager */
					if (target.getLayoutData() instanceof GridData) {
						((GridData)target.getLayoutData()).exclude = false;
					}
					break;
				case HIDE:
					target.setVisible(false);
					/* Hiding means that the control's area is not used, that is, 
					 * it is excluded by the layout manager */
					if (target.getLayoutData() instanceof GridData) {
						((GridData)target.getLayoutData()).exclude = true;
					}
					break;
				default:
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
					/* Hiding means that the control's area is not used, that is, 
					 * it is excluded by the layout manager */
					if (target.getLayoutData() instanceof GridData) {
						((GridData)target.getLayoutData()).exclude = true;
					}
					break;
				case HIDE:
					target.setVisible(true);
					/* Showing means that the control's area is used, that is, 
					 * it is included by the layout manager */
					if (target.getLayoutData() instanceof GridData) {
						((GridData)target.getLayoutData()).exclude = false;
					}
					break;
				default:
					break;
				}
			}
		}
	}

	/*
	 * Upon receipt of the event, calls {@link #setState()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		setState();
	}

	/*
	 * Upon receipt of the event, calls {@link #setState()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		setState();
	}
}
