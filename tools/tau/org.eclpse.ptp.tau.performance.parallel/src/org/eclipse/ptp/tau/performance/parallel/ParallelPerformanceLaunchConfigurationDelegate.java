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
package org.eclipse.ptp.tau.performance.parallel;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.internal.ParallelLaunchConfigurationDelegate;
import org.eclipse.ptp.tau.performance.internal.IPerformanceLaunchConfigurationConstants;
import org.eclipse.ptp.tau.performance.internal.PerformanceLaunchManager;

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
		PerformanceLaunchManager plaunch=new PerformanceLaunchManager(new ParallelLaunchConfigurationDelegate(),IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME ,IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		plaunch.launch(configuration,mode, launchIn, monitor);// tool, 
	}
}
