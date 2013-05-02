package org.eclipse.ptp.etfw.toolopts;

public class ExecTool extends ExternalTool {

	/**
	 * If true the actual executable is an argument passed to one or more additional utilities
	 */
	public boolean prependExecution = false;

	/**
	 * If true the executable and arguments in the launch configuration are ignored and the utilities given are run instead.
	 * 
	 * @since 5.0
	 */
	public boolean replaceExecution = false;

	/**
	 * The array of individual tools to be passed the executable being analyzed (in nesting order)
	 */
	public ToolApp[] execUtils = null;
}
