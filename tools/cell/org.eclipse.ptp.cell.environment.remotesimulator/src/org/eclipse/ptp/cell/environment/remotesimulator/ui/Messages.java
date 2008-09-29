package org.eclipse.ptp.cell.environment.remotesimulator.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.remotesimulator.ui.messages"; //$NON-NLS-1$
	public static String ConfigurationPage_Automatic;
	public static String ConfigurationPage_BaseDir;
	public static String ConfigurationPage_CellApplicationLaunch;
	public static String ConfigurationPage_Custom;
	public static String ConfigurationPage_RemoteCellSimulatorConnectionProperties;
	public static String ConfigurationPage_RemoteCellSimulatorLabel;
	public static String ConfigurationPage_RemoteHostInfo;
	public static String ConfigurationPage_RemoteHostToSimulatorConnection;
	public static String ConfigurationPage_Set;
	public static String ConfigurationPage_TargetNameLabel;
	public static String SimulatorConfigDialog_RemoteSimulatorCustomConfigTitle;
	public static String SimulatorConfigDialog_Test;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
