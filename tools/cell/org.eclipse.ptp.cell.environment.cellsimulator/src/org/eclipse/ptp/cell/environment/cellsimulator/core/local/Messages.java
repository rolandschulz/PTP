package org.eclipse.ptp.cell.environment.cellsimulator.core.local;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.core.local.messages"; //$NON-NLS-1$
	public static String LocalSimulatorDelegate_CouldNotRead;
	public static String LocalSimulatorDelegate_CouldNotRemove;
	public static String LocalSimulatorDelegate_CouldNotWrite;
	public static String LocalSimulatorDelegate_CreateLocalDirectoryFailed;
	public static String LocalSimulatorDelegate_CreatingProcessError;
	public static String LocalSimulatorDelegate_SpawingPOSIXProcessProblem;
	public static String LocalTargetControl_CleaningUp;
	public static String LocalTargetControl_LaunchCompleted;
	public static String LocalTargetControl_LaunchLocalCellSimulator;
	public static String LocalTargetControl_LocalCellSimulatorForceShutdown;
	public static String LocalTargetControl_LocalCellSimulatorLaunchCancelation;
	public static String LocalTargetControl_LocalCellSimulatorLaunchCancelled;
	public static String LocalTargetControl_LocalCellSimulatorLaunchInterruption;
	public static String LocalTargetControl_LocalCellSimulatorShutdown;
	public static String LocalTargetControl_LocalCellSimulatorShutdownCancelation;
	public static String LocalTargetControl_Preparing;
	public static String LocalTargetControl_ShutDownCompleted;
	public static String LocalTargetControl_ShuttingDown;
	public static String LocalTargetControl_SimulatorNotLaunched;
	public static String LocalTargetControl_UnexpectedErrorLaunchingSimulator;
	public static String LocalSimulatorConfiguration_PathMustBeAbsolute;
	public static String LocalSimulatorConfiguration_PathDoesNotExist;
	public static String LocalSimulatorConfiguration_MustBeFile;
	public static String LocalSimulatorConfiguration_MustBeDir;
	public static String LocalSimulatorConfiguration_MustBeReadable;
	public static String LocalSimulatorConfiguration_MustBeWritable;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
