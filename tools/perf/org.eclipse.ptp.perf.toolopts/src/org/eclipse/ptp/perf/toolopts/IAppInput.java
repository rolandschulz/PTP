package org.eclipse.ptp.perf.toolopts;

import org.eclipse.debug.core.ILaunchConfiguration;
/**
 * This is the interface for an object that returns one or more elements of a command passed to a tool being executed
 * @author wspear
 *
 */
public interface IAppInput {
	/**
	 * Returns the argument(s) contained by this object, building them from information in the configuration if necessary
	 * @param configuration
	 * @return
	 */
	public String getArgument(ILaunchConfiguration configuration);
}
