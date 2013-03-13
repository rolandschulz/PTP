package org.eclipse.ptp.etfw.tau.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.ui.messages.messages"; //$NON-NLS-1$
	public static String PapiOptionDialog_InvalidPapiMakefile;
	public static String PapiOptionDialog_NoPapiDirInMakefile;
	public static String PapiOptionDialog_PapiDirNotFound;
	public static String PapiOptionDialog_PapiUtilsNotFound;
	public static String PapiOptionDialog_SelectPapiCounters;
	public static String PerformanceDatabaseCombo_NoDatabasesAvailable;
	public static String PerformanceDatabaseCombo_TauJarDialogTitle;
	public static String PerformanceDatabaseCombo_TauJarsNotFound;
	public static String TAUMakefileCombo_BuildingMakefileList;
	public static String TAUMakefileCombo_UpdatingMakefileList;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
