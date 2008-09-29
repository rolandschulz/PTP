package org.eclipse.ptp.cell.managedbuilder.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.managedbuilder.core.messages"; //$NON-NLS-1$

	public static String PDTCompilerBuildFlagsWarningDialogMessage;
	
	public static String PDTLinkerBuildFlagsWarningDialogMessage;

	public static String PDTBuildFlagsWarningDialogTitle;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Hide constructor to prevent instances.
	}
}
