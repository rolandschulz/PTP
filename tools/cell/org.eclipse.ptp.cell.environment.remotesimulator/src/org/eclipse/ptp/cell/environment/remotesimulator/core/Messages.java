package org.eclipse.ptp.cell.environment.remotesimulator.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.remotesimulator.core.messages"; //$NON-NLS-1$
	public static String TargetControl_CannotPauseRemoteSimulator;
	public static String TargetControl_CannotResumeRemoteSimulator;
	public static String TargetControl_CouldNotConnectToRemoteHost;
	public static String TargetControl_CouldNotConnectToSimulatorOnRemoteHost;
	public static String TargetControl_CouldNotCreateSocket;
	public static String TargetControl_RemoteHostConnection;
	public static String TargetControl_RemoteSimulatorConnection;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
