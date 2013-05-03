package org.eclipse.ptp.internal.rm.lml.monitor.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rm.lml.monitor.core.messages.messages"; //$NON-NLS-1$
	public static String LMLResourceManagerMonitor_LMLMonitorJob;
	public static String LMLResourceManagerMonitor_RMSelectionJob;
	public static String LMLResourceManagerMonitor_unableToOpenConnection;
	public static String MonitorControl_UnableToLocateConnection;
	public static String MonitorControl_UnableToLocateLaunchController;
	public static String MonitorControl_unableToOpenRemoteConnection;
	public static String MonitorControlManager_monitorAddedJobName;
	public static String MonitorControlManager_monitorRemovedJobName;
	public static String MonitorControlManager_monitorSelectionChangedJobName;
	public static String MonitorControlManager_monitorUpdatedJobName;
	public static String MonitorControlManager_monitoRefreshJobName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
