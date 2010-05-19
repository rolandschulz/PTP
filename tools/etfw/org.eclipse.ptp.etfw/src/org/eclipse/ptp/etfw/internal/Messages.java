package org.eclipse.ptp.etfw.internal;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.internal.messages"; //$NON-NLS-1$
	public static String BuilderTool_all;
	public static String BuilderTool_BuildIncomplete;
	public static String BuilderTool_BuildSuccessful;
	public static String BuilderTool_InstrumentingBuilding;
	public static String BuilderTool_NoConfig;
	public static String BuilderTool_NoConfSelected;
	public static String BuilderTool_NoInfo;
	public static String BuilderTool_NoMakeTargetAll;
	public static String BuilderTool_NoManagedProject;
	public static String BuilderTool_SelConfHasNoName;
	public static String BuildLaunchUtils_BinDir;
	public static String BuildLaunchUtils_PleaseSelectDir;
	public static String BuildLaunchUtils_Select;
	public static String LauncherTool_ExecutionComplete;
	public static String LauncherTool_ExecutionError;
	public static String LauncherTool_NotFound;
	public static String LauncherTool_NothingToRun;
	public static String LauncherTool_RunningApplication;
	public static String LauncherTool_Tool;
	public static String ParametricToolLaunchManager_NoPerformanceAnalysisJobsConstructed;
	public static String ParametricToolLaunchManager_Op1;
	public static String ParametricToolLaunchManager_Op2;
	public static String ParametricToolLaunchManager_Op3;
	public static String ParametricToolLaunchManager_OpNone;
	public static String ParametricToolLaunchManager_OptimizationLevel;
	public static String PostlaunchTool_Analysis;
	public static String PostlaunchTool_CouldNotRun;
	public static String PostlaunchTool_DataCollected;
	public static String PostlaunchTool_DataCollectError;
	public static String PostlaunchTool_NoData;
	public static String PostlaunchTool_NoToolNoData;
	public static String PostlaunchTool_NoValidFiles;
	public static String PostlaunchTool_SelectPerfDir;
	public static String PostlaunchTool_TheCommand;
	public static String ToolLaunchManager_CollectingPerfData;
	public static String ToolLaunchManager_ExecutingInstrumentedProject;
	public static String ToolLaunchManager_InstrumentingAndBuilding;
	public static String ToolStep_Unknown;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
