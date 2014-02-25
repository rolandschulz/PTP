/*******************************************************************************
 * Copyright (c) 2013-2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 * Carsten Karbach, FZ Juelich
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.IMonitorUIConstants;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorChangedListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

public class MonitorPropertyTester extends PropertyTester {
	private static final String NAMESPACE = "org.eclipse.ptp.rm.monitor"; //$NON-NLS-1$
	private static final String IS_ACTIVE = "isActive"; //$NON-NLS-1$

	/**
	 * Id of the currently selected monitor instance or null, if none is selected.
	 */
	private String selectedMonitorId = null;

	/**
	 * Retrieve the evaluation service and trigger an update of the
	 * enablement option for all monitor commands.
	 */
	private void requestEvaluationOfProperty() {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			IEvaluationService service = (IEvaluationService) window.getService(IEvaluationService.class);
			if (service != null) {
				service.requestEvaluation(NAMESPACE + "." + IS_ACTIVE); //$NON-NLS-1$
			}
		}
	}

	public MonitorPropertyTester() {
		MonitorControlManager.getInstance().addMonitorChangedListener(new IMonitorChangedListener() {
			@Override
			public void monitorAdded(IMonitorControl[] monitors) {
				// Do nothing
			}

			@Override
			public void monitorRemoved(IMonitorControl[] monitors) {
				// Do nothing
			}

			@Override
			public void monitorUpdated(IMonitorControl[] monitors) {
				requestEvaluationOfProperty();
			}
		});

		MonitorControlManager.getInstance().addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				// Keep track of the currently selected monitor directly within the property tester
				selectedMonitorId = null;
				if (event.getSelection() instanceof IStructuredSelection) {
					IStructuredSelection sel = (IStructuredSelection) event.getSelection();
					if (!sel.isEmpty()) {
						selectedMonitorId = ((IMonitorControl) sel.getFirstElement()).getControlId();
					}
				}

				requestEvaluationOfProperty();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
	 * java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {

		IMonitorControl monitor = null;
		// Check if there is an active page at all before using it
		if (PlatformUI.getWorkbench() != null && PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null &&
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage() != null) {
			// Try to get a selection directly from the view as this is faster than depending on the listener
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			ISelection selection = page.getSelection(IMonitorUIConstants.ID_SYSTEM_MONITOR_VIEW);
			if (selection != null && selection instanceof IStructuredSelection) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (!sel.isEmpty()) {
					Object selected = sel.getFirstElement();
					if (selected instanceof IMonitorControl) {
						monitor = (IMonitorControl) selected;
					}
				}
			}
		}

		if (monitor == null) {// If monitor instance could not be gathered from the view
			if (receiver instanceof IMonitorControl) {
				monitor = (IMonitorControl) receiver;
			} else if (selectedMonitorId != null) {
				// Try to use the selected monitor, if the receiver is not directly a IMonitorControl instance
				monitor = MonitorControlManager.getInstance().getMonitorControl(selectedMonitorId);
			}
		}

		if (monitor != null && IS_ACTIVE.equals(property)) {
			return monitor.isActive() == toBoolean(expectedValue);
		}

		return false;
	}

	/**
	 * Converts the given expected value to a boolean.
	 * 
	 * @param expectedValue
	 *            the expected value (may be <code>null</code>).
	 * @return <code>false</code> if the expected value equals Boolean.FALSE, <code>true</code> otherwise
	 */
	protected boolean toBoolean(Object expectedValue) {
		if (expectedValue instanceof Boolean) {
			return ((Boolean) expectedValue).booleanValue();
		}
		return true;
	}
}
