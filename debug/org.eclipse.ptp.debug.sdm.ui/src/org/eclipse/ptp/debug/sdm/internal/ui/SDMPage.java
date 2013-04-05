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
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.RemoteUIServices;
import org.eclipse.ptp.ui.preferences.ScrolledPageContent;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;

/**
 * The dynamic tab for SDM-based debugger implementations.
 */
public class SDMPage extends AbstractLaunchConfigurationTab {
	protected static final String EXTENSION_POINT_ID = "defaultPath"; //$NON-NLS-1$
	protected static final String ATTR_PATH = "path"; //$NON-NLS-1$

	protected static final String EMPTY_STRING = ""; //$NON-NLS-1$
	protected static final String LOCALHOST = "localhost"; //$NON-NLS-1$

	private IRemoteConnection fRemoteConnection;

	protected Combo fSDMBackendCombo;
	protected Text fSDMPathText;
	protected Text fSessionAddressText;
	protected Text fBackendPathText;
	protected Button fSDMPathBrowseButton;
	protected Button fBackendPathBrowseButton;
	protected Button fDefaultSessionAddressButton;
	protected ExpandableComposite fAdvancedOptions;

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
		} else if (getFieldContent(fSDMPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_8);
		}
		// setErrorMessage(errMsg);
		return (getErrorMessage() == null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		ScrolledPageContent pageContent = new ScrolledPageContent(parent);
		pageContent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite comp = pageContent.getBody();
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
		fSDMBackendCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateBackend();
				updateLaunchConfigurationDialog();
			}
		});

		label = new Label(comp, SWT.NONE);
		label.setText(Messages.SDMPage_0);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		label.setLayoutData(gd);

		fSDMPathText = new Text(comp, SWT.SINGLE | SWT.BORDER);
		fSDMPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fSDMPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fSDMPathBrowseButton = createPushButton(comp, Messages.SDMPage_1, null);
		fSDMPathBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fSDMPathBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseFile();
				if (file != null) {
					fSDMPathText.setText(file);
				}
			}
		});

		/*
		 * Advanced options
		 */
		fAdvancedOptions = new ExpandableComposite(comp, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		fAdvancedOptions.setText(Messages.SDMPage_12);
		fAdvancedOptions.addExpansionListener(new ExpansionAdapter() {
			@Override
			public void expansionStateChanged(ExpansionEvent e) {
				ScrolledPageContent parent = getParentScrolledComposite((ExpandableComposite) e.getSource());
				if (parent != null) {
					parent.reflow(true);
				}
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		fAdvancedOptions.setLayoutData(gd);

		Composite advComp = new Composite(fAdvancedOptions, SWT.NONE);
		fAdvancedOptions.setClient(advComp);
		advComp.setLayout(new GridLayout(3, false));
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		advComp.setLayoutData(gd);

		Group sessGroup = new Group(advComp, SWT.SHADOW_ETCHED_IN);
		sessGroup.setLayout(new GridLayout(2, false));
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 3;
		sessGroup.setLayoutData(gd);

		fDefaultSessionAddressButton = createCheckButton(sessGroup, Messages.SDMPage_Use_default_session_address);
		fDefaultSessionAddressButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateEnablement();
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		gd.horizontalSpan = 2;
		fDefaultSessionAddressButton.setLayoutData(gd);
		fDefaultSessionAddressButton.setSelection(true);

		label = new Label(sessGroup, SWT.NONE);
		label.setText(Messages.SDMPage_Session_address);
		gd = new GridData(SWT.FILL, SWT.BEGINNING, false, false);
		label.setLayoutData(gd);

		fSessionAddressText = new Text(sessGroup, SWT.SINGLE | SWT.BORDER);
		fSessionAddressText.setLayoutData(new GridData(SWT.FILL, SWT.BEGINNING, true, false));
		fSessionAddressText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});
		gd = new GridData(SWT.FILL, SWT.BEGINNING, true, false);
		fSessionAddressText.setLayoutData(gd);

		label = new Label(advComp, SWT.NONE);
		label.setText(Messages.SDMPage_13);
		gd = new GridData(SWT.FILL, SWT.CENTER, false, false);
		label.setLayoutData(gd);

		fBackendPathText = new Text(advComp, SWT.SINGLE | SWT.BORDER);
		fBackendPathText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fBackendPathText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateLaunchConfigurationDialog();
			}
		});

		fBackendPathBrowseButton = createPushButton(advComp, Messages.SDMPage_1, null);
		fBackendPathBrowseButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		fBackendPathBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				String file = browseFile();
				if (file != null) {
					fBackendPathText.setText(file);
				}
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
			String backend = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.PREFS_SDM_BACKEND);
			fSDMBackendCombo
					.setText(configuration.getAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND, backend));
			fSessionAddressText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST));
			fSDMPathText.setText(configuration
					.getAttribute(
							IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
							Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_PATH
									+ backend)));
			fBackendPathText.setText(configuration.getAttribute(
					SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH,
					Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH
							+ backend)));
			fRemoteConnection = getRemoteConnection(configuration);
			fDefaultSessionAddressButton.setSelection(fRemoteConnection == null
					|| (fRemoteConnection.supportsTCPPortForwarding() && fSessionAddressText.getText().equals(LOCALHOST)));
			updateEnablement();
		} catch (CoreException e) {
			// Ignore
		}
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
		} else if (getFieldContent(fSDMPathText.getText()) == null) {
			setErrorMessage(Messages.SDMPage_5);
		}
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
			configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH,
					getFieldContent(fBackendPathText.getText()));
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
					getFieldContent(fSDMPathText.getText()));
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
		String backend = Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND);
		configuration.setAttribute(SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND, backend);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_PATH + backend));
		configuration.setAttribute(
				SDMLaunchConfigurationConstants.ATTR_DEBUGGER_SDM_BACKEND_PATH,
				Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(), SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH
						+ backend));
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, LOCALHOST);
	}

	/**
	 * Browse for a file.
	 * 
	 * @return path to file selected in browser
	 */
	private String browseFile() {
		if (fRemoteConnection != null) {
			IRemoteUIServices remoteUISrv = RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
			if (remoteUISrv != null) {
				IRemoteUIFileManager fileManager = remoteUISrv.getUIFileManager();
				if (fileManager != null) {
					fileManager.setConnection(fRemoteConnection);
					return fileManager.browseFile(getShell(), Messages.SDMPage_10, fSDMPathText.getText(), 0);
				}
			}
		}

		FileDialog dialog = new FileDialog(getShell());
		dialog.setText(Messages.SDMPage_10);
		dialog.setFileName(fSDMPathText.getText());
		return dialog.open();
	}

	private ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent = control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent = parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
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
							services[0] = RemoteServices.getRemoteServices(remId, monitor);
						}
					});
				} catch (InvocationTargetException e) {
					// Ignore
				} catch (InterruptedException e) {
					// Ignore
				}
				if (services[0] != null) {
					String name = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONNECTION_NAME, (String) null);
					if (name != null) {
						return services[0].getConnectionManager().getConnection(name);
					}
				}
			}
		} catch (CoreException e) {
			// Ignore
		}
		return null;
	}

	private void updateBackend() {
		String backend = getFieldContent(fSDMBackendCombo.getText());
		if (backend != null) {
			fSDMPathText.setText(Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.PREFS_SDM_PATH + backend));
			fBackendPathText.setText(Preferences.getString(SDMDebugCorePlugin.getUniqueIdentifier(),
					SDMPreferenceConstants.PREFS_SDM_BACKEND_PATH + backend));
		}
	}

	private void updateEnablement() {
		fSessionAddressText.setEnabled(!fDefaultSessionAddressButton.getSelection());
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
