package org.eclipse.ptp.cell.ui.progress;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.ui.progress.messages"; //$NON-NLS-1$
	public static String ProgressQueue_Canceling;
	public static String ProgressQueue_CancelStatusPolling;
	public static String ProgressQueue_Interrupted;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
