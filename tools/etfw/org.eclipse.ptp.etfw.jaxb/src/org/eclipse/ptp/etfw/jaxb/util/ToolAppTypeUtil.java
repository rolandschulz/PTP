/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.jaxb.util;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.jaxb.data.ToolArgumentType;
import org.eclipse.ptp.etfw.jaxb.data.ToolPaneType;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBCoreConstants;

/**
 * Utility methods for getting tool app arguments and environment variables.
 * 
 * @author "Chris Navarro"
 * 
 */
public class ToolAppTypeUtil {

	/**
	 * Builds and concatenates all arguments from the defined sources (toolArgument and toolPane objects)
	 */
	public static List<String> getArguments(ILaunchConfiguration configuration, List<ToolPaneType> toolPanes,
			List<ToolArgumentType> toolArgs)
			throws CoreException
	{
		List<String> input = new ArrayList<String>();

		if (toolArgs != null) {
			for (ToolArgumentType toolArg : toolArgs) {
				String nextArg = ToolArgumentTypeUtil.getArgument(configuration, toolArg);// toolArg.getArgument(configuration);
				if (nextArg != null) {
					nextArg = nextArg.trim();

					if (nextArg.length() > 0)
					{
						int space = nextArg.indexOf(' ');
						if (nextArg.indexOf('-') == 0 && space > 0) {
							input.add(nextArg.substring(0, space).trim());
							nextArg = nextArg.substring(space).trim();
						}

						input.add(nextArg);
					}
				}
			}
		}

		if (toolPanes != null) {
			for (ToolPaneType toolPane : toolPanes) {
				String arg = JAXBCoreConstants.ZEROSTR;
				if (toolPane.getPrependWith() != null) {
					arg += toolPane.getPrependWith();
				}

				if (toolPane.getEncloseWith() != null) {
					arg += toolPane.getEncloseWith();
				}
				// if (toolPane.isVirtual()) {
				// continue;
				// }
				// for (int i = 0; i < allInput.length; i++) {
				// if (allInput[i] != null) {
				String nextArg = ToolPaneTypeUtil.getArgument(configuration, toolPane.getConfigId());// toolPane.getArgument(configuration);
				if (nextArg != null) {
					nextArg = nextArg.trim();
					arg += nextArg;
					if (toolPane.getEncloseWith() != null) {
						arg += toolPane.getEncloseWith();
					}

					if (arg.length() > 0)
					{
						int space = arg.indexOf(' ');
						if (arg.indexOf('-') == 0 && space > 0) {
							input.add(arg.substring(0, space).trim());
							arg = arg.substring(space).trim();
						}

						input.add(arg);
					}
					// input+=nextArg;//allInput[i].getArgument(configuration);
					// input+=" ";
				}

				// }
				// }
			}
		}
		return input;// getArgs(configuration)+" "+getPaneArgs(configuration);
	}

	/**
	 * Builds and concatenates all envVars from the defined sources (toolArgument and toolPane objects)
	 */
	public static Map<String, String> getEnvVars(ILaunchConfiguration configuration, List<ToolPaneType> toolPanes,
			List<ToolArgumentType> toolArgs) throws CoreException
	{
		LinkedHashMap<String, String> vars = new LinkedHashMap<String, String>();

		if (toolPanes != null)
			for (ToolPaneType toolPane : toolPanes) {
				// if (toolPane.isVirtual()) {
				// continue;
				// }
				if (toolPane != null) {
					Map<String, String> test = ToolPaneTypeUtil.getEnvVars(configuration, toolPane.getConfigVarId());
					if (test != null)
						vars.putAll(test);
				}
			}
		return vars;// getArgs(configuration)+" "+getPaneArgs(configuration);
	}
}
