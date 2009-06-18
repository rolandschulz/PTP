package org.eclipse.ptp.internal.rdt.ui.scannerinfo;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.ui.scannerinfo.messages"; //$NON-NLS-1$
	public static String RemoteIncludeDialog_browse;
	public static String RemoteIncludeDialog_cancel;
	public static String RemoteIncludeDialog_configurations;
	public static String RemoteIncludeDialog_directory;
	public static String RemoteIncludeDialog_languages;
	public static String RemoteIncludeDialog_ok;
	public static String RemoteIncludeDialog_select;
	public static String RemoteIncludeTab_title;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
