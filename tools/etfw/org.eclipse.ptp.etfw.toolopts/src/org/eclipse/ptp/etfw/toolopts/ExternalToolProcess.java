/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.etfw.toolopts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Encapsulates all relevant attributes of a performance toolchain including
 * compilers, analysis tools, runtime prefixes, arguments and TODO environment
 * settings
 * 
 * @author wspear
 */
public class ExternalToolProcess {

	/**
	 * Fields define the parameters that may be iterated over when running a parametric workflow
	 * @author wspear
	 *
	 */
	public static class Parametric {
		public boolean runParametric = false;
		public boolean weakScaling = false;
		public String mpiProcs = "1"; //$NON-NLS-1$
		public List<String> argWeakBools = new ArrayList<String>();
		public List<String> argNames = new ArrayList<String>();
		public List<String> argValues = new ArrayList<String>();

		public List<String> varWeakBools = new ArrayList<String>();
		public List<String> varNames = new ArrayList<String>();
		public List<String> varValues = new ArrayList<String>();
		public String compileropt;
	}

	public String toolID = null;
	public String toolName = null;

	/**
	 * If true there will be no automatic assumptions about when execute steps
	 * must be performed. Automatic execution will not take place after build
	 * steps!
	 */
	public boolean explicitExecution = false;

	/**
	 * If true the program is recompiled with performance-analysis specific
	 * modifications
	 */
	public boolean recompile = false;

	/**
	 * If true the actual executable is an argument passed to one or more
	 * additional utilities
	 */
	public boolean prependExecution = false;

	// public String compilerPathFinder=null;
	public Map<String, String> groupApp = null;

	/**
	 * The ordered list of performance tools to be invoked in this process
	 */
	public List<ExternalTool> externalTools = null;

	/**
	 * Parametric elements
	 */
	public Parametric para = null;

	/**
	 * End parametric
	 */
	public ExternalToolProcess() {
		externalTools = new ArrayList<ExternalTool>();
		groupApp = new HashMap<String, String>();
	}

	/**
	 * Returns the first analysis tool defined in this process
	 * @since 5.0
	 */
	public PostProcTool getFirstAnalyzer(ILaunchConfiguration configuration) {
		for (int i = 0; i < externalTools.size(); i++) {
			final ExternalTool pt = externalTools.get(i);
			if (pt instanceof PostProcTool && (configuration == null || pt.canRun(configuration))) {
				return (PostProcTool) externalTools.get(i);
			}
		}
		return null;
	}

	/**
	 * Returns the first build tool defined in this process
	 * @since 5.0
	 */
	public BuildTool getFirstBuilder(ILaunchConfiguration configuration) {
		for (int i = 0; i < externalTools.size(); i++) {
			final ExternalTool pt = externalTools.get(i);
			if (pt instanceof BuildTool && (configuration == null || pt.canRun(configuration))) {
				return (BuildTool) externalTools.get(i);
			}
		}
		return null;
	}

	/**
	 * Returns the first exec tool defined in this execution process
	 * @since 5.0
	 */
	public ExecTool getFirstRunner(ILaunchConfiguration configuration) {
		for (int i = 0; i < externalTools.size(); i++) {
			final ExternalTool pt = externalTools.get(i);
			if (pt instanceof ExecTool && (configuration == null || pt.canRun(configuration))) {
				return (ExecTool) externalTools.get(i);
			}
		}
		return null;
	}

	/**
	 * Returns the nth ExecTool defined in this process.
	 * 
	 * @param configuration
	 * @param n
	 * @return
	 * @since 5.0
	 */
	public ExecTool getNthRunner(ILaunchConfiguration configuration, int n) {
		if (n < 1) {
			n = 1;
		}
		int count = 0;
		for (int i = 0; i < externalTools.size(); i++) {
			final ExternalTool pt = externalTools.get(i);
			if (pt instanceof ExecTool && (configuration == null || pt.canRun(configuration))) {
				count++;
				if (count == n) {
					return (ExecTool) externalTools.get(i);
				}
			}
		}
		return null;
	}
}
