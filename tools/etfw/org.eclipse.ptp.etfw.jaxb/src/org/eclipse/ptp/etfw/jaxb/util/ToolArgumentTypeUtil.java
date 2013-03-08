package org.eclipse.ptp.etfw.jaxb.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.jaxb.data.ToolArgumentType;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Utility methods for obtaining tool argument environment variables and arguments.
 * 
 * TODO determine if this is relevant with the recent re-implementation of ETFw with JAXB and the control attributes.
 * 
 * @author "Chris Navarro"
 * 
 */
public class ToolArgumentTypeUtil {

	public static int ARG = 0;
	public static int VAR = 1;

	/**
	 * Builds and returns the argument from the elements defined in this object
	 */
	public static String getArgument(ILaunchConfiguration configuration, ToolArgumentType toolArg) {

		if (toolArg.getArgType() != ARG) {
			return null;
		}

		if (toolArg.isUseConfValue()) {
			String carg = toolArg.getValue();
			String cval = JAXBCoreConstants.ZEROSTR;
			try {
				cval = configuration.getAttribute(toolArg.getConfVal(), JAXBCoreConstants.ZEROSTR);
			} catch (CoreException e) {
				e.printStackTrace();
			}
			if (toolArg.isRequireValue() && cval.trim().length() <= 0)
				return JAXBCoreConstants.ZEROSTR;
			carg = carg.replace(ToolsOptionsConstants.CONF_VALUE, cval);
			return carg;
		} else {
			return toolArg.getValue();
		}
	}
}
