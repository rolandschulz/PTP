package org.eclipse.ptp.cell.utils.debug;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.debug.messages"; //$NON-NLS-1$
	public static String DebugPolicy_FailedLogStatus;
	public static String DebugPolicy_FailedOptionsFile;
	public static String DebugPolicy_InternalErrorMessage;
	public static String DebugPolicy_UnknownDebugOptions;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Private constructor to prevent instances of this class.
	}
}
