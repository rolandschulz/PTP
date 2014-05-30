/*******************************************************************************
 * Copyright (c) 2013 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.git.ui.preferences;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner;
import org.eclipse.ptp.internal.rdt.sync.git.core.CommandRunner.CommandResults;
import org.eclipse.ptp.internal.rdt.sync.git.ui.Activator;
import org.eclipse.ptp.internal.rdt.sync.git.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.exceptions.RemoteSyncException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConstants;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
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
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

public class SyncGitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String instanceScopeSyncNode = "org.eclipse.ptp.rdt.sync.core"; //$NON-NLS-1$
	private static final String GIT_LOCATION_NODE_NAME = "git-location"; //$NON-NLS-1$

	Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	Map<IRemoteConnection, String> fConnectionNameToGitPathMap = new HashMap<IRemoteConnection, String>();
	IRemoteConnection fSelectedConnection = null;
	String pathWarningMessage = null;
	String pathErrorMessage = null;
	String gitWarningMessage = null;
	String gitErrorMessage = null;

	private Composite composite;
	private Combo fConnectionCombo;
	private Button fUseDefaultGitLocationCheckbox;
	private Text fGitLocationText;
	private Button fBrowseButton;
	private Button fGitValidateButton;

	@Override
	protected Control createContents(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		composite.setLayout(layout);

		fConnectionCombo = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		GridData gd = new GridData();
		gd.horizontalSpan = 2;
		fConnectionCombo.setLayoutData(gd);
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
				update();
			}
		});

		fUseDefaultGitLocationCheckbox = new Button(composite, SWT.CHECK);
		gd = new GridData();
		gd.horizontalSpan = 2;
		fUseDefaultGitLocationCheckbox.setLayoutData(gd);
		fUseDefaultGitLocationCheckbox.setText(Messages.SyncGitPreferencePage_0);
		fUseDefaultGitLocationCheckbox.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleCheckDefaultGitLocation();
				update();
			}
		});

		fGitLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGitLocationText.setLayoutData(gd);
		fGitLocationText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				update();
			}
		});

		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.SyncGitPreferencePage_1);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection == null) {
					return;
				}
				if (!fSelectedConnection.isOpen()) {
					IRemoteUIConnectionManager mgr = getUIConnectionManager();
					if (mgr != null) {
						mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
					}
				}
				if (!fSelectedConnection.isOpen()) {
					return;
				}
				IRemoteUIServices remoteUIServices = RemoteUIServices.getRemoteUIServices(fSelectedConnection.getRemoteServices());
				if (remoteUIServices == null) {
					return;
				}
				IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
				if (fileMgr == null) {
					return;
				}
				fileMgr.setConnection(fSelectedConnection);
				String selectedPath = fileMgr.browseFile(fGitLocationText.getShell(), Messages.SyncGitPreferencePage_3
						+ fSelectedConnection.getName() + ")", null, //$NON-NLS-1$
						IRemoteUIConstants.NONE);
				if (selectedPath != null) {
					fGitLocationText.setText(selectedPath);
				}
			}
		});

		fGitValidateButton = new Button(composite, SWT.PUSH);
		gd = new GridData();
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.RIGHT;
		fGitValidateButton.setLayoutData(gd);
		fGitValidateButton.setText(Messages.SyncGitPreferencePage_15);
		fGitValidateButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				validateGit();
				update();
			}
		});

		this.populateConnectionCombo(fConnectionCombo);
		this.handleConnectionSelected();

		return composite;
	}

	@Override
	public void init(IWorkbench arg0) {
		// nothing to do
	}

	public void update() {
		this.validatePath();
		this.validatePage();
		getContainer().updateMessage();
		getContainer().updateButtons();
		updateApplyButton();
	}

	@Override
	public void performApply() {
		this.storeConnectionSettings();
		this.saveAllConnectionSettings();
	}

	@Override
	protected void performDefaults() {
		fConnectionNameToGitPathMap.clear();
		this.loadConnectionSettings();
	}

	@Override
	public boolean performOk() {
		this.performApply();
		return true;
	}

	@Override
	public String getMessage() {
		if (pathWarningMessage != null) {
			return pathWarningMessage;
		} else {
			return gitWarningMessage;
		}
	}

	@Override
	public int getMessageType() {
		// Currently all messages are warning messages.
		return IMessageProvider.WARNING;
	}

	@Override
	public String getErrorMessage() {
		if (pathErrorMessage != null) {
			return pathErrorMessage;
		} else {
			return gitErrorMessage;
		}
	}

	private void handleConnectionSelected() {
		this.storeConnectionSettings();
		this.clearMessages();
		int sel = fConnectionCombo.getSelectionIndex();
		if (sel == -1) {
			fSelectedConnection = null;
		} else {
			fSelectedConnection = fComboIndexToRemoteConnectionMap.get(sel);
		}
		this.loadConnectionSettings();
	}

	// Save settings to internal map for currently selected connection
	private void storeConnectionSettings() {
		if (fSelectedConnection == null) {
			return;
		}
		if (fUseDefaultGitLocationCheckbox.getSelection()) {
			fConnectionNameToGitPathMap.put(fSelectedConnection, null);
		} else {
			// TODO: Verify that path is valid.
			fConnectionNameToGitPathMap.put(fSelectedConnection, fGitLocationText.getText());
		}
	}

	// Load settings to UI elements for currently selected connection
	private void loadConnectionSettings() {
		if (fSelectedConnection == null) {
			return;
		}

		String gitBinary = null;
		// Try retrieving settings from map
		if (fConnectionNameToGitPathMap.containsKey(fSelectedConnection)) {
			gitBinary = fConnectionNameToGitPathMap.get(fSelectedConnection);

			// If not there, load from preference store
		} else {
			IScopeContext context = InstanceScope.INSTANCE;
			Preferences prefSyncNode = context.getNode(instanceScopeSyncNode);
			if (prefSyncNode == null) {
				Activator.log(Messages.SyncGitPreferencePage_18);
			} else {
				try {
					// Avoid creating node if it doesn't exist
					if (prefSyncNode.nodeExists(GIT_LOCATION_NODE_NAME)) {
						Preferences prefGitNode = prefSyncNode.node(GIT_LOCATION_NODE_NAME);
						gitBinary = prefGitNode.get(fSelectedConnection.getName(), null);
					}
				} catch (BackingStoreException e) {
					Activator.log(Messages.SyncGitPreferencePage_19, e);
				}
			}
		}

		// Set UI elements
		if (gitBinary == null) {
			fUseDefaultGitLocationCheckbox.setSelection(true);
			this.handleCheckDefaultGitLocation();
			fGitLocationText.setText(""); //$NON-NLS-1$
		} else {
			fUseDefaultGitLocationCheckbox.setSelection(false);
			this.handleCheckDefaultGitLocation();
			fGitLocationText.setText(gitBinary);
		}
	}

	private void clearMessages() {
		pathWarningMessage = null;
		pathErrorMessage = null;
		gitWarningMessage = null;
		gitErrorMessage = null;
	}

	private void handleCheckDefaultGitLocation() {
		if (fUseDefaultGitLocationCheckbox.getSelection()) {
			fGitLocationText.setEnabled(false);
			fBrowseButton.setEnabled(false);
		} else {
			fGitLocationText.setEnabled(true);
			fBrowseButton.setEnabled(true);
		}
	}

	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();

		// TODO: Handle case where service provider is not found.
		IRemoteServices rs = this.getRemoteServicesProvider();
		List<IRemoteConnection> connections = rs.getConnectionManager().getConnections();

		fComboIndexToRemoteConnectionMap.clear();
		for (int i = 0; i < connections.size(); i++) {
			connectionCombo.add(connections.get(i).getName(), i);
			fComboIndexToRemoteConnectionMap.put(i, connections.get(i));
		}

		if (connections.size() > 0) {
			connectionCombo.select(0);
		}
	}

	// Return the remote service to use or null if it cannot be found.
	// Currently, we simply always return the JSch provider, which *should* always be available.
	IRemoteServices remoteServicesProvider = null;

	private IRemoteServices getRemoteServicesProvider() {
		if (remoteServicesProvider == null) {
			remoteServicesProvider = RemoteServices.getRemoteServices("org.eclipse.remote.JSch"); //$NON-NLS-1$
		}
		return remoteServicesProvider;
	}

	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteServices rs = this.getRemoteServicesProvider();
		IRemoteUIConnectionManager connectionManager = RemoteUIServices.getRemoteUIServices(rs).getUIConnectionManager();
		return connectionManager;
	}

	// Basic path checks that do not require running remote commands
	private boolean validatePath() {
		pathErrorMessage = null;
		pathWarningMessage = null;
		if (fUseDefaultGitLocationCheckbox.getSelection()) {
			return true;
		}
		IPath gitPath = new Path(fGitLocationText.getText());
		if (gitPath.isEmpty()) {
			pathErrorMessage = Messages.SyncGitPreferencePage_4;
			return false;
		}
		if (!gitPath.isAbsolute()) {
			pathErrorMessage = Messages.SyncGitPreferencePage_5;
			return false;
		}
		return true;
	}

	// Deeper path validation that uses Git on the remote machine
	private boolean validateGit() {
		gitErrorMessage = null;
		gitWarningMessage = null;
		if (!this.validatePath()) {
			return false;
		}

		// If use default is selected, assume the user wants to retrieve and validate the system default Git.
		if (fUseDefaultGitLocationCheckbox.getSelection()) {
			gitErrorMessage = this.setSystemDefaultGit();
			if (gitErrorMessage != null) {
				return false;
			}
		}
		// Otherwise, assume the user wants to validate whatever is in the textbox.
		IPath gitPath;
		gitPath = new Path(fGitLocationText.getText());
		List<String> args = Arrays.asList(gitPath.toString(), "--version"); //$NON-NLS-1$
		String errorMessage = null;
		CommandResults cr = null;
		try {
			cr = this.runRemoteCommand(args, Messages.SyncGitPreferencePage_6);
		} catch (RemoteExecutionException e) {
			errorMessage = this.buildErrorMessage(null, Messages.SyncGitPreferencePage_7, e);
		}

		if (errorMessage != null) {
			MessageDialog.openError(null, Messages.SyncGitPreferencePage_8, errorMessage);
			gitErrorMessage = Messages.SyncGitPreferencePage_7;
			return false;
		}
		if (cr.getExitCode() == 126) {
			gitErrorMessage = Messages.SyncGitPreferencePage_9;
			return false;
		}
		if (cr.getExitCode() == 127) {
			gitErrorMessage = Messages.SyncGitPreferencePage_10;
			return false;
		}

		int version = parseGitVersionAsInt(cr.getStdout());
		String versionString = parseGitVersionAsString(cr.getStdout());
		if (cr.getExitCode() != 0 || version == 0) {
			gitWarningMessage = Messages.SyncGitPreferencePage_11;
			return true;
		}

		if (version < 10700) {
			gitWarningMessage = Messages.SyncGitPreferencePage_12 + versionString + Messages.SyncGitPreferencePage_13;
		}

		// Prefer false positives to false negatives. Return true by default.
		return true;
	}

	// Retrieve system default Git and place in textbox
	// Return error message
	private String setSystemDefaultGit() {
		List<String> args = Arrays.asList("which", "git"); //$NON-NLS-1$ //$NON-NLS-2$
		String errorMessage = null;
		CommandResults cr = null;
		try {
			cr = this.runRemoteCommand(args, Messages.SyncGitPreferencePage_16);
			errorMessage = this.buildErrorMessage(cr, Messages.SyncGitPreferencePage_17, null);
		} catch (RemoteExecutionException e) {
			errorMessage = this.buildErrorMessage(null, Messages.SyncGitPreferencePage_17, e);
		}

		if (errorMessage == null) {
			fGitLocationText.setText(cr.getStdout().trim());
		} else {
			fGitLocationText.setText(""); //$NON-NLS-1$
		}
		return errorMessage;
	}

	// Decide if page is valid and alter UI elements accordingly.
	private void validatePage() {
		boolean isValid = (pathErrorMessage == null);
		if (isValid) {
			fConnectionCombo.setEnabled(true);
		} else {
			fConnectionCombo.setEnabled(false);
		}
	}

	// Wrapper for running commands - wraps exceptions and invoking of command runner inside container run command.
	private CommandResults remoteCommandResults;

	private CommandResults runRemoteCommand(final List<String> command, final String commandDesc) throws RemoteExecutionException {
		try {
			new ProgressMonitorDialog(composite.getShell()).run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					RecursiveSubMonitor progress = RecursiveSubMonitor.convert(monitor, 100);
					progress.subTask(commandDesc);
					try {
						remoteCommandResults = CommandRunner.executeRemoteCommand(fSelectedConnection, command, null,
								progress.newChild(100));
					} catch (RemoteSyncException e) {
						throw new InvocationTargetException(e);
					} catch (IOException e) {
						throw new InvocationTargetException(e);
					} catch (InterruptedException e) {
						throw new InvocationTargetException(e);
					} catch (RemoteConnectionException e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			});
		} catch (InvocationTargetException e) {
			throw new RemoteExecutionException(e.getCause());
		} catch (InterruptedException e) {
			throw new RemoteExecutionException(e);
		}
		return remoteCommandResults;
	}

	// Builds error message for command.
	// Either the command result or the exception should be null, but not both.
	// baseMessage cannot be null.
	// Returns error message or null if no error occurred (can only occur if cr is not null).
	private String buildErrorMessage(CommandResults cr, String baseMessage, RemoteExecutionException e) {
		// Command successful
		if (cr != null && cr.getExitCode() == 0) {
			return null;
		}

		// Command runs but unsuccessfully
		if (cr != null) {
			return baseMessage + ": " + cr.getStderr(); //$NON-NLS-1$
		}

		// Command did not run - exception thrown
		String errorMessage = baseMessage;
		if (e.getMessage() != null) {
			errorMessage += ": " + e.getMessage(); //$NON-NLS-1$
		}

		return errorMessage;
	}

	private void saveAllConnectionSettings() {
		if (fConnectionNameToGitPathMap.size() == 0) {
			return;
		}

		IScopeContext context = InstanceScope.INSTANCE;
		Preferences prefSyncNode = context.getNode(instanceScopeSyncNode);
		if (prefSyncNode == null) {
			Activator.log(Messages.SyncGitPreferencePage_20);
			return;
		}

		// Avoid creating node if not needed. Connections set to default (null) are not stored.
		Preferences prefGitNode = null;
		try {
			if (prefSyncNode.nodeExists(GIT_LOCATION_NODE_NAME)) {
				prefGitNode = prefSyncNode.node(GIT_LOCATION_NODE_NAME);
			}
		} catch (BackingStoreException e) {
			Activator.log(Messages.SyncGitPreferencePage_21, e);
		}

		for (Map.Entry<IRemoteConnection, String> entry : fConnectionNameToGitPathMap.entrySet()) {
			// Avoid creating node if not necessary. Connections set to default (null) are not stored.
			if (prefGitNode == null) {
				if (entry.getValue() == null) {
					continue;
				} else {
					prefGitNode = prefSyncNode.node(GIT_LOCATION_NODE_NAME);
				}
			}

			if (entry.getValue() == null) {
				prefGitNode.remove(entry.getKey().getName());
			} else {
				prefGitNode.put(entry.getKey().getName(), entry.getValue());
			}
		}

		try {
			prefSyncNode.flush();
		} catch (BackingStoreException e) {
			Activator.log(e);
		}
	}

	/**
	 * Parse raw output of "git --version" and return an integer representation of the version, suitable for comparisons.
	 * 
	 * @param versionCommandOutput
	 * @return version integer or 0 on failure to parse
	 */
	public static int parseGitVersionAsInt(String versionCommandOutput) {
		Matcher m = Pattern.compile("git version ([0-9]+)\\.([0-9]+)\\.([0-9]+).*").matcher(versionCommandOutput.trim()); //$NON-NLS-1$
		if (m.matches()) {
			return Integer.parseInt(m.group(1)) * 10000 + Integer.parseInt(m.group(2)) * 100 + Integer.parseInt(m.group(3));
		} else {
			return 0;
		}
	}

	/**
	 * Parse raw output of "git --version" and return the version string, suitable for displaying to users.
	 * 
	 * @param versionCommandOutput
	 * @return version string or null on failure to parse
	 */
	public static String parseGitVersionAsString(String versionCommandOutput) {
		Matcher m = Pattern.compile("git version ([0-9]+\\.[0-9]+\\.[0-9]+).*").matcher(versionCommandOutput.trim()); //$NON-NLS-1$
		if (m.matches()) {
			return m.group(1);
		} else {
			return null;
		}
	}
}