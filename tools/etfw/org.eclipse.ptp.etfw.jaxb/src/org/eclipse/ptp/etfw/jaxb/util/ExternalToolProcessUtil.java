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
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.etfw.jaxb.data.BuildToolType;
import org.eclipse.ptp.etfw.jaxb.data.EtfwToolProcessType;
import org.eclipse.ptp.etfw.jaxb.data.ExecToolType;
import org.eclipse.ptp.etfw.jaxb.data.PostProcToolType;

/**
 * This class provides utility methods for obtaining specific tools from a workflow as well as some methods that were part of the
 * original tools parent class for determining if the tool can run.
 * 
 * @author "Chris Navarro"
 * 
 */
public class ExternalToolProcessUtil {
	public static BuildToolType getBuildTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrPostProcToolOrBuildTool();
		List<BuildToolType> buildTools = new ArrayList<BuildToolType>();
		for (Object o : tools) {
			if (o instanceof BuildToolType) {
				BuildToolType tool = (BuildToolType) o;
				if ((configuration == null || canRun(tool, configuration))) {
					buildTools.add(tool);
					// return tool;
				}
			}
		}

		if (index < buildTools.size()) {
			return buildTools.get(index);
		}

		return null;
	}

	public static ExecToolType getExecTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrPostProcToolOrBuildTool();
		List<ExecToolType> execTools = new ArrayList<ExecToolType>();
		for (Object o : tools) {
			if (o instanceof ExecToolType) {
				ExecToolType tool = (ExecToolType) o;

				if ((configuration == null || canRun(tool, configuration))) {
					execTools.add(tool);
					// return tool;
				}
			}
		}

		if (index < execTools.size()) {
			return execTools.get(index);
		}

		return null;
	}

	public static PostProcToolType getPostProcTool(EtfwToolProcessType etfwTool, ILaunchConfiguration configuration, int index) {
		List<Object> tools = etfwTool.getExecToolOrPostProcToolOrBuildTool();
		List<PostProcToolType> postProcTools = new ArrayList<PostProcToolType>();
		for (Object o : tools) {
			if (o instanceof PostProcToolType) {
				PostProcToolType tool = (PostProcToolType) o;
				if ((configuration == null || canRun(tool, configuration))) {
					postProcTools.add(tool);
					// return tool;
				}
			}
		}

		if (index < postProcTools.size()) {
			return postProcTools.get(index);
		}

		return null;
	}

	public static boolean canRun(PostProcToolType tool, ILaunchConfiguration configuration) {
		if (tool.getRequireTrue() == null || configuration == null) {
			return true;
		}
		boolean res = false;
		try {
			res = configuration.getAttribute(tool.getRequireTrue(), false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static boolean canRun(ExecToolType tool, ILaunchConfiguration configuration) {
		if (tool.getRequireTrue() == null || configuration == null) {
			return true;
		}
		boolean res = false;
		try {
			res = configuration.getAttribute(tool.getRequireTrue(), false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return res;
	}

	public static boolean canRun(BuildToolType tool, ILaunchConfiguration configuration) {

		if (tool.getRequireTrue() == null || configuration == null) {
			return true;
		}
		boolean res = false;
		try {
			res = configuration.getAttribute(tool.getRequireTrue(), false);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return res;
	}
}
