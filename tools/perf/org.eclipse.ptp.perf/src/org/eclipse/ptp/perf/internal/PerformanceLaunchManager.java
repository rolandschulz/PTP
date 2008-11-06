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
package org.eclipse.ptp.perf.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.perf.Activator;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.toolopts.BuildTool;
import org.eclipse.ptp.perf.toolopts.ExecTool;
import org.eclipse.ptp.perf.toolopts.PerformanceProcess;
import org.eclipse.ptp.perf.toolopts.PerformanceTool;
import org.eclipse.ptp.perf.toolopts.PostProcTool;

public class PerformanceLaunchManager {
	
	protected static final String buildText="Instrumenting and Building";
	protected static final String launchText="Executing Instrumented Project";
	protected static final String collectText="Collecting Performance Data";
	
	protected String appNameAttribute;
	protected String projNameAttribute;
	protected String appPathAttribute=null;
	protected LaunchConfigurationDelegate paraDel;
	
	public PerformanceLaunchManager(LaunchConfigurationDelegate delegate, String appNameAtt,String projNameAtt){
		paraDel=delegate;
		appNameAttribute=appNameAtt;
		projNameAttribute=projNameAtt;
	}
	
	public PerformanceLaunchManager(LaunchConfigurationDelegate delegate, String appNameAtt,String projNameAtt, String appPathAtt){
		paraDel=delegate;
		appNameAttribute=appNameAtt;
		appPathAttribute=appPathAtt;
		projNameAttribute=projNameAtt;
	}
	
	private static boolean runStep(PerfStep step){
		step.schedule();
		try {
			step.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		return step.getResult().isOK();
	}
	
	private static boolean canRun(ILaunchConfiguration configuration, String toolReq) throws CoreException{
		
		if(toolReq==null){
			return true;
		}
		
		return configuration.getAttribute(toolReq, false);
	
	}
	
	/**
	 * The primary launch command of this launch configuration delegate.  The operations in this function are divided into
	 * three jobs:  Buildig, Running and Data collection
	 * @throws InterruptedException 
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException
	{
		final ILaunch launch = launchIn;
		
		PerformanceProcess pproc = Activator.getTool(configuration.getAttribute(IPerformanceLaunchConfigurationConstants.SELECTED_TOOL, (String)null));
		String bProgPath=null;
		String bOutLoc=null;
		//boolean built=false;
		boolean ran=false;
		PerfBuilder builder = null;
		PerfLauncher launcher=null;
		
		boolean buildOnly=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.BUILDONLY, false);
		boolean analyzeOnly=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.ANALYZEONLY, false);
		
		if(buildOnly){
			builder = new PerfBuilder(configuration, pproc.getFirstBuilder(), projNameAttribute,appPathAttribute);
			runStep(builder);
			return;
		}
		
		if(analyzeOnly){
			//String lookdir="somedir";
			PerfPostlaunch analyzer=new PerfPostlaunch(configuration,pproc.getFirstAnalyzer(),projNameAttribute,null);
			runStep(analyzer);
			return;
		}
		
		
		if(!pproc.recompile){
			builder = new PerfBuilder(configuration, null, projNameAttribute,appPathAttribute);
			if(!runStep(builder)){
				return;
			}
			bProgPath=builder.getProgramPath();
			bOutLoc=builder.getOutputLocation();
			//built=true;
			if(!pproc.prependExecution&&!(pproc.perfTools.get(0) instanceof ExecTool)){
				launcher = new PerfLauncher(configuration,null,appNameAttribute ,projNameAttribute, appPathAttribute,
						bProgPath,paraDel,launch);
				
				if(!runStep(launcher)){
					return;
				}
				ran=true;
			}
		}

		
		for(int i=0;i<pproc.perfTools.size();i++){//PerformanceTool t : pproc.perfTools){
			PerformanceTool t = pproc.perfTools.get(i);
			
			if(!canRun(configuration,t.requireTrue))
			{
				continue;
			}
			
			if(t instanceof BuildTool)
			{
				/**
				 * Uses the specified tool's build settings on the build manager for this project, 
				 * producing a new build configuration and instrumented binary file
				 */
				builder = new PerfBuilder(configuration, (BuildTool)t, projNameAttribute,appPathAttribute);
				if(!runStep(builder)){
					return;
				}
				bProgPath=builder.getProgramPath();
				bOutLoc=builder.getOutputLocation();
				//built=true;
				
				if(!pproc.prependExecution&&!ran&&i<pproc.perfTools.size()-1&&!(pproc.perfTools.get(i+1) instanceof ExecTool)){
					launcher = new PerfLauncher(configuration,null,appNameAttribute ,projNameAttribute, appPathAttribute,
							bProgPath,paraDel,launch);
					
					if(!runStep(launcher)){
						return;
					}
					ran=true;
				}
			}
			else if(t instanceof ExecTool){
				/**
				 * Execute the program specified in the build step
				 */
				launcher = new PerfLauncher(configuration,(ExecTool)t,appNameAttribute ,projNameAttribute, appPathAttribute,
						bProgPath,paraDel,launch);
				
				if(!runStep(launcher)){
					return;
				}
			}
			else if(t instanceof PostProcTool){
				/**
				 * Collect performance data from the execution handled in the run step
				 */
				final PerfPostlaunch analyzer=new PerfPostlaunch(configuration,(PostProcTool)t,projNameAttribute,bOutLoc);
				
				if(!runStep(analyzer)){
					return;
				}
			}
		}
	}
}
