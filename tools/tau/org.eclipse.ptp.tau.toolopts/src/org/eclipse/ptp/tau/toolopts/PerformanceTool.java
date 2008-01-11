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
package org.eclipse.ptp.tau.toolopts;

import java.util.HashMap;
import java.util.Map;


/**
 * Encapsulates all relevant attributes of a performance toolchain
 * including compilers, analysis tools, runtime prefixes, 
 * arguments and TODO environment settings
 * @author wspear
 *
 */
public class PerformanceTool {
	public String toolID=null;
	public String toolName=null;
	
	/**
	 * If true the program is recompiled with performance-analysis specific modifications
	 */
	public boolean recompile=false;
	
	/**
	 * If true the compiler command is entirely replaced rather than prepended
	 */
	public boolean replaceCompiler=false;

	
	protected ToolApp ccCompiler=null;
	protected ToolApp cxxCompiler=null;
	protected ToolApp f90Compiler=null;
	protected ToolApp allCompilers=null;
	
	//public String compilerPathFinder=null;
	public Map groupApp=null;
	
	/**
	 * If true the actual executable is an argument passed to one or more additional utilities
	 */
	public boolean prependExecution=false;
	
	/**
	 * The array of individual tools to be passed the executable being analyzed (in nesting order)
	 */
	public ToolApp[] execUtils=null;
	
	/**
	 * The array of analysis commadnds to be invoked when execution is complete (in order of execution)
	 */
	public ToolApp[] analysisCommands=null;
	
	public PerformanceTool()
	{
		groupApp=new HashMap();
	}
	
	
	public ToolApp getCcCompiler(){
		if(ccCompiler!=null)
			return ccCompiler;
		return allCompilers;
	}
	public ToolApp getCxxCompiler(){
		if(cxxCompiler!=null)
			return cxxCompiler;
		return allCompilers;
	}
	public ToolApp getF90Compiler(){
		if(ccCompiler!=null)
			return f90Compiler;
		return allCompilers;
	}
	public ToolApp getGlobalCompiler(){
		return allCompilers;
	}
	
//	/**
//	 * Returns the list of arguments associated with all compilers
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	private String getAllCompArgs(ILaunchConfiguration configuration) throws CoreException
//	{
//		String args="";
//		if(allCompilers!=null)
//		{
//			args+=allCompilers.getArguments(configuration);//  allCompilers.getArgs()+allCompilers.getPaneArgs(configuration);
//		}
//		return args;
//	}
//	
//	/**
//	 * Returns the full command for this tool's cc compiler, including arguments
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	public String getCcCommand(ILaunchConfiguration configuration) throws CoreException
//	{
//		return ccCompiler.getCommand(configuration)+" "+getAllCompArgs(configuration);
//	}
//	
//	/**
//	 * Returns the full command for this tool's cxx compiler, including arguments
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	public String getCxxCommand(ILaunchConfiguration configuration) throws CoreException
//	{
//		return cxxCompiler.getCommand(configuration)+" "+getAllCompArgs(configuration);
//	}
//	
//	/**
//	 * Returns the full command for this tool's f90 compiler, including arguments
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	public String getF90Command(ILaunchConfiguration configuration) throws CoreException
//	{
//		return f90Compiler.getCommand(configuration)+" "+getAllCompArgs(configuration);
//	}
//	
//	/**
//	 * Returns the full command for all of this tool's compilers.  If compiler names are
//	 * different for the various types it may contain only arguments that apply to each
//	 * @param configuration
//	 * @return
//	 * @throws CoreException
//	 */
//	public String getAllCompCommand(ILaunchConfiguration configuration) throws CoreException
//	{
//		return allCompilers.getCommand(configuration);
//	}
	
}
