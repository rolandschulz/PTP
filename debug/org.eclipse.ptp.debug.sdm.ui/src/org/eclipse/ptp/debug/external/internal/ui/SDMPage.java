/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.remote.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected Text fRMDebuggerText = null;
	protected Button fRMDebuggerButton = null;
	
	private IRemoteServices remoteServices = null;
	private IRemoteConnection connection = null;
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private String errMsg = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Label label = new Label(comp, SWT.NONE);
		label.setText(ExternalDebugUIMessages.getString("SDMDebuggerPage.path")); //$NON-NLS-1$
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		fRMDebuggerText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fRMDebuggerText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fRMDebuggerButton = createPushButton(comp, ExternalDebugUIMessages.getString("SDMDebuggerPage.browse"), null); //$NON-NLS-1$
		fRMDebuggerButton.addSelectionListener(new SelectionAdapter() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseRemoteFile();
				if (file != null) {
					fRMDebuggerText.setText(file);
			    	updateLaunchConfigurationDialog();
				}
		    }
		});
		setControl(parent);
		
		enableRemoteSection(false);
	}
	
	/**
	 * @param enabled
	 */
	private void enableRemoteSection(boolean enabled) {
		fRMDebuggerText.setEnabled(enabled);
		fRMDebuggerButton.setEnabled(enabled);
		if (!enabled) {
			fRMDebuggerText.setText(EMPTY_STRING);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);
	}
	
	/**
	 * @return
	 */
	private String browseRemoteFile() {
		IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
		if (fileManager != null) {
			IPath path = fileManager.browseFile(getShell(), ExternalDebugUIMessages.getString("SDMDebuggerPage.selectDebuggerExe"), fRMDebuggerText.getText()); //$NON-NLS-1$
			return path.toString();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (remoteServices != null) {
			if (getFieldContent(fRMDebuggerText.getText()) == null) {
				errMsg = ExternalDebugUIMessages.getString("SDMDebuggerPage.err1"); //$NON-NLS-1$
			}
			else
				errMsg = null;
		}
		setErrorMessage(errMsg);
		return (errMsg == null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		try {
			String rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
			IResourceManagerControl rm = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			if (rm != null) {
				IResourceManagerConfiguration rmConfig = rm.getConfiguration();
				if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
					enableRemoteSection(true);
					AbstractRemoteResourceManagerConfiguration remConfig = (AbstractRemoteResourceManagerConfiguration)rmConfig;
					remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remConfig.getRemoteServicesId());
					if (remoteServices != null) {
						connection = remoteServices.getConnectionManager().getConnection(remConfig.getConnectionName());
					}
				}
			}
			fRMDebuggerText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
		} catch(CoreException e) {
			errMsg = ExternalDebugUIMessages.getString("SDMDebuggerPage.err2"); //$NON-NLS-1$
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (remoteServices != null) {
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, getFieldContent(fRMDebuggerText.getText()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalDebugUIMessages.getString("SDMDebuggerPage.debuggname"); //$NON-NLS-1$
	}
	
    /**
     * @param text
     * @return
     */
    protected String getFieldContent(String text) {
        if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
            return null;
        return text;
    }
}
