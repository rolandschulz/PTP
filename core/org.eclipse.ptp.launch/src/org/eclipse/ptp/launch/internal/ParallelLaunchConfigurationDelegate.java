/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.internal;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.internal.ui.LaunchUtils;

/**
 * 
 */
public class ParallelLaunchConfigurationDelegate extends AbstractParallelLaunchConfigurationDelegate {
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (monitor == null)
		    monitor = new NullProgressMonitor();		
				
		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 10);
		
		LaunchUtils.switchPerspectiveTo(LaunchUtils.PPerspectiveFactory_ID);    		
		monitor.worked(1);

		//check for cancellation
		if (monitor.isCanceled())
			return;
		
		monitor.subTask(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Verifying_launch_attributes"));
		
		// done the verification phase
		String projectName = verifyProject(configuration).getName();
		JobRunConfiguration jrunconfig = getJobRunConfiguration(configuration); 
		//String[] args = verifyArgument(configuration);
		File workDirectory = vertifyWorkDirectory(configuration);

		//getLaunchManager().execMI(launch, workDirectory, null, jrunconfig, monitor);
		monitor.worked(5);
		
		
		getLaunchManager().setPTPConfiguration(configuration);
				
		if (monitor.isCanceled())
			return;

		monitor.done();
	}
}