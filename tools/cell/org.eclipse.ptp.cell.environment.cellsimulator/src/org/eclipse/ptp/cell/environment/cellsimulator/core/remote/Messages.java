package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.core.remote.messages"; //$NON-NLS-1$
	public static String RemoteSimulatorDelegate_CannotCheckPath;
	public static String RemoteSimulatorDelegate_CannotDeletePath;
	public static String RemoteSimulatorDelegate_CannotDownloadFile;
	public static String RemoteSimulatorDelegate_CannotReadSimulatorPID;
	public static String RemoteSimulatorDelegate_CannotReadSimulatorPIDInFile;
	public static String RemoteSimulatorDelegate_CannotSendSignalToSimulator;
	public static String RemoteSimulatorDelegate_CannotUploadFile;
	public static String RemoteSimulatorDelegate_CreateRemoteDirFailed;
	public static String RemoteSimulatorDelegate_ErrorCreatingProcess;
	public static String RemoteSimulatorDelegate_TargetHostConnectionFailed;
	public static String RemoteSimulatorDelegate_TargetHostNotConnected;
	public static String RemoteTargetControl_CleaningUp;
	public static String RemoteTargetControl_CouldNotConnectToRemoteHost;
	public static String RemoteTargetControl_CouldNotCreatePortForward;
	public static String RemoteTargetControl_CouldNotCreateSocket;
	public static String RemoteTargetControl_Disconnecting;
	public static String RemoteTargetControl_ForwardingRemoteConnection;
	public static String RemoteTargetControl_InvalidParameter;
	public static String RemoteTargetControl_LaunchCompleted;
	public static String RemoteTargetControl_LaunchLocalCellSimulator;
	public static String RemoteTargetControl_LocalCellSimulatorForceShutdown;
	public static String RemoteTargetControl_LocalCellSimulatorLaunchCancelation;
	public static String RemoteTargetControl_LocalCellSimulatorLaunchInterruption;
	public static String RemoteTargetControl_LocalCellSimulatorLaunchShutdownCancelation;
	public static String RemoteTargetControl_LocalCellSimulatorShutdown;
	public static String RemoteTargetControl_Preparing;
	public static String RemoteTargetControl_RemoteHostconnection;
	public static String RemoteTargetControl_RemoteSimulatorLaunchCancelled;
	public static String RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringConnection;
	public static String RemoteTargetControl_RemoteSimulatorLaunchCancelledDuringPortForwarding;
	public static String RemoteTargetControl_ShutDownCompleted;
	public static String RemoteTargetControl_ShuttingDown;
	public static String RemoteTargetControl_SimulatorNotLaunched;
	public static String RemoteTargetControl_UnexpectedErrorLaunchingSimulator;
	public static String RemoteSimulatorConfiguration_IncorrectAssociation;
	public static String RemoteSimulatorConfiguration_RemoteHostVerificationFailed;
	public static String RemoteSimulatorConfiguration_PathMustBeAbsolute;
	public static String RemoteSimulatorConfiguration_PathDoesNotExist;
	public static String RemoteSimulatorConfiguration_MustBeFile;
	public static String RemoteSimulatorConfiguration_MustBeDir;
	public static String RemoteSimulatorConfiguration_MustBeReadable;
	public static String RemoteSimulatorConfiguration_MustBeWritable;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
