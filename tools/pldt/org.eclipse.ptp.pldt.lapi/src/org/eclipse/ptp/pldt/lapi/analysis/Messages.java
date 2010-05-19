package org.eclipse.ptp.pldt.lapi.analysis;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.lapi.analysis.messages"; //$NON-NLS-1$
	public static String LapiCASTVisitor_lapi_call;
	public static String LapiCASTVisitor_lapi_constant;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
