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
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.ControlStateRuleUtil;
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

	private static final boolean DEBUG = false;

	public enum Action {
		ENABLE, DISABLE, SHOW, HIDE, NONE;
	};

	private final Control target;
	private final ControlStateRuleType rule;
	private final Action action;
	private final Map<String, Button> map;
	private final IVariableMap varMap;

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

		if (DEBUG)
			System.out.println("Enter ControlStateListener.ControlStateListener target=" + target + " rule=" + rule + " action=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ action + " map=" + map + " varMap=" + varMap); //$NON-NLS-1$ //$NON-NLS-2$

		this.target = target;
		this.rule = rule;
		this.action = action;
		this.map = map;
		this.varMap = varMap;
		Set<Button> buttonSources = new HashSet<Button>();
		ControlStateRuleUtil.addSources(rule, map, buttonSources);
		for (Button button : buttonSources) {
			button.addSelectionListener(this);
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateListener.ControlStateListener"); //$NON-NLS-1$

	}

	/**
	 * Checks for cyclical dependencies by looking at the targets getting their listeners. The check is only done for listeners
	 * associated with buttons as targets.
	 */
	public void findCyclicalDependencies(Set<Button> buttons) throws CoreException {

		if (DEBUG)
			System.out.println("Enter ControlStateListener.findCyclicalDependencies buttons=" + buttons); //$NON-NLS-1$

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
						((ControlStateListener) swtListener).findCyclicalDependencies(buttons);
					}
				}
			}
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateListener.findCyclicalDependencies"); //$NON-NLS-1$

	}

	/**
	 * State of the controls in the rule is reevaluated and the state of the target is set accordingly.
	 */
	public void setState() {

		if (DEBUG)
			System.out.println("Enter ControlStateListener.setState"); //$NON-NLS-1$
		if (DEBUG)
			System.out.println("In ControlStateListener.setState target=" + target + " rule=" + rule + " action=" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ action + " map=" + map + " varMap=" + varMap); //$NON-NLS-1$ //$NON-NLS-2$

		synchronized (ControlStateListener.class) {
			if (ControlStateRuleUtil.evaluate(rule, map, varMap)) {
				switch (action) {
				case ENABLE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setEnabled(true)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setEnabled(true);
					break;
				case DISABLE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setEnabled(false)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setEnabled(false);
					break;
				case SHOW:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setVisible(true)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setVisible(true);
					/*
					 * Showing means that the control's area is used, that is,
					 * it is included by the layout manager
					 */
					if (target.getLayoutData() instanceof GridData) {
						((GridData) target.getLayoutData()).exclude = false;
					}
					break;
				case HIDE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setVisible(false)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setVisible(false);
					/*
					 * Hiding means that the control's area is not used, that is,
					 * it is excluded by the layout manager
					 */
					if (target.getLayoutData() instanceof GridData) {
						((GridData) target.getLayoutData()).exclude = true;
					}
					break;
				default:
					break;
				}
			} else {
				switch (action) {
				case ENABLE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setEnabled(false)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setEnabled(false);
					break;
				case DISABLE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setEnabled(true)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setEnabled(true);
					break;
				case SHOW:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setVisible(false)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setVisible(false);
					/*
					 * Hiding means that the control's area is not used, that is,
					 * it is excluded by the layout manager
					 */
					if (target.getLayoutData() instanceof GridData) {
						((GridData) target.getLayoutData()).exclude = true;
					}
					break;
				case HIDE:

					if (DEBUG)
						System.out.println("In ControlStateListener.setState target=" + target + " setVisible(true)"); //$NON-NLS-1$ //$NON-NLS-2$

					target.setVisible(true);
					/*
					 * Showing means that the control's area is used, that is,
					 * it is included by the layout manager
					 */
					if (target.getLayoutData() instanceof GridData) {
						((GridData) target.getLayoutData()).exclude = false;
					}
					break;
				default:
					break;
				}
			}
		}

		if (DEBUG)
			System.out.println("Exit1 ControlStateListener.setState"); //$NON-NLS-1$

	}

	/*
	 * Upon receipt of the event, calls {@link #setState()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {

		if (DEBUG)
			System.out.println("Enter ControlStateListener.widgetDefaultSelected e=" + e); //$NON-NLS-1$

		setState();

		if (DEBUG)
			System.out.println("Exit1 ControlStateListener.widgetDefaultSelected"); //$NON-NLS-1$

	}

	/*
	 * Upon receipt of the event, calls {@link #setState()} (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {

		if (DEBUG)
			System.out.println("Enter ControlStateListener.widgetSelected e=" + e); //$NON-NLS-1$

		setState();

		if (DEBUG)
			System.out.println("Exit1 ControlStateListener.widgetSelected"); //$NON-NLS-1$

	}
}
