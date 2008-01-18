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
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProxyOptions;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for SDM-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IResourceManagerControl resourceManager = null;
	private IRemoteServices remoteServices = null;
	private IRemoteConnection connection = null;
	
	private String errMsg = null;
	protected Text fRMDebuggerPathText = null;
	protected Text fRMDebuggerAddressText = null;
	protected Button fRMDebuggerBrowseButton = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		try {
			/*
			 * Work out the address to supply as argument to the debug server. There are currently
			 * two cases:
			 * 
			 * 1. If port forwarding is enabled, then the address needs to be the address of the host that is 
			 * running the proxy (since this is where the tunnel begins), but accessible from the machine running 
			 * the debug server. Since the debug server machine may be on a local network (e.g. a node in a 
			 * cluster), it will typically NOT be the same address that is used to start the proxy. 
			 * 
			 * 2. If port forwarding is not enabled, then the address will be the address of the host running 
			 * Eclipse). NOTE: this assumes that the machine running the debug server can contact the local host directly. 
			 * In the case of the SDM, the "master" debug server process can potentially run on any node in the cluster. 
			 * In many environments, compute nodes cannot communicate outside their local network.
			 */
			String address = workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, EMPTY_STRING);
			String rmId = workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
			IResourceManagerControl rm = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			IResourceManagerConfiguration rmConfig = rm.getConfiguration();

			/*
			 * Enable remote section if this is a remote resource manager
			 */
			if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
				enableRemoteSection(true);
			}

			/*
			 * If the resource manager has been changed and this is a remote
			 * resource manager, then update the host field
			 */
			if (resourceManager != rm) {
				resourceManager = rm;
				if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
					AbstractRemoteResourceManagerConfiguration remConfig = (AbstractRemoteResourceManagerConfiguration)rmConfig;
					remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remConfig.getRemoteServicesId());
					if (remoteServices != null) {
						connection = remoteServices.getConnectionManager().getConnection(remConfig.getConnectionName());
						if (remConfig.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
							address = connection.getAddress();
						} else {
							address = remConfig.getLocalAddress();
						}
					}
				}
			}
			
			fRMDebuggerPathText.setText(workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
			fRMDebuggerAddressText.setText(address);
		} catch(CoreException e) {
			errMsg = ExternalDebugUIMessages.getString("SDMDebuggerPage.err2"); //$NON-NLS-1$
		}
	}
	
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
		
		fRMDebuggerPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fRMDebuggerPathText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fRMDebuggerPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
		    	updateLaunchConfigurationDialog();
			}
		});
		fRMDebuggerBrowseButton = createPushButton(comp, ExternalDebugUIMessages.getString("SDMDebuggerPage.browse"), null); //$NON-NLS-1$
		fRMDebuggerBrowseButton.addSelectionListener(new SelectionAdapter() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseRemoteFile();
				if (file != null) {
					fRMDebuggerPathText.setText(file);
				}
		    }
		});
		
		label = new Label(comp, SWT.NONE);
		label.setText(ExternalDebugUIMessages.getString("SDMDebuggerPage.host")); //$NON-NLS-1$
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);
		
		fRMDebuggerAddressText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fRMDebuggerAddressText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fRMDebuggerAddressText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
		    	updateLaunchConfigurationDialog();
			}
		});
	
		setControl(parent);
		
		enableRemoteSection(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return ExternalDebugUIMessages.getString("SDMDebuggerPage.debuggname"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		System.out.println("initializeFrom");
		try {
			String rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
			resourceManager = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			IResourceManagerConfiguration rmConfig = resourceManager.getConfiguration();

			/*
			 * Enable remote section if this is a remote resource manager
			 */
			if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
				enableRemoteSection(true);
				fRMDebuggerAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, EMPTY_STRING));
			}
			fRMDebuggerPathText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
		} catch (CoreException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (remoteServices != null) {
			if (getFieldContent(fRMDebuggerPathText.getText()) == null) {
				errMsg = ExternalDebugUIMessages.getString("SDMDebuggerPage.err1"); //$NON-NLS-1$
			} else if (getFieldContent(fRMDebuggerAddressText.getText()) == null) {
				errMsg = ExternalDebugUIMessages.getString("SDMDebuggerPage.err3"); //$NON-NLS-1$
			} else {
				errMsg = null;
			}
		}
		setErrorMessage(errMsg);
		return (errMsg == null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (remoteServices != null) {
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, getFieldContent(fRMDebuggerPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, getFieldContent(fRMDebuggerAddressText.getText()));
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
		if (remoteServices != null) {
			IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
			if (fileManager != null) {
				IPath path = fileManager.browseFile(getShell(), ExternalDebugUIMessages.getString("SDMDebuggerPage.selectDebuggerExe"), fRMDebuggerPathText.getText()); //$NON-NLS-1$
				return path.toString();
			}
		}
		return null;
	}
	
	/**
	 * @param enabled
	 */
	private void enableRemoteSection(boolean enabled) {
		fRMDebuggerAddressText.setEnabled(enabled);
		if (!enabled) {
			fRMDebuggerAddressText.setText(EMPTY_STRING);
		}
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
