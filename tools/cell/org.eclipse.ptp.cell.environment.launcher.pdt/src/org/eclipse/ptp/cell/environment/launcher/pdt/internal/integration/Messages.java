package org.eclipse.ptp.cell.environment.launcher.pdt.internal.integration;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.launcher.pdt.internal.integration.messages"; //$NON-NLS-1$
	public static String PdtProfileIntegration_FinalizeApplication_NoTraceFileGenerated;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
