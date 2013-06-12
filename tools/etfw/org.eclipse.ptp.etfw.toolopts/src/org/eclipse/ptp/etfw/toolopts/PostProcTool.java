package org.eclipse.ptp.etfw.toolopts;

/**
 * @author wspear
 *Encapsulates the commands, arguments and settings for data processing/analysis operations to be used in an ETFw workflow
 */
public class PostProcTool extends ExternalTool {
	/**
	 * The array of analysis commands to be invoked when execution is complete
	 * (in order of execution)
	 */
	public ToolApp[] analysisCommands = null;
	public boolean useDefaultLocation = false;
	public String forAllLike = null;
	/**
	 * @since 4.0
	 */
	public boolean useLatestFileOnly = true;
	public int depth = -1;
}
