package org.eclipse.ptp.cell.environment.cellsimulator.core.common;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.cellsimulator.core.common.messages"; //$NON-NLS-1$
	public static String AbstractTargetControl_BootingBIOS;
	public static String AbstractTargetControl_CannotResumeSimulator;
	public static String AbstractTargetControl_CleaningUp;
	public static String AbstractTargetControl_ConfiguringOS;
	public static String AbstractTargetControl_ConnectingLinuxConsole;
	public static String AbstractTargetControl_ConnectingThroughSSH;
	public static String AbstractTargetControl_ConnectionCanceled;
	public static String AbstractTargetControl_ConnectionFailed;
	public static String AbstractTargetControl_CreatingMachine;
	public static String AbstractTargetControl_Failed;
	public static String AbstractTargetControl_InvalidParameter;
	public static String AbstractTargetControl_KillingSystemProcesses;
	public static String AbstractTargetControl_LinuxConsole;
	public static String AbstractTargetControl_LinuxConsoleMessage;
	public static String AbstractTargetControl_LoadingKernel;
	public static String AbstractTargetControl_LoadingOS;
	public static String AbstractTargetControl_ParsingLaunchScript;
	public static String AbstractTargetControl_PreparingSimulatorPlugin;
	public static String AbstractTargetControl_PreparingToDeploy;
	public static String AbstractTargetControl_ProblemLaunchingSimulator;
	public static String AbstractTargetControl_ProblemWithSimulator;
	public static String AbstractTargetControl_ProcessConsole;
	public static String AbstractTargetControl_SimulatorCanceled;
	public static String AbstractTargetControl_SimulatorExited;
	public static String AbstractTargetControl_SimulatorLaunchFailed;
	public static String AbstractTargetControl_SimulatorPauseFailed;
	public static String AbstractTargetControl_SimulatorReadyToBoot;
	public static String AbstractTargetControl_SimulatorResumeFailed;
	public static String AbstractTargetControl_SimulatorRunningQuestion;
	public static String AbstractTargetControl_SimulatorShutDownError;
	public static String AbstractTargetControl_StartingSimulator;
	public static String AbstractTargetControl_StartingVirtualNetwork;
	public static String AbstractTargetControl_TCLConsoleMessage;
	public static String AbstractTargetControl_TryingAgain;
	public static String AbstractTargetControl_UnhandledException;
	public static String AbstractTargetControl_ValidatingParameters;
	public static String CommonConfigFactory_ReadTCLScriptFailed;
	public static String CommonConfigFactory_MemorySize;
	public static String CommonConfigFactory_InvalidPersistenceID;
	public static String CommonConfigFactory_CannotBeEmpty;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
