package org.eclipse.ptp.internal.remote.server.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.remote.server.core.messages.messages"; //$NON-NLS-1$
	public static String AbstractRemoteServerRunner_serverRunningCancelToTerminate;
	public static String AbstractRemoteServerRunner_unableToLocatePayload;
	public static String AbstractRemoteServerRunner_cannotRunServerMissingRequirements;
	public static String AbstractRemoteServerRunner_serverFinishedWithExitCode;
	public static String AbstractRemoteServerRunner_launching;
	public static String AbstractRemoteServerRunner_serverRestartAborted;
	public static String AbstractRemoteServerRunner_unableToOpenConnection;
	public static String AbstractRemoteServerRunner_cannotRunUnpack;
	public static String AbstractRemoteServerRunner_runningValidate;
	public static String AbstractRemoteServerRunner_unpackingPayload;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
