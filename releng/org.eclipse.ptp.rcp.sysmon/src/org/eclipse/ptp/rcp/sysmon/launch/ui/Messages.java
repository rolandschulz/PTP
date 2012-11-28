package org.eclipse.ptp.rcp.sysmon.launch.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rcp.sysmon.launch.ui.messages"; //$NON-NLS-1$
	public static String SysMonApplicationTab_Application;
	public static String SysMonApplicationTab_Application_program;
	public static String SysMonApplicationTab_Application_program_not_specified;
	public static String SysMonApplicationTab_Browse;
	public static String SysMonApplicationTab_Cannot_read_configuration;
	public static String SysMonApplicationTab_Display_output_in_console;
	public static String SysMonApplicationTab_Please_specify_remote_connection_first;
	public static String SysMonApplicationTab_Select_application_to_execute;
	public static String SysMonApplicationTab_Unable_to_open_connection;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
