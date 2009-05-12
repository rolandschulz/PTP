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
package org.eclipse.ptp.perf.parallel;

import java.io.File;

//import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.ParallelLaunchConfigurationDelegate;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.perf.internal.ILaunchFactory;
import org.eclipse.ptp.perf.internal.PerformanceLaunchManager;

/**
 * Launches parallel C/C++ (or Fortran) applications after rebuilding them with performance instrumentation
 */
public class ParallelPerformanceLaunchConfigurationDelegate extends ParallelLaunchConfigurationDelegate implements IPerformanceLaunchConfigurationConstants{
	
	/**
	 * The primary launch command of this launch configuration delegate.  The operations in this function are divided into
	 * three jobs:  Buildig, Running and Data collection
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException
	{
		// save the executable location so we can access it in the postprocessing 
		ILaunchConfigurationWorkingCopy  wc=configuration.getWorkingCopy();
		String progName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME,"defaultValue");
		String progPath = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,"defaultValue");
		String projName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,"defaultValue");
		wc.setAttribute(PERF_EXECUTABLE_NAME, progPath+File.separator+progName);
		wc.setAttribute(PERF_PROJECT_NAME, projName);
		wc.setAttribute(PERF_ATTR_ARGUMENTS_TAG, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS);
		wc.setAttribute(PERF_PROJECT_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		wc.setAttribute(PERF_EXECUTABLE_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME);
		wc.setAttribute(PERF_EXECUTABLE_PATH_TAG, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH);
		wc.doSave();
		
		ILaunchFactory lf = new ParallelLaunchFactory();
		
		{
			PerformanceLaunchManager plaunch=new PerformanceLaunchManager(new ParallelLaunchConfigurationDelegate(), lf);//,IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME ,IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH);
			plaunch.launch(configuration,mode, launchIn, monitor);// tool, 
		}
	}
}
