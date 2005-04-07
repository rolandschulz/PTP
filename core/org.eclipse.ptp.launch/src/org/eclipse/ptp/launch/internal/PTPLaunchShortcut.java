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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.internal.ui.DebugUIPlugin;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 *
 */
public class PTPLaunchShortcut implements ILaunchShortcut {
    public static final String LauncherGroupID = "org.eclipse.debug.ui.launchGroup.run";
    
	/**
	 * @see ILaunchShortcut#launch(IEditorPart, String)
	 */
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		IProject element = (IProject) input.getAdapter(IProject.class);
		launch(element, mode);
	}
	
	/**
	 * @see ILaunchShortcut#launch(ISelection, String)
	 */
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
		    launch(((IStructuredSelection)selection).getFirstElement(), mode);
		} 		
	}
	
	public void launch(Object element, String mode) {
	    if (!(element instanceof IFile)) {
	        MessageDialog.openInformation(PTPLaunchPlugin.getActiveWorkbenchShell(), "Incorrect file", "Please select parallel program file");
	        return;
	    }
	        
	    IFile file = (IFile)element;
	        
	    ILaunchConfiguration config = getILaunchConfigure(file);
	    IStructuredSelection selection = null;
	    if (config == null)
	        selection  = new StructuredSelection();
	    else 
	        selection = new StructuredSelection(config);
	        
	    ILaunchGroup group = DebugUITools.getLaunchGroup(config, mode);
		DebugUITools.openLaunchConfigurationDialogOnGroup(DebugUIPlugin.getShell(), selection, group.getIdentifier());
	}
	
	public ILaunchConfiguration getILaunchConfigure(IFile file) {
	    String projectName = file.getProject().getName();
	    ILaunchManager lm = getLaunchManager();
	    ILaunchConfigurationType configType = lm.getLaunchConfigurationType(IPTPLaunchConfigurationConstants.PTP_LAUNCHCONFIGURETYPE_ID);
		try {
		    ILaunchConfiguration[] configs = lm.getLaunchConfigurations(configType);
		    for (int i=0; i<configs.length; i++) {
		        if (configs[i].getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(projectName))
		            return configs[i];
		    }
		    ILaunchConfigurationWorkingCopy wc = configType.newInstance(file.getProject(), projectName);		    
		    wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
		    wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, file.getName());
	        wc.setAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, IPTPLaunchConfigurationConstants.DEF_NUMBER_OF_PROCESSES);
	        //wc.setAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, IPTPLaunchConfigurationConstants.DEF_NETWORK_TYPE);
	        wc.setAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, IPTPLaunchConfigurationConstants.DEF_PROCESSES_PER_NODE);
	        wc.setAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, IPTPLaunchConfigurationConstants.DEF_FIRST_NODE_NUMBER);

		    return wc.doSave();
		} catch (CoreException e) {
		}
		return null;
	}
	
	
	private ILaunchManager getLaunchManager() {
	    return DebugPlugin.getDefault().getLaunchManager();
	}
}
