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
package org.eclipse.ptp.debug.sdm.internal.ui;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
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
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for SDM-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IResourceManagerControl resourceManager = null;
	private IRemoteServices remoteServices = null;
	//private IRemoteUIServices remoteUIServices = null;
	//private IRemoteConnection connection = null;
	
	private String errMsg = null;
	protected Text fRMDebuggerPathText = null;
	protected Text fRMDebuggerAddressText = null;
	protected Button fRMDebuggerBrowseButton = null;

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		/*
		 * Debugger tab is selected from within an existing page...
		 */
		try {
			fRMDebuggerAddressText.setText(getAddress(workingCopy));
			fRMDebuggerPathText.setText(workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
		} catch(CoreException e) {
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
		label.setText(Messages.getString("SDMDebuggerPage.path")); //$NON-NLS-1$
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
		fRMDebuggerBrowseButton = createPushButton(comp, Messages.getString("SDMDebuggerPage.browse"), null); //$NON-NLS-1$
		fRMDebuggerBrowseButton.addSelectionListener(new SelectionAdapter() {
		    @Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseFile();
				if (file != null) {
					fRMDebuggerPathText.setText(file);
				}
		    }
		});
		
		label = new Label(comp, SWT.NONE);
		label.setText(Messages.getString("SDMDebuggerPage.host")); //$NON-NLS-1$
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
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.getString("SDMDebuggerPage.debuggname"); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		/*
		 * Launch configuration is selected or we have just selected SDM as the debugger...
		 */
		try {
			String rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
			resourceManager = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			fRMDebuggerAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, EMPTY_STRING));
			fRMDebuggerPathText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING));
		} catch (CoreException e) {
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		if (getFieldContent(fRMDebuggerPathText.getText()) == null) {
			errMsg = Messages.getString("SDMDebuggerPage.err1"); //$NON-NLS-1$
		} else if (getFieldContent(fRMDebuggerAddressText.getText()) == null) {
			errMsg = Messages.getString("SDMDebuggerPage.err3"); //$NON-NLS-1$
		} else {
			errMsg = null;
		}
		setErrorMessage(errMsg);
		return (errMsg == null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Note: performApply gets called when either text is modified via
		 * updateLaunchConfigurationDialog(). Only update the configuration if
		 * things are valid.
		 */
		if (isValid(configuration)) {
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, getFieldContent(fRMDebuggerPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, getFieldContent(fRMDebuggerAddressText.getText()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * We have just selected SDM as the debugger...
		 */
		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();
		String path = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_FILE);
		/*
		 * Guess that the sdm executable is in the same location as the proxy. If not then use the
		 * preference setting.
		 */
		try {
			String rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
			IResourceManagerControl rm = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			if (rm != null) {
				IResourceManagerConfiguration rmConfig = rm.getConfiguration();
				if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
					AbstractRemoteResourceManagerConfiguration remConfig = (AbstractRemoteResourceManagerConfiguration)rmConfig;
					IPath rmPath = new Path(remConfig.getProxyServerPath());
					path = rmPath.removeLastSegments(1).append("sdm").toString(); //$NON-NLS-1$/
				}
			}
		} catch (CoreException e) {
		}

		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, path);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, getAddress(configuration));
	}
		
	/**
	 * Browse for a file. If remoteServices is not null, then the currently
	 * select resource manager supports remote browsing.
	 * 
	 * @return path to file selected in browser
	 */
	private String browseFile() {
		IRemoteUIServices remoteUISrv = getRemoteUIServices();
		if (remoteUISrv != null) {
			IRemoteUIFileManager fileManager = remoteUISrv.getUIFileManager();
			if (fileManager != null) {
				fileManager.setConnection(getRemoteConnection());
				IPath path = fileManager.browseFile(getShell(), 
						Messages.getString("SDMDebuggerPage.selectDebuggerExe"), 
						fRMDebuggerPathText.getText()); //$NON-NLS-1$
				if (path != null) {
					return path.toString();
				}
			}
		} else {
			FileDialog dialog = new FileDialog(getShell());
			dialog.setText(Messages.getString("SDMDebuggerPage.selectDebuggerExe"));
			dialog.setFileName(fRMDebuggerPathText.getText());
			return dialog.open();
		}
		return null;
	}

	/**
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
	 * 
	 * @param configuration
	 * @return
	 */
	private String getAddress(ILaunchConfigurationWorkingCopy configuration) {
		String address;
		String rmId;
		try {
			address = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, EMPTY_STRING);
			rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
		} catch (CoreException e) {
			return EMPTY_STRING;
		}
		
		IResourceManagerControl rm = (IResourceManagerControl)PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
		if (rm != null) {
			/*
			 * If the resource manager has been changed and this is a remote
			 * resource manager, then update the host field
			 */
			if (resourceManager != rm) {
				resourceManager = rm;
				AbstractRemoteResourceManagerConfiguration config = getRemoteResourceManagerConfigure();
				if (config != null) {
					if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
						return getRemoteConnection().getAddress();
					} else {
						return  config.getLocalAddress();
					}
				} else {
					return "localhost"; //$NON-NLS-1$
				}
			}
		}
		return address;
	}
	
	private AbstractRemoteResourceManagerConfiguration getRemoteResourceManagerConfigure() {
		if (resourceManager != null) {
			IResourceManagerConfiguration rmConfig = resourceManager.getConfiguration();
			if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration)
				return (AbstractRemoteResourceManagerConfiguration)rmConfig;
		}		
		return null;
		
	}
	
	/**
	 * Return remote services
	 * @return remote services
	 */
	private IRemoteServices getRemoteServices() {
		if (resourceManager == null)
			return null;
		
		if (remoteServices == null) {
			IResourceManagerConfiguration rmConfig = resourceManager.getConfiguration();
			if (rmConfig instanceof AbstractRemoteResourceManagerConfiguration) {
				AbstractRemoteResourceManagerConfiguration remConfig = (AbstractRemoteResourceManagerConfiguration)rmConfig;
				remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remConfig.getRemoteServicesId());
			}
		}
		return remoteServices;
	}
	private IRemoteUIServices getRemoteUIServices() {
		IRemoteServices rsrv = getRemoteServices();
		if (rsrv != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(rsrv);
		}
		return null;
	}
	
	private IRemoteConnection getRemoteConnection() {
		IRemoteServices rsrv = getRemoteServices();
		if (rsrv != null) {
			AbstractRemoteResourceManagerConfiguration config = getRemoteResourceManagerConfigure();
			if (config != null)
				return rsrv.getConnectionManager().getConnection(config.getConnectionName());
		}
		return null;
	}
	
    /**
     * Get and clean content of a Text field
     * @param text text obtained from a Text field
     * @return cleaned up content
     */
    protected String getFieldContent(String text) {
        if (text.trim().length() == 0 || text.equals(EMPTY_STRING))
            return null;
        return text;
    }
}
