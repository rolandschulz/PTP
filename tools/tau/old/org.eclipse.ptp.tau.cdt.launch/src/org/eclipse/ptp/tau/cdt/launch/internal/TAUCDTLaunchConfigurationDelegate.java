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
package org.eclipse.ptp.tau.cdt.launch.internal;

import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.launch.internal.LocalRunLaunchDelegate;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.tau.core.internal.TAULaunchManage;


/**
 * 
 */
public class TAUCDTLaunchConfigurationDelegate extends LocalRunLaunchDelegate {
	
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException
	{
		TAULaunchManage manager = new TAULaunchManage(configuration,monitor, ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME,ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME);
		boolean runbuilt = false;
		try {
			runbuilt = manager.TAUBuild();
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		if(!runbuilt)
			return;
		
		//Launch the instrumented executable
		super.launch(configuration, mode, launch, monitor);
		
		//Collect data and clean up
		try{
		manager.postlaunch();
		}catch(Exception e){e.printStackTrace();}
	}		
}
