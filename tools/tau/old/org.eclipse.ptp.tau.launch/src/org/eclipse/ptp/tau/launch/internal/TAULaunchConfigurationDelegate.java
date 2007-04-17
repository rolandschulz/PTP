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
package org.eclipse.ptp.tau.launch.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.internal.ParallelLaunchConfigurationDelegate;
import org.eclipse.ptp.tau.core.internal.TAULaunchManage;


/**
 * 
 */
public class TAULaunchConfigurationDelegate extends ParallelLaunchConfigurationDelegate {

	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		TAULaunchManage manager = new TAULaunchManage(configuration,monitor, IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME ,IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME);//ICDTLaunchConfigurationConstants.ATTR_PROGRAM_NAME , ICDTLaunchConfigurationConstants.ATTR_PROJECT_NAME
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
		//ILaunch l = launch;//super.getLaunch(configuration, mode);
		//l.
		mode = ILaunchManager.RUN_MODE;
		super.launch(configuration, mode, launch, monitor);
		
		/*if(launch.canTerminate());
		while(!launch.isTerminated()){try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
			//Auto-generated catch block
			e.printStackTrace();
		}System.out.println("Waiting on launch");
		}*/
		
		//Collect data and clean up
		try{
			manager.postlaunch();
		}catch(Exception e){e.printStackTrace();}
		
	}
	
}
