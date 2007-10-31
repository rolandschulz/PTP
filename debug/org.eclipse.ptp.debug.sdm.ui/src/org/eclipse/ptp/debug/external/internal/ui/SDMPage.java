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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.launch.IPTPRemoteLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for gdb-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected Text fRMDebuggerText = null;
	protected Button fRMDebuggerButton = null;
	
	private String remoteHost = null;
	protected static final String EMPTY_STRING = "";

	private String errMsg = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fRMDebuggerText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fRMDebuggerText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fRMDebuggerButton = createPushButton(comp, ExternalDebugUIMessages.getString("SDMDebuggerPage.remotedebugger"), null);
		fRMDebuggerButton.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
				/*
				String file = browseRemoteFile();
				if (file != null) {
					fRMDebuggerText.setText(file);
			    	updateLaunchConfigurationDialog();
				}
				*/
		    }
		});
		setControl(parent);
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
	/*
	private String browseRemoteFile() {
		IHost host = getHost(remoteHost);
		if (host != null) {
			SystemRemoteFileDialog dialog = new SystemRemoteFileDialog(getShell(), "Select remote debugger...", host);
			dialog.setBlockOnOpen(true);
			if (dialog.open() == Window.OK) {
				Object rmObj = dialog.getSelectedObject();
				if (rmObj instanceof IRemoteFile) {
					return ((IRemoteFile)rmObj).getAbsolutePath();
				}
			}
		}
		return null;
	}
	*/
	/*
	private String browseRemoteDirectory() {
		IHost host = getHost(getRemoteConnection().getHostname());
		if (host != null) {
			SystemRemoteFolderDialog dialog = new SystemRemoteFolderDialog(getShell(), "Select remote working directory", host);
			//dialog.setPreSelection(selection)
			dialog.setBlockOnOpen(true);
			if (dialog.open() == Window.OK) {
				Object rmObj = dialog.getSelectedObject();
				if (rmObj instanceof IRemoteFile) {
					return ((IRemoteFile)rmObj).getAbsolutePath();
				}
			}
		}
		return null;
	}
	*/
	/*
	private IResourceManager getResourceManager() {
		try {
			IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
			if (universe != null) {
				String rmID = configuration.getAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_UNIQUENAME, (String)null);
				if (rmID != null) {
					for (IResourceManager resMgr : universe.getResourceManagers()) {
						if (resMgr.getUniqueName().equals(rmID)) {
							return resMgr;
						}
					}
				}
			}
		}
		catch (CoreException e) {}
		return null;
	}
	private IRemoteConnection getRemoteConnection() {
		if (rmConnection == null) {
			IResourceManager rmMgr = getResourceManager();
			if (rmMgr != null) {
				if (rmMgr instanceof IResourceManagerControl) {
					IResourceManagerConfiguration rmConfig = ((IResourceManagerControl)rmMgr).getConfiguration();
					if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
						String remoteID = ((AbstractRemoteResourceManagerConfiguration)rmConfig).getRemoteServicesId();
						IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remoteID);
						if (remoteServices != null) {
							rmConnection = remoteServices.getConnectionManager().getConnection(((AbstractRemoteResourceManagerConfiguration)rmConfig).getConnectionName());
						}
					}
				}
			}
		}
		return rmConnection;
	}
	*/
	
	/**
	 * @param hostname
	 * @return
	 */
	/*
	private IHost getHost(String hostname) {
		if (hostname == null)
			return null;
		IHost[] hosts = RSECorePlugin.getDefault().getSystemRegistry().getHosts();
		for (IHost host : hosts) {
			if (host.getAliasName().equals(hostname)) {
				return host;
			}
		}
		return null;
	}
	*/

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (remoteHost != null) {
			if (getFieldContent(fRMDebuggerText.getText()) == null) {
				errMsg = "No remote debugger found";
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
			remoteHost = configuration.getAttribute(IPTPRemoteLaunchConfigurationConstants.ATTR_REMOTE_CONNECTION, (String)null);
			enableRemoteSection(remoteHost != null);
			fRMDebuggerText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
		}
		catch(CoreException e) {
			errMsg = "Exception occurred reading configuration";
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (remoteHost != null) {
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, getFieldContent(fRMDebuggerText.getText()));
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalDebugUIMessages.getString("SDMDebuggerPage.debuggname");
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
