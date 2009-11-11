package org.eclipse.ptp.etfw.toolopts;

public class PostProcTool extends ExternalTool {
	/**
	 * The array of analysis commands to be invoked when execution is complete (in order of execution)
	 */
	public ToolApp[] analysisCommands=null;
	public boolean useDefaultLocation=false;
	public String forAllLike=null;
	public int depth=-1;
}
