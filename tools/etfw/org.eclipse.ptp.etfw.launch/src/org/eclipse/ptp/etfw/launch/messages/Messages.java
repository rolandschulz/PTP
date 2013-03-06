package org.eclipse.ptp.etfw.launch.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.launch.messages.messages"; //$NON-NLS-1$
	public static String PerformanceAnalysisTab_BuildInstrumentedExecutable;
	public static String PerformanceAnalysisTab_SelectExistingPerfData;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
