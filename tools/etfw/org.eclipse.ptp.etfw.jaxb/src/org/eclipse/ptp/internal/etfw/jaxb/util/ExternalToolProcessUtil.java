/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.etfw.jaxb.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.internal.etfw.jaxb.data.AnalysisToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.internal.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.internal.etfw.jaxb.data.ExecToolType;

/**
 * This class provides utility methods for obtaining specific tools from a workflow as well as some methods that were part of the
 * original tools parent class for determining if the tool can run.
 * 
 * @author "Chris Navarro"
 * 
 */
public class ExternalToolProcessUtil {
	public static BuildToolType getBuildTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrAnalysisToolOrBuildTool();
		List<BuildToolType> buildTools = new ArrayList<BuildToolType>();
		for (Object o : tools) {
			if (o instanceof BuildToolType) {
				BuildToolType tool = (BuildToolType) o;
				if ((configuration == null || canRun(true, tool, configuration))) {
					buildTools.add(tool);
				}
			}
		}

		if (index < buildTools.size()) {
			return buildTools.get(index);
		}

		return null;
	}

	public static ExecToolType getExecTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrAnalysisToolOrBuildTool();
		List<ExecToolType> execTools = new ArrayList<ExecToolType>();
		for (Object o : tools) {
			if (o instanceof ExecToolType) {
				ExecToolType tool = (ExecToolType) o;

				if ((configuration == null || canRun(true, tool, configuration))) {
					execTools.add(tool);
				}
			}
		}

		if (index < execTools.size()) {
			return execTools.get(index);
		}

		return null;
	}

	public static AnalysisToolType getAnalysisTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrAnalysisToolOrBuildTool();
		List<AnalysisToolType> analysisTools = new ArrayList<AnalysisToolType>();
		for (Object o : tools) {
			if (o instanceof AnalysisToolType) {
				AnalysisToolType tool = (AnalysisToolType) o;
				if ((configuration == null || canRun(true, tool, configuration))) {
					analysisTools.add(tool);
				}
			}
		}

		if (index < analysisTools.size()) {
			return analysisTools.get(index);
		}

		return null;
	}
	
	public static boolean evaluate(ILaunchConfiguration configuration, String name) {
		if (name != null) {
			/*
			 * Check if there is a value in the launch configuration for this attribute, that
			 * is, if the attribute is or is not defined
			 */
			try {
				String value = configuration.getAttribute(name, (String) null);
				if (value != null) {
					/* Value is defined in the launch configuration, that is, the attribute is defined */
					return true;
				}
			} catch (CoreException e) {
				// Ignore
			}
			/* Value is not defined in the launch configuration or there was an exception */
			return false;
		}
		return true;
	}

	public static boolean canRun(boolean globalState, AnalysisToolType tool, ILaunchConfiguration configuration) {
		boolean result = true;
		if (result) result &= ExternalToolProcessUtil.evaluate(configuration, tool.getRequireTrue());
		if (result) result &= ToolStateUtil.evaluate(tool.getToolState(), configuration, globalState);
		return result;
	}

	public static boolean canRun(boolean globalState, ExecToolType tool, ILaunchConfiguration configuration) {
		boolean result = true;
		if (result) result &= ExternalToolProcessUtil.evaluate(configuration, tool.getRequireTrue());
		if (result) result &= ToolStateUtil.evaluate(tool.getToolState(), configuration, globalState);
		return result;
	}

	public static boolean canRun(boolean globalState, BuildToolType tool, ILaunchConfiguration configuration) {
		boolean result = true;
		if (result) result &= ExternalToolProcessUtil.evaluate(configuration, tool.getRequireTrue());
		if (result) result &= ToolStateUtil.evaluate(tool.getToolState(), configuration, globalState);
		return result;
	}
}
