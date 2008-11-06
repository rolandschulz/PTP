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
package org.eclipse.ptp.perf.toolopts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Encapsulates all relevant attributes of a performance toolchain
 * including compilers, analysis tools, runtime prefixes, 
 * arguments and TODO environment settings
 * @author wspear
 *
 */
public class PerformanceProcess {
	public String toolID=null;
	public String toolName=null;
	
	/**
	 * If true the program is recompiled with performance-analysis specific modifications
	 */
	public boolean recompile=false;
	
	/**
	 * If true the actual executable is an argument passed to one or more additional utilities
	 */
	public boolean prependExecution=false;
	
	
	
	//public String compilerPathFinder=null;
	public Map<String, String> groupApp=null;
	
	/**
	 * The ordered list of performance tools to be invoked in this process
	 */
	public List<PerformanceTool> perfTools=null; 
	
	public PerformanceProcess()
	{
		perfTools=new ArrayList<PerformanceTool>();
		groupApp=new HashMap<String, String>();
	}
	
	public BuildTool getFirstBuilder(){
		for(int i=0;i<perfTools.size();i++){
			if(perfTools.get(i) instanceof BuildTool){
				return (BuildTool) perfTools.get(i);
			}
		}
		return null;
	}
	
	public ExecTool getFirstRunner(){
		for(int i=0;i<perfTools.size();i++){
			if(perfTools.get(i) instanceof ExecTool){
				return (ExecTool) perfTools.get(i);
			}
		}
		return null;
	}
	
	public PostProcTool getFirstAnalyzer(){
		for(int i=0;i<perfTools.size();i++){
			if(perfTools.get(i) instanceof PostProcTool){
				return (PostProcTool) perfTools.get(i);
			}
		}
		return null;
	}
}
