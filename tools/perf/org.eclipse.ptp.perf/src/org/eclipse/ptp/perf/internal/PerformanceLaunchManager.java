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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobChangeEvent;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.core.runtime.jobs.JobChangeAdapter;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

public class PerformanceLaunchManager {
	
	private static final String buildText="Instrumenting and Building";
	private static final String launchText="Executing Instrumented Project";
	private static final String collectText="Collecting Performance Data";
	
	private String appNameAttribute;
	private String projNameAttribute;
	private String appPathAttribute=null;
	private LaunchConfigurationDelegate paraDel;
	
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
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException //, TAULaunch bLTool
	{
		final ILaunch launch = launchIn;
		final PerformanceLaunchSteps tauManager = new PerformanceLaunchSteps(configuration,appNameAttribute ,projNameAttribute, appPathAttribute);//, bLTool
		
		/**
		 * Uses the specified tool's build settings on the build manager for this project, 
		 * producing a new build configuration and instrumented binary file
		 */
		Job tauBuild=new Job(buildText){
			protected IStatus run(IProgressMonitor monitor) {
				try {
					tauManager.buildIndstrumented(monitor);
				} catch (Exception e) {
					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Build Incomplete",e);
				}
				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Build Successful",null);
			}
		};
		
		/**
		 * Execute the program specified in the build step
		 */
		final Job tauRun=new Job(launchText)
		{
			protected IStatus run(IProgressMonitor monitor) {
				try {
				if(!tauManager.performLaunch(paraDel, launch, monitor))
					return new Status(IStatus.WARNING,"com.ibm.jdg2e.concurrency",IStatus.WARNING,"Nothing to run",null);
				} catch (Exception e) {
						try {
							tauManager.cleanup();
						} catch (CoreException e1) {}
					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Execution Error",e);
				}
				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Execution Complete",null);
			}
		};
		
		/**
		 * Collect performance data from the execution handled in the run step
		 */
		final Job tauCollect=new Job(collectText){

			protected IStatus run(IProgressMonitor monitor) {
				try{
					tauManager.postlaunch(monitor);
				}catch(Exception e){
					return new Status(IStatus.ERROR,"com.ibm.jdg2e.concurrency",IStatus.ERROR,"Data Collection Error",e);
				}
				return new Status(IStatus.OK,"com.ibm.jdg2e.concurrency",IStatus.OK,"Data Collected",null);
			}
		};

		/**
		 * Manages job execution order
		 */
		JobChangeAdapter tauChange = new JobChangeAdapter(){
			public void done (IJobChangeEvent event){
				if(event.getJob().getName().equals(buildText)&&event.getResult().isOK())
				{
					tauRun.schedule();
				}
				else
				if(event.getJob().getName().equals(launchText)&&event.getResult().isOK())
				{
					tauCollect.schedule();
				}
			}
		};
		
		tauBuild.addJobChangeListener(tauChange);
		tauRun.addJobChangeListener(tauChange);
		tauBuild.schedule();
	}
}
