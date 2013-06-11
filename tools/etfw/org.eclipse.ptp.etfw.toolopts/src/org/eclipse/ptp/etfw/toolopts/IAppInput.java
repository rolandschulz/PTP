package org.eclipse.ptp.etfw.toolopts;

import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * This is the interface for an object that returns one or more elements of a command passed to a tool being executed
 * 
 * @author wspear
 * 
 */
public interface IAppInput {
	/**
	 * Returns the argument(s) contained by this object, building them from information in the configuration if necessary
	 * 
	 * @param configuration
	 * @return
	 */
	public String getArgument(ILaunchConfiguration configuration);

	/**
	 * @param configuration
	 * @return The map of environment variables defined for this tool's launch
	 */
	public Map<String, String> getEnvVars(ILaunchConfiguration configuration);
}
