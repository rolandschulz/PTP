package org.eclipse.ptp.cell.utils.terminal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.terminal.messages"; //$NON-NLS-1$
	public static String AbstractTerminalProvider_1;
	public static String AbstractTerminalProvider_2;
	public static String AbstractTerminalProvider_FailedDelegateMethod;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Private constructor to prevent instances of this class.
	}
}
