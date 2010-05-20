package org.eclipse.ptp.etfw.tau.perfdmf.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.perfdmf.views.messages"; //$NON-NLS-1$
	public static String PerfDMFView_Default;
	public static String PerfDMFView_LaunchParaProf;
	public static String PerfDMFView_OpenInParaProf;
	public static String PerfDMFView_Refresh;
	public static String PerfDMFView_RefreshData;
	public static String PerfDMFView_SelectDatabase;
	public static String PerfDMFView_SelectOtherDatabase;
	public static String PerfDMFView_UsingDatabase;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
