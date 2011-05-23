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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMLaunchConfigurationConstants;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.debug.sdm.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteProxyOptions;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.rm.core.rmsystem.IRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * The dynamic tab for SDM-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private IResourceManager resourceManager = null;

	protected Combo fSDMBackendCombo = null;
	protected Text fRMDebuggerPathText = null;
	protected Text fRMDebuggerAddressText = null;
	protected Button fRMDebuggerBrowseButton = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#activated(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	@Override
	public void activated(ILaunchConfigurationWorkingCopy workingCopy) {
		/*
		 * Debugger tab is selected from within an existing page...
		 */
		try {
			fSDMBackendCombo.setText(workingCopy.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
							SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
			fRMDebuggerAddressText.setText(getAddress(workingCopy));
			fRMDebuggerPathText.setText(workingCopy.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					EMPTY_STRING));
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));
		comp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		Label label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_11);
		GridData gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fSDMBackendCombo = new Combo(comp, SWT.READ_ONLY);
		fSDMBackendCombo.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fSDMBackendCombo.setItems(SDMDebugCorePlugin.getDefault().getDebuggerBackends());
		fSDMBackendCombo.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_0);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fRMDebuggerPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fRMDebuggerPathText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fRMDebuggerPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		fRMDebuggerBrowseButton = createPushButton(comp, Messages.SDMPage_1, null);
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
		label.setText(Messages.SDMPage_2);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.SDMPage_3;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		/*
		 * Launch configuration is selected or we have just selected SDM as the
		 * debugger...
		 */
		try {
			String rmId = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
					EMPTY_STRING);
			resourceManager = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
			fSDMBackendCombo.setText(configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
							SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
			fRMDebuggerAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST,
					EMPTY_STRING));
			fRMDebuggerPathText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					EMPTY_STRING));
		} catch (CoreException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		if (getFieldContent(fRMDebuggerAddressText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_4);
		} else if (getFieldContent(fRMDebuggerPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_5);
		}
		return (getErrorMessage() == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (getFieldContent(fRMDebuggerAddressText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_7);
		} else if (getFieldContent(fRMDebuggerPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_8);
		}
		// setErrorMessage(errMsg);
		return (getErrorMessage() == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Note: performApply gets called when either text is modified via
		 * updateLaunchConfigurationDialog(). Only update the configuration if
		 * things are valid.
		 */
		if (isValid(configuration)) {
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					getFieldContent(fSDMBackendCombo.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					getFieldContent(fRMDebuggerPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST,
					getFieldContent(fRMDebuggerAddressText.getText()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * We have just selected SDM as the debugger...
		 */
		configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, getAddress(configuration));
	}

	/**
	 * Browse for a file. If remoteServices is not null, then the currently
	 * select resource manager supports remote browsing.
	 * 
	 * @return path to file selected in browser
	 */
	private String browseFile() {
		IRemoteUIServices remoteUISrv = getRemoteUIServices(resourceManager);
		if (remoteUISrv != null) {
			IRemoteUIFileManager fileManager = remoteUISrv.getUIFileManager();
			if (fileManager != null) {
				fileManager.setConnection(getRemoteConnection(resourceManager));
				return fileManager.browseFile(getShell(), Messages.SDMPage_10, fRMDebuggerPathText.getText(), 0);
			}
		} else {
			FileDialog dialog = new FileDialog(getShell());
			dialog.setText(Messages.SDMPage_10);
			dialog.setFileName(fRMDebuggerPathText.getText());
			return dialog.open();
		}
		return null;
	}

	/**
	 * Work out the address to supply as argument to the debug server. There are
	 * currently two cases:
	 * 
	 * 1. If port forwarding is enabled, then the address needs to be the
	 * localhost address of the host where the tunnel begins. Note this is
	 * different to previous versions where the debug server machine was
	 * possibly on a local network (e.g. a node in a cluster) but not
	 * necessarily on the same machine as the tunnel.
	 * 
	 * 2. If port forwarding is not enabled, then the address will be the
	 * address of the host running Eclipse). NOTE: this assumes that the machine
	 * running the debug server can contact the local host directly. In the case
	 * of the SDM, the "master" debug server process can potentially run on any
	 * node in the cluster. In many environments, compute nodes cannot
	 * communicate outside their local network.
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

		IResourceManager rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmId);
		if (rm != null) {
			/*
			 * If the resource manager has been changed and this is a remote
			 * resource manager, then update the host field
			 */
			if (resourceManager != rm) {
				resourceManager = rm;
				IRemoteResourceManagerConfiguration config = getRemoteResourceManagerConfiguration();
				if (config != null) {
					if (config.testOption(IRemoteProxyOptions.PORT_FORWARDING)) {
						return "localhost"; //$NON-NLS-1$
						// return getRemoteConnection(rm).getAddress();
					} else {
						return config.getLocalAddress();
					}
				} else {
					return "localhost"; //$NON-NLS-1$
				}
			}
		}
		return address;
	}

	/**
	 * Get the RM configuration information
	 * 
	 * @return AbstractRemoteResourceManagerConfiguration
	 */
	private IRemoteResourceManagerConfiguration getRemoteResourceManagerConfiguration() {
		if (resourceManager != null) {
			IResourceManagerConfiguration rmConfig = resourceManager.getConfiguration();
			if (rmConfig instanceof IRemoteResourceManagerConfiguration) {
				return (IRemoteResourceManagerConfiguration) rmConfig;
			}
		}
		return null;

	}

	/**
	 * Return remote services
	 * 
	 * @return remote services
	 */
	private IRemoteServices getRemoteServices(IResourceManager rm) {
		if (rm != null) {
			IResourceManagerComponentConfiguration rmConfig = rm.getControlConfiguration();
			return PTPRemoteUIPlugin.getDefault().getRemoteServices(rmConfig.getRemoteServicesId(), getLaunchConfigurationDialog());
		}
		return null;
	}

	/**
	 * Look up remote UI services
	 * 
	 * @return IRemoteUIServices
	 */
	private IRemoteUIServices getRemoteUIServices(IResourceManager rm) {
		IRemoteServices rsrv = getRemoteServices(rm);
		if (rsrv != null) {
			return PTPRemoteUIPlugin.getDefault().getRemoteUIServices(rsrv);
		}
		return null;
	}

	/**
	 * Get the current remote connection selected in the RM. Will open the
	 * connection if it is closed.
	 * 
	 * @return IRemoteConnection
	 */
	private IRemoteConnection getRemoteConnection(IResourceManager rm) {
		IRemoteServices rsrv = getRemoteServices(rm);
		if (rsrv != null) {
			String connName = rm.getControlConfiguration().getConnectionName();
			if (connName != null) {
				IRemoteConnectionManager mgr = rsrv.getConnectionManager();
				if (mgr != null) {
					IRemoteConnection conn = mgr.getConnection(connName);
					if (conn != null && !conn.isOpen()) {
						IRemoteUIServices uiServices = getRemoteUIServices(rm);
						if (uiServices != null) {
							IRemoteUIConnectionManager connMgr = uiServices.getUIConnectionManager();
							if (connMgr != null) {
								connMgr.openConnectionWithProgress(getShell(), null, conn);
							}
						}
					}
					return conn;
				}
			}
		}
		return null;
	}

	/**
	 * Get and clean content of a Text field
	 * 
	 * @param text
	 *            text obtained from a Text field
	 * @return cleaned up content
	 */
	protected String getFieldContent(String text) {
		if (text.trim().length() == 0 || text.equals(EMPTY_STRING)) {
			return null;
		}
		return text;
	}
}
