/*
 * Created on 1/12/2004
 *
 */
package org.eclipse.ptp.debug;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPDTLaunchConfigurationConstants;

/**
 * @author clement
 *
 */
public abstract class AbstractAttachDebugger {
    protected String pidText = null;
    protected String debugConfigName = "";
    protected ILaunch debugLaunch = null;
    
    public AbstractAttachDebugger(String pidText, String debugConfigName) {
        this.pidText = pidText;
        this.debugConfigName = debugConfigName;
    }
    
    protected ILaunchManager getLaunchManager() {
        return DebugPlugin.getDefault().getLaunchManager();
    }
    
    protected abstract ILaunchConfiguration createDebugConfiguration(IFile exeFile) throws CoreException;
	protected abstract boolean createLaunch(ILaunchConfiguration config) throws CoreException;
    public abstract void attachDebugger(ILaunchConfiguration pdtConfig) throws CoreException;
    
    protected IFile findExeFile(ILaunchConfiguration pdtConfig) throws CoreException {
        String projectName = pdtConfig.getAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
        String programName = pdtConfig.getAttribute(IPDTLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String)null);

		if (projectName != null && !projectName.equals("")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IFile programPath = project.getFile(programName);
			if (programPath != null && programPath.exists() && programPath.getLocation().toFile().exists())
			    return programPath;
		}

        Status status = new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, "No such program("+ programName +") found.", null);
        throw new CoreException(status);
    }
    
	protected String renderTargetLabel(String name) {
		String format = "{0} ({1})";
		String timestamp = DateFormat.getInstance().format(new Date(System.currentTimeMillis()));
		return MessageFormat.format(format, new String[]{name, timestamp});
	}
	
	protected int getPid() throws CoreException {
	    int pid = -1;
	    if (pidText != null && !pidText.equals("")) {
		    try {
		        pid = Integer.parseInt(pidText);
		    } catch (NumberFormatException e) {
		    }
	    }
        return pid;
    }
}
