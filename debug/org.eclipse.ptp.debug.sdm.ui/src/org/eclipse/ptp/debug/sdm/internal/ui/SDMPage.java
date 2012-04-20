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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.debug.sdm.core.SDMDebugCorePlugin;
import org.eclipse.ptp.debug.sdm.core.SDMLaunchConfigurationConstants;
import org.eclipse.ptp.debug.sdm.core.SDMPreferenceConstants;
import org.eclipse.ptp.debug.sdm.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
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
	protected static final String LOCALHOST = "localhost"; //$NON-NLS-1$

	private IRemoteConnection fRemoteConnection = null;

	protected Combo fSDMBackendCombo = null;
	protected Text fRMDebuggerPathText = null;
	protected Text fSessionAddressText = null;
	protected Button fRMDebuggerBrowseButton = null;
	protected Button fDefaultSessionAddressButton;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse .swt.widgets.Composite)
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

		fDefaultSessionAddressButton = createCheckButton(comp, Messages.SDMPage_Use_default_session_address);
		fDefaultSessionAddressButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		fDefaultSessionAddressButton.setLayoutData(gd);

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_Session_address);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fSessionAddressText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fSessionAddressText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fSessionAddressText.addModifyListener(new ModifyListener() {
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		/*
		 * Launch configuration is selected or we have just selected SDM as the debugger...
		 */
		try {
			fSDMBackendCombo.setText(configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
							SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)));
			fSessionAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST));
			fRMDebuggerPathText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					EMPTY_STRING));
			fRemoteConnection = getRemoteConnection(configuration);
			fDefaultSessionAddressButton.setSelection(fRemoteConnection == null
					|| (fRemoteConnection.supportsTCPPortForwarding() && fSessionAddressText.getText().equals(LOCALHOST)));
			updateEnablement();
		} catch (CoreException e) {
		}
	}

	private void updateEnablement() {
		fSessionAddressText.setEnabled(!fDefaultSessionAddressButton.getSelection());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {
		setErrorMessage(null);
		if (getFieldContent(fSessionAddressText.getText()) == null) {
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
		if (getFieldContent(fSessionAddressText.getText()) == null) {
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
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * Note: performApply gets called when either text is modified via updateLaunchConfigurationDialog(). Only update the
		 * configuration if things are valid.
		 */
		if (isValid(configuration)) {
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
					getFieldContent(fSDMBackendCombo.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					getFieldContent(fRMDebuggerPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST,
					getFieldContent(fSessionAddressText.getText()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse. debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		/*
		 * We have just selected SDM as the debugger...
		 */
		configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, EMPTY_STRING);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST);
	}

	/**
	 * Browse for a file.
	 * 
	 * @return path to file selected in browser
	 */
	private String browseFile() {
		if (fRemoteConnection != null) {
			IRemoteUIServices remoteUISrv = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(
					fRemoteConnection.getRemoteServices());
			if (remoteUISrv != null) {
				IRemoteUIFileManager fileManager = remoteUISrv.getUIFileManager();
				if (fileManager != null) {
					fileManager.setConnection(fRemoteConnection);
					return fileManager.browseFile(getShell(), Messages.SDMPage_10, fRMDebuggerPathText.getText(), 0);
				}
			}
		}

		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages.SDMPage_10);
		dialog.setFileName(fRMDebuggerPathText.getText());
		return dialog.open();
	}

	/**
	 * Helper method to locate the remote connection used by the launch configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @throws CoreException
	 */
	private IRemoteConnection getRemoteConnection(ILaunchConfiguration configuration) {
		try {
			final String remId = configuration
					.getAttribute(IPTPLaunchConfigurationConstants.ATTR_REMOTE_SERVICES_ID, (String) null);
			if (remId != null) {
				final IRemoteServices[] services = new IRemoteServices[1];
				try {
					getLaunchConfigurationDialog().run(false, true, new IRunnableWithProgress() {
						public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
							services[0] = PTPRemoteCorePlugin.getDefault().getRemoteServices(remId, monitor);
						}
					});
				} catch (InvocationTargetException e) {
				} catch (InterruptedException e) {
				}
				if (services[0] != null) {
					String name = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
					if (name != null) {
						return services[0].getConnectionManager().getConnection(name);
					}
				}
			}
		} catch (CoreException e) {
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
