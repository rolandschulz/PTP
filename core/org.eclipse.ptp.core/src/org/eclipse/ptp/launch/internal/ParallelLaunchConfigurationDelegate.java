package org.eclipse.ptp.launch.internal;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.ptp.ui.UIUtils;

/**
 * @author Clement
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
		
		UIUtils.switchPerspectiveTo(UIUtils.PPerspectiveFactory_ID);    		
		monitor.worked(1);

		//check for cancellation
		if (monitor.isCanceled())
			return;
		
		monitor.subTask(UIMessage.getResourceString("ParallelLaunchConfigurationDelegate.Verifying_launch_attributes"));
		
		// done the verification phase
		String projectName = verifyProject(configuration).getName();
		String[] args = vertifyArgument(configuration);
		File workDirectory = vertifyWorkDirectory(configuration);

		getLaunchManager().execMI(launch, workDirectory, null, args, monitor);
		monitor.worked(5);
		
		
		getLaunchManager().setPDTConfiguration(configuration);
				
		if (monitor.isCanceled())
			return;

		monitor.done();
	}
}