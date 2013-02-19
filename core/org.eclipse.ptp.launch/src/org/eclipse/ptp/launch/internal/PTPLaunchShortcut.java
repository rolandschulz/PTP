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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchGroup;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public class PTPLaunchShortcut implements ILaunchShortcut {
	public static final String LauncherGroupID = "org.eclipse.debug.ui.launchGroup.run"; //$NON-NLS-1$

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
			launch(((IStructuredSelection) selection).getFirstElement(), mode);
		}
	}

	public void launch(Object element, String mode) {
		if (!(element instanceof IFile)) {
			MessageDialog.openInformation(PTPLaunchPlugin.getActiveWorkbenchShell(), Messages.PTPLaunchShortcut_0,
					Messages.PTPLaunchShortcut_1);
			return;
		}

		IFile file = (IFile) element;

		ILaunchConfiguration config = getILaunchConfigure(file);
		IStructuredSelection selection = null;
		if (config == null) {
			selection = new StructuredSelection();
		} else {
			selection = new StructuredSelection(config);
		}

		ILaunchGroup group = DebugUITools.getLaunchGroup(config, mode);
		DebugUITools.openLaunchConfigurationDialogOnGroup(PTPDebugUIPlugin.getShell(), selection, group.getIdentifier());
	}

	public ILaunchConfiguration getILaunchConfigure(IFile file) {
		String projectName = file.getProject().getName();
		ILaunchManager lm = getLaunchManager();
		ILaunchConfigurationType configType = lm.getLaunchConfigurationType(IPTPLaunchConfigurationConstants.LAUNCH_APP_TYPE_ID);
		try {
			ILaunchConfiguration[] configs = lm.getLaunchConfigurations(configType);
			for (ILaunchConfiguration config : configs) {
				if (config.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, "").equals(projectName)) { //$NON-NLS-1$
					return config;
				}
			}
			ILaunchConfigurationWorkingCopy wc = configType.newInstance(file.getProject(), projectName);
			wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);
			wc.setAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, file.getName());
			// wc.setAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, IPTPLaunchConfigurationConstants.DEF_NETWORK_TYPE);

			return wc.doSave();
		} catch (CoreException e) {
			// Ignore
		}
		return null;
	}

	private ILaunchManager getLaunchManager() {
		return DebugPlugin.getDefault().getLaunchManager();
	}
}
