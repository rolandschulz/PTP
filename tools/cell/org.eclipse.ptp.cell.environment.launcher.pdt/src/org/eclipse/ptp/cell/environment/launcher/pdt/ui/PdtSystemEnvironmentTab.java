/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.pdt.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.cell.environment.launcher.pdt.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.IPdtLaunchAttributes;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;


/**
 * @author Richard Maciel
 *
 */
public class PdtSystemEnvironmentTab extends AbstractLaunchConfigurationTab {
	// PDT System-specific info
	Frame pdtSystemInfo;
	TextGroup traceLibDirPath;
	TextGroup pdtModulePath;
	
	
	
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite topControl = new Composite(parent, SWT.NONE);
		GridLayout topLayout = new GridLayout();
		topControl.setLayout(topLayout);

		setControl(topControl);
		
		ModifyListener pdtModifyListener = new PdtModifyListener();

		FrameMold frMold = new FrameMold(Messages.getString("PdtSystemEnvironmentTab.FrameControl_Label_PdtSystemInfo")); //$NON-NLS-1$
		TextMold tmold = new TextMold(TextMold.GRID_DATA_SPAN | TextMold.GRID_DATA_ALIGNMENT_FILL 
				| TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.LABELABOVE, 
				Messages.getString("PdtSystemEnvironmentTab.TextControl_Label_TraceLibPath")); //$NON-NLS-1$
		
		
		// System Info Frame
		pdtSystemInfo = new Frame(topControl, frMold);
		
		// Controls from the System info frame
		traceLibDirPath = new TextGroup(pdtSystemInfo.getTopUserReservedComposite(), tmold);
		traceLibDirPath.addModifyListener(pdtModifyListener);
		
		
		tmold.setLabel(Messages.getString("PdtSystemEnvironmentTab.TextControl_Label_PdtKernelModulePath")); //$NON-NLS-1$
		pdtModulePath = new TextGroup(pdtSystemInfo.getTopUserReservedComposite(), tmold);
		pdtModulePath.addModifyListener(pdtModifyListener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return Messages.getString("PdtSystemEnvironmentTab.Tab_Title_PdtSystem"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			traceLibDirPath.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH,
							IPdtLaunchAttributes.DEFAULT_TRACE_LIB_PATH));
			pdtModulePath.setString(
					configuration.getAttribute(IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH,
							IPdtLaunchAttributes.DEFAULT_PDT_MODULE_PATH));
		} catch (CoreException e) {
			Debug.POLICY.logError(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH,
				traceLibDirPath.getString());
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH,
				pdtModulePath.getString());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH,
				IPdtLaunchAttributes.DEFAULT_TRACE_LIB_PATH);
		configuration.setAttribute(IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH,
				IPdtLaunchAttributes.DEFAULT_PDT_MODULE_PATH);
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		Debug.read();
		
		if (!super.isValid(launchConfig)) {
			return false;
		}
		
		// Just check if the paths are valid unix paths.
		// launchConfig.getAttribute(attributeName, defaultValue)
		// For now just return true
		try {
				// Check remote paths
				if(!PdtEnvironmentTab.validateRemotePath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH, IPdtLaunchAttributes.DEFAULT_PDT_MODULE_PATH))) {
					setErrorMessage(Messages.getString("PdtSystemEnvironmentTab.IsValid_Error_RemotePdtModulePathInvalid")); //$NON-NLS-1$
					return false;
				} else if(!PdtEnvironmentTab.validateRemotePath(launchConfig.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH, IPdtLaunchAttributes.DEFAULT_TRACE_LIB_PATH))) {
					setErrorMessage(Messages.getString("PdtSystemEnvironmentTab.IsValid_Error_RemoteTraceLibDirPathInvalid")); //$NON-NLS-1$
					return false;
				}
				
		} catch (CoreException e) {
			Debug.POLICY.logError(e);
			return false;
		}
		
		setErrorMessage(null);
		return true;
	}
	
	/**
	 * Simple listener of the tab's controls
	 * 
	 * @author Richard Maciel
	 *
	 */
	private class PdtModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			Debug.read();
			
			try {
				updateLaunchConfigurationDialog();
			} catch(Exception exception) {
				Debug.POLICY.logError(exception);
			}
		}
	}
	
}
