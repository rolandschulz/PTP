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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.internal.rm.lml.monitor.ui.LMLMonitorUIPlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.jaxb.control.core.LaunchControllerManager;
import org.eclipse.ptp.rm.jaxb.core.data.ControlType;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.lml.core.JobStatusData;
import org.eclipse.ptp.rm.lml.internal.core.model.Row;

public class JobPropertyTester extends PropertyTester {
	private static final String OUTPUT_READY = "outputReady"; //$NON-NLS-1$
	private static final String ERROR_READY = "errorReady"; //$NON-NLS-1$
	private static final String OPERATION_SUPPORTED = "operationSupported"; //$NON-NLS-1$
	private static final String JOB_STATE = "jobState"; //$NON-NLS-1$
	private static final String JOB_STATE_DETAIL = "jobStateDetail"; //$NON-NLS-1$
	private static final String AUTHORIZED = "authorized"; //$NON-NLS-1$

	private static final String JOB_STATUS_CMD = "GET_JOB_STATUS";//$NON-NLS-1$

	/**
	 * @param status
	 * @return
	 */
	protected boolean isAuthorised(JobStatusData status) {
		if (status.getRemoteId() == null || status.getConnectionName() == null) {
			return false;
		}
		IRemoteServices services = PTPRemoteCorePlugin.getDefault().getRemoteServices(status.getRemoteId());
		if (!services.isInitialized()) {
			return false;
		}
		IRemoteConnection connection = services.getConnectionManager().getConnection(status.getConnectionName());
		if (connection == null || !connection.getUsername().equals(status.getOwner())) {
			return false;
		}
		return true;
	}

	protected boolean operationSupported(JobStatusData status, String operation) {
		try {
			ILaunchController jobController = LaunchControllerManager.getInstance().getLaunchController(status.getRemoteId(),
					status.getConnectionName(), status.getConfigurationName());
			if (jobController != null) {
				ResourceManagerData data = jobController.getConfiguration();
				if (data != null) {
					ControlType control = data.getControlData();
					if (operation.equals(JOB_STATUS_CMD)) {
						return control.getGetJobStatus() != null;
					}
					if (operation.equals(IJobControl.HOLD_OPERATION)) {
						return control.getHoldJob() != null;
					}
					if (operation.equals(IJobControl.RELEASE_OPERATION)) {
						return control.getReleaseJob() != null;
					}
					if (operation.equals(IJobControl.RESUME_OPERATION)) {
						return control.getResumeJob() != null;
					}
					if (operation.equals(IJobControl.SUSPEND_OPERATION)) {
						return control.getSuspendJob() != null;
					}
					if (operation.equals(IJobControl.TERMINATE_OPERATION)) {
						return control.getTerminateJob() != null;
					}
				}
			}
		} catch (CoreException e) {
			LMLMonitorUIPlugin.log(e.getStatus());
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object, java.lang.String, java.lang.Object[],
	 * java.lang.Object)
	 */
	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if (receiver instanceof Row) {
			Row row = (Row) receiver;
			JobStatusData status = row.status;
			if (OUTPUT_READY.equals(property)) {
				return status.getOutReady() == toBoolean(expectedValue);
			} else if (ERROR_READY.equals(property)) {
				return status.getErrReady() == toBoolean(expectedValue);
			} else if (OPERATION_SUPPORTED.equals(property)) {
				return operationSupported(status, toString(expectedValue));
			} else if (JOB_STATE.equals(property)) {
				return status.getState().equals(toString(expectedValue));
			} else if (JOB_STATE_DETAIL.equals(property)) {
				return status.getStateDetail().equals(toString(expectedValue));
			} else if (AUTHORIZED.equals(property)) {
				return isAuthorised(status) == toBoolean(expectedValue);
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

	/**
	 * Converts the given expected value to a <code>String</code>.
	 * 
	 * @param expectedValue
	 *            the expected value (may be <code>null</code>).
	 * @return the empty string if the expected value is <code>null</code>,
	 *         otherwise the <code>toString()</code> representation of the
	 *         expected value
	 */
	protected String toString(Object expectedValue) {
		return expectedValue == null ? "" : expectedValue.toString(); //$NON-NLS-1$
	}
}
