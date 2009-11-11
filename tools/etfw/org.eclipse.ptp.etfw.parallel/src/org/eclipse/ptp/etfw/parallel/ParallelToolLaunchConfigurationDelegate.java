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
package org.eclipse.ptp.etfw.parallel;

import java.io.File;

//import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.IToolLaunchConfigurationConstants;
import org.eclipse.ptp.etfw.internal.ILaunchFactory;
import org.eclipse.ptp.etfw.internal.ToolLaunchManager;
import org.eclipse.ptp.launch.ParallelLaunchConfigurationDelegate;

/**
 * Launches parallel C/C++ (or Fortran) applications after rebuilding them with performance instrumentation
 */
public class ParallelToolLaunchConfigurationDelegate extends ParallelLaunchConfigurationDelegate implements IToolLaunchConfigurationConstants{
	
	private boolean initialized = false;
	
	/**
	 * The primary launch command of this launch configuration delegate.  The operations in this function are divided into
	 * three jobs:  Buildig, Running and Data collection
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException
	{
		if(initialized){
			super.launch(configuration, mode, launchIn, monitor);
			return;
		}
		
		// save the executable location so we can access it in the postprocessing 
		ILaunchConfigurationWorkingCopy  wc=configuration.getWorkingCopy();
		String progName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME,"defaultValue");
		String progPath = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,"defaultValue");
		String projName = wc.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,"defaultValue");
		wc.setAttribute(EXTOOL_EXECUTABLE_NAME, progPath+File.separator+progName);
		wc.setAttribute(EXTOOL_PROJECT_NAME, projName);
		wc.setAttribute(EXTOOL_ATTR_ARGUMENTS_TAG, IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS);
		wc.setAttribute(EXTOOL_PROJECT_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		wc.setAttribute(EXTOOL_EXECUTABLE_NAME_TAG, IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME);
		wc.setAttribute(EXTOOL_EXECUTABLE_PATH_TAG, IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH);
		wc.doSave();
		
		ILaunchFactory lf = new ParallelLaunchFactory();
		
		{
			initialized=true;
			ToolLaunchManager plaunch=new ToolLaunchManager(this, lf);//,IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME ,IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH);
			plaunch.launch(configuration,mode, launchIn, monitor);// tool, 
		}
		initialized=false;
	}
}
