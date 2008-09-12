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
	
	/**
	 * The primary launch command of this launch configuration delegate.  The operations in this function are divided into
	 * three jobs:  Buildig, Running and Data collection
	 * @throws InterruptedException 
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException
	{
		final ILaunch launch = launchIn;
		
		
		/**
		 * Uses the specified tool's build settings on the build manager for this project, 
		 * producing a new build configuration and instrumented binary file
		 */
		final PerfBuilder builder = new PerfBuilder(configuration, projNameAttribute,appPathAttribute);
		
		builder.schedule();
		try {
			builder.join();
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		
		if(!builder.getResult().isOK()){
			return;
		}
		
		/**
		 * Execute the program specified in the build step
		 */
		final PerfLauncher launcher = new PerfLauncher(configuration,appNameAttribute ,projNameAttribute, appPathAttribute,
				builder.getProgramPath(),paraDel,launch);
		
		launcher.schedule();
		try {
			launcher.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		if(!launcher.getResult().isOK()){
			return;
		}
		
		
		/**
		 * Collect performance data from the execution handled in the run step
		 */
		final PerfPostlaunch analyzer=new PerfPostlaunch(configuration,projNameAttribute,builder.getOutputLocation());
		
		analyzer.schedule();
		
		//final PerformanceLaunchSteps tauManager = new PerformanceLaunchSteps(configuration,appNameAttribute ,projNameAttribute, appPathAttribute,null,null);//, bLTool
		
	
//		Job tauBuild=new Job(buildText){
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//					builder.buildIndstrumented(monitor);
//				} catch (Exception e) {
//					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Build Incomplete",e);
//				}
//				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Build Successful",null);
//			}
//		};
		
		
//		final Job tauRun=new Job(launchText)
//		{
//			protected IStatus run(IProgressMonitor monitor) {
//				try {
//				if(!tauManager.performLaunch(paraDel, launch, monitor))
//					return new Status(IStatus.WARNING,"com.ibm.jdg2e.concurrency",IStatus.WARNING,"Nothing to run",null);
//				} catch (Exception e) {
//						try {
//							tauManager.cleanup();
//						} catch (CoreException e1) {}
//					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Execution Error",e);
//				}
//				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Execution Complete",null);
//			}
//		};
		

//		final Job tauCollect=new Job(collectText){
//
//			protected IStatus run(IProgressMonitor monitor) {
//				try{
//					tauManager.postlaunch(monitor);
//				}catch(Exception e){
//					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Data Collection Error",e);
//				}
//				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Data Collected",null);
//			}
//		};

//		/**
//		 * Manages job execution order
//		 */
//		JobChangeAdapter tauChange = new JobChangeAdapter(){
//			public void done (IJobChangeEvent event){
//				if(event.getJob().getName().equals(buildText)&&event.getResult().isOK())
//				{
//					tauRun.schedule();
//				}
//				else
//				if(event.getJob().getName().equals(launchText)&&event.getResult().isOK())
//				{
//					tauCollect.schedule();
//				}
//			}
//		};
		
//		tauBuild.addJobChangeListener(tauChange);
//		tauRun.addJobChangeListener(tauChange);
//		tauBuild.schedule();
	}
}
