package org.eclipse.ptp.etfw.tau.papitest;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.papitest.messages"; //$NON-NLS-1$
	public static String PAPISplash_Browse;
	public static String PAPISplash_NativeCounters;
	public static String PAPISplash_PapiBin;
	public static String PAPISplash_PresetCounters;
	public static String PAPISplash_SelectPapiBin;
	public static String TestPAPI_PapiCounters;
	public static String TestPAPI_SelectedPapiCounters;
	public static String TestPAPI_SelectPapiCounters;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
