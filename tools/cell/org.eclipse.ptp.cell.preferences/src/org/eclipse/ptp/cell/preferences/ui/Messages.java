package org.eclipse.ptp.cell.preferences.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.preferences.ui.messages"; //$NON-NLS-1$
	public static String CellEnvironmentPreferecePageDescription;
	public static String CellIDEPreferencePageDescription;
	public static String SdkSysrootPath;
	public static String LabelDefaultName;
	public static String SPUTimingBinaryPreferenceLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
