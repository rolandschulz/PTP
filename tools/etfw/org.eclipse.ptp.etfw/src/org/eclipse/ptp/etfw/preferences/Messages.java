package org.eclipse.ptp.etfw.preferences;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.preferences.messages"; //$NON-NLS-1$
	public static String ExternalToolPreferencePage_Add;
	public static String ExternalToolPreferencePage_ExToolConf;
	public static String ExternalToolPreferencePage_Remove;
	public static String ExternalToolPreferencePage_SelectToolDefXML;
	public static String ExternalToolPreferencePage_ToolDefFile;
	public static String ToolLocationPreferencePage_BinDir;
	public static String ToolLocationPreferencePage_BinDirectory;
	public static String ToolLocationPreferencePage_Browse;
	public static String ToolLocationPreferencePage_Select;
	public static String ToolLocationPreferencePage_ToolLocationConf;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
