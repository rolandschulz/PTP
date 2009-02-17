package org.eclipse.ptp.perf.toolopts;

public class PostProcTool extends PerformanceTool {
	/**
	 * The array of analysis commands to be invoked when execution is complete (in order of execution)
	 */
	public ToolApp[] analysisCommands=null;
	public boolean useDefaultLocation=false;
	public String forAllLike=null;
}
