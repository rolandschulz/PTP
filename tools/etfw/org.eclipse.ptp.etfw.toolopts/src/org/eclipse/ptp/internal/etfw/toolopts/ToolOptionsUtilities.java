/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.toolopts;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.toolopts.ToolsOptionsConstants;

/**
 * Utility methods related to tool options plugin
 * 
 * @author Beth Tibbitts
 * 
 */
public class ToolOptionsUtilities {
	/**
	 * Get a configuration attribute that was stored by the performance framework; may translate the given key
	 * for its own purposes, so this consolidates that translation so each client doesn't have to know how. <br>
	 * Note: perhaps this is too general, are there other suffixes for retrieving different things?
	 * 
	 * @param key
	 * @return
	 * @throws CoreException
	 */
	public static String getConfigAttribute(ILaunchConfiguration configuration, String paneTitle, String key, String defaultValue)
			throws CoreException {
		key = paneTitle + ToolsOptionsConstants.TOOL_PANE_ID_SUFFIX + key.toUpperCase()
				+ ToolsOptionsConstants.TOOL_CONFIG_ARGUMENT_SUFFIX;
		final String result = configuration.getAttribute(key, defaultValue);
		return result;
	}
}
