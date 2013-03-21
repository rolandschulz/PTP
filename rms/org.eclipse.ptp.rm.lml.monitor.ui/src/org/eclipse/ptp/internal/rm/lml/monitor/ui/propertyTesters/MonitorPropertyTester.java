/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rm.lml.monitor.ui.propertyTesters;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.monitor.core.listeners.IMonitorChangedListener;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.services.IEvaluationService;

public class MonitorPropertyTester extends PropertyTester {
	private static final String NAMESPACE = "org.eclipse.ptp.rm.monitor"; //$NON-NLS-1$
	private static final String IS_ACTIVE = "isActive"; //$NON-NLS-1$

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
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				if (window != null) {
					IEvaluationService service = (IEvaluationService) window.getService(IEvaluationService.class);
					if (service != null) {
						service.requestEvaluation(NAMESPACE + "." + IS_ACTIVE); //$NON-NLS-1$
					}
				}
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
		if (receiver instanceof MonitorControl) {
			MonitorControl monitor = (MonitorControl) receiver;
			if (IS_ACTIVE.equals(property)) {
				return monitor.isActive() == toBoolean(expectedValue);
			}
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
