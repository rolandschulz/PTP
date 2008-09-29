package org.eclipse.ptp.cell.pdt.xml.preferences.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.pdt.xml.preferences.ui.messages"; //$NON-NLS-1$
	public static String PdtPreferencesPage_FieldEditor_EventGroupsDir;
	public static String PdtPreferencesPage_PageTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
