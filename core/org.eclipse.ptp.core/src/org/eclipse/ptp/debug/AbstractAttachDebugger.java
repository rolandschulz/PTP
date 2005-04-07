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
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;

/**
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
    public abstract void attachDebugger(ILaunchConfiguration ptpConfig) throws CoreException;
    
    protected IFile findExeFile(ILaunchConfiguration ptpConfig) throws CoreException {
        String projectName = ptpConfig.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
        String programName = ptpConfig.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String)null);

		if (projectName != null && !projectName.equals("")) {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			IFile programPath = project.getFile(programName);
			if (programPath != null && programPath.exists() && programPath.getLocation().toFile().exists())
			    return programPath;
		}

        Status status = new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, "No such program("+ programName +") found.", null);
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
