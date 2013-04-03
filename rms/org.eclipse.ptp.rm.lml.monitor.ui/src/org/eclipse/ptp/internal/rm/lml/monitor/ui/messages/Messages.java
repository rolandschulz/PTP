/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.internal.rm.lml.monitor.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rm.lml.monitor.ui.messages.messages"; //$NON-NLS-1$

	public static String ActionUtils_Remove_Files;

	public static String AddMonitorDialog_Add_Monitor;
	public static String AddMonitorDialog_Choose_a_monitor_type;

	public static String AddMonitorDialog_Connection_Type;
	public static String AddMonitorDialog_Monitor_Type;
	public static String AddMonitorDialog_Please_select_a_monitor_type;
	public static String AddMonitorDialog_Select_new_monitor;
	public static String RemoveMonitorHandler_Are_you_sure_1;
	public static String RemoveMonitorHandler_Are_you_sure_2;
	public static String RemoveMonitorHandler_Remove_Monitor;
	public static String StartMonitorHandler_Start_Monitor;
	public static String StartMonitorHandler_Unable_to_start_monitor;
	public static String StopMonitorHandler_Stop_Monitors;
	public static String StopMonitorHandler_Unable_to_stop_monitor;
	public static String MonitorView_AreYouSure;
	public static String MonitorView_Status;
	public static String MonitorView_StopMonitor;
	public static String MonitorView_ConnectionName;
	public static String MonitorView_ConfigurationName;
	public static String MonitorView_ToggleMonitor;
	public static String RemoveJob_Cannot_undo;
	public static String RemoveJob_Permanently_remove_job_entry;
	public static String AbstractControlHandler_ControlJob;
	public static String AbstractControlHandler_Failed_to_cancel_job;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
