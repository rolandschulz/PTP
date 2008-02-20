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


import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.LocalRunLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;

/**
 * Launches sequential C/C++ (or Fortran) applications after rebuilding them with performance tool instrumentation
 */
public class PerformanceLaunchConfigurationDelegate extends LocalRunLaunchDelegate implements IPerformanceLaunchConfigurationConstants{
	
	/**
	 * The primary launch command of this launch configuration delegate.  The operations in this function are divided into
	 * three jobs:  Building, Running and Data collection
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launchIn, IProgressMonitor monitor) throws CoreException
	{
		//TODO:  This is a special case for TAU.  It should be merged into the general performance framework
		//TAULaunch tool=null;
		//if(configuration.getAttribute(TAULAUNCH, TAULAUNCH_DEF))
			//tool=new TAULaunch();
		PerformanceLaunchManager plaunch=new PerformanceLaunchManager(new LocalRunLaunchDelegate(),ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		plaunch.launch(configuration, mode, launchIn, monitor);//,tool
		
	}
}
