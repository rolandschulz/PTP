package org.eclipse.ptp.etfw.toolopts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * @author wspear
 *Represents a single tool command and relevant settings to be used by the ETFw workflow
 */
public abstract class ExternalTool {
	public String toolID = null;
	public String toolName = null;
	public String toolType = null;
	public String requireTrue = null;
	public ToolApp global = null;

	/**
	 * Determines if this this external tool can be executed based on if the value of 'requireTrue' is set to true or false in the
	 * configuration.
	 * 
	 * @param configuration
	 * @return
	 */
	public boolean canRun(ILaunchConfiguration configuration) {

		if (requireTrue == null || configuration == null) {
			return true;
		}
		boolean res = false;
		try {
			res = configuration.getAttribute(requireTrue, false);
		} catch (final CoreException e) {
			e.printStackTrace();
		}
		return res;
	}
}
