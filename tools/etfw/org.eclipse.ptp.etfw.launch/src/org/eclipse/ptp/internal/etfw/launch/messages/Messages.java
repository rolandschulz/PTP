package org.eclipse.ptp.internal.etfw.launch.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.etfw.launch.messages.messages"; //$NON-NLS-1$
	public static String ETFWToolTabBuilder_ToggleShowHideSelectedAttributes;
	public static String ETFWVariableResolver_DE_REF_ERROR;
	public static String PerformanceAnalysisTab_BuildInstrumentedExecutable;
	public static String PerformanceAnalysisTab_NoWorkflowSelected;
	public static String PerformanceAnalysisTab_NoWorkflowTypeSelected;
	public static String PerformanceAnalysisTab_PleaseSelectWorkflow;
	public static String PerformanceAnalysisTab_PleaseSelectWorkflowType;
	public static String PerformanceAnalysisTab_SelectExistingPerfData;
	public static String PerformanceAnalysisTab_SelectTool;
	public static String PerformanceAnalysisTab_Tab_Name;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
