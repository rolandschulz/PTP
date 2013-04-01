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
package org.eclipse.ptp.rdt.sync.ui;

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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.ptp.rdt.sync.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.core.RecursiveSubMonitor;
import org.eclipse.ptp.rdt.sync.core.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.swt.SWT;
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

public class SyncGitPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	private static final String GIT_BINARY_KEY = "git-binary"; //$NON-NLS-1$

	Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();
	Map<IRemoteConnection, String> fConnectionNameToGitPathMap = new HashMap<IRemoteConnection, String>();
	IRemoteConnection fSelectedConnection = null;
	String specificGitWarning;
	String specificGitError;

	private Composite composite;
	private Combo fConnectionCombo;
	private Button fUseDefaultGitLocationCheckbox;
	private Text fGitLocationText;
	private Button fBrowseButton;

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
			}
		});

		fGitLocationText = new Text(composite, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		fGitLocationText.setLayoutData(gd);

		fBrowseButton = new Button(composite, SWT.PUSH);
		fBrowseButton.setText(Messages.SyncGitPreferencePage_1);
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					if(!fSelectedConnection.isOpen()) {
						try {
							fSelectedConnection.open(null);
						} catch (RemoteConnectionException e1) {
							IStatus status = new Status(IStatus.ERROR, "org.eclipse.ptp.rdt.core", Messages.SyncGitPreferencePage_2, e1); //$NON-NLS-1$
							ErrorDialog.openError(getShell(), Messages.SyncGitPreferencePage_2, null, status);
							return;
						}
					}
					IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().
							getRemoteUIServices(fSelectedConnection.getRemoteServices());
					if (remoteUIServices != null) {
						IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
						if (fileMgr != null) {
							fileMgr.setConnection(fSelectedConnection);
							String correctPath = fGitLocationText.getText();
							String selectedPath = fileMgr.browseDirectory(fGitLocationText.getShell(),
									Messages.SyncGitPreferencePage_3 + fSelectedConnection.getName() + ")", correctPath, //$NON-NLS-1$
									IRemoteUIConstants.NONE);
							if (selectedPath != null) {
								fGitLocationText.setText(selectedPath);
							}
						}
					}
				}
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
	
	@Override
	public void performApply() {
		this.saveConnectionSettings();
		for (Map.Entry<IRemoteConnection, String> entry : fConnectionNameToGitPathMap.entrySet()) {
			entry.getKey().setAttribute(GIT_BINARY_KEY, entry.getValue());
		}
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

	private void handleConnectionSelected() {
		this.saveConnectionSettings();
		int sel = fConnectionCombo.getSelectionIndex();
		if (sel == -1) {
			fSelectedConnection = null;
		} else {
			fSelectedConnection = fComboIndexToRemoteConnectionMap.get(sel);
		}
		this.loadConnectionSettings();
	}

	// Save settings to internal map for currently selected connection
	private void saveConnectionSettings() {
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
		String gitDir;
		if (fConnectionNameToGitPathMap.containsKey(fSelectedConnection)) {
			gitDir = fConnectionNameToGitPathMap.get(fSelectedConnection);
		} else {
			gitDir = fSelectedConnection.getAttributes().get(GIT_BINARY_KEY);
		}
		if (gitDir == null) {
			fUseDefaultGitLocationCheckbox.setSelection(true);
			this.handleCheckDefaultGitLocation();
			fGitLocationText.setText(""); //$NON-NLS-1$
		} else {
			fUseDefaultGitLocationCheckbox.setSelection(false);
			this.handleCheckDefaultGitLocation();
			fGitLocationText.setText(gitDir);
		}
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
		IRemoteConnection[] connections = rs.getConnectionManager().getConnections();

		fComboIndexToRemoteConnectionMap.clear();
		for (int i = 0; i < connections.length; i++) {
			connectionCombo.add(connections[i].getName(), i);
			fComboIndexToRemoteConnectionMap.put(i, connections[i]);
		}

		if (connections.length > 0) {
			connectionCombo.select(0);
		}
	}

	// Return the remote service to use or null if it cannot be found.
	// Currently, we simply always return the remote tools provider, which *should* always be available.
	IRemoteServices remoteServicesProvider = null;
	private IRemoteServices getRemoteServicesProvider() {
		if (remoteServicesProvider != null) {
			return remoteServicesProvider;
		}

		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(null);
		for (int i = 0; i < providers.length; i++) {
			if  (providers[i].getName().equals("Remote Tools")) { //$NON-NLS-1$
				remoteServicesProvider = providers[i];
				break;
			}
		}

		return remoteServicesProvider;
	}
	
	// Check if the Git location is valid (does not actually set it as valid).
	// Also sets Git error and warning messages.
	private boolean validateGit() {
		specificGitError = null;
		specificGitWarning = null;
		IPath gitPath = new Path(fGitLocationText.getText());
		if (gitPath.isEmpty()) {
			specificGitError = Messages.SyncGitPreferencePage_4;
			return false;
		}
    	if (!gitPath.isAbsolute()) {
    		specificGitError = Messages.SyncGitPreferencePage_5;
    		return false;
    	}
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
			specificGitError = Messages.SyncGitPreferencePage_7;
			return false;
		} else if (cr.getExitCode() == 126) {
			specificGitError = Messages.SyncGitPreferencePage_9;
			return false;
		} else if (cr.getExitCode() == 127) {
			specificGitError = Messages.SyncGitPreferencePage_10;
			return false;
		}

		int version = parseGitVersionAsInt(cr.getStdout());
		String versionString = parseGitVersionAsString(cr.getStdout());
		if (cr.getExitCode() != 0 || version == 0) {
			specificGitWarning = Messages.SyncGitPreferencePage_11;
			return true;
		}

		if (version < 10600) {
			specificGitWarning = Messages.SyncGitPreferencePage_12 + versionString + Messages.SyncGitPreferencePage_13;
		} else if (version < 10700) {
			specificGitWarning = Messages.SyncGitPreferencePage_12 + versionString + Messages.SyncGitPreferencePage_14;
		}

		// Prefer false positives to false negatives. Return true by default.
		return true;
	}

	// Wrapper for running commands - wraps exceptions and invoking of command runner inside container run command.
	private CommandResults remoteCommandResults;
	private CommandResults runRemoteCommand(final List<String> command, final String commandDesc) throws RemoteExecutionException {
		try {
			// TODO: Figure out where to get a context in which to run
			getContainer().run(true, true, new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException {
					RecursiveSubMonitor progress = RecursiveSubMonitor.convert(monitor, 100);
					progress.subTask(commandDesc);
					try {
						remoteCommandResults = CommandRunner.executeRemoteCommand(fSelectedConnection, command, null, progress.newChild(100));
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

	/**
	 * Parse raw output of "git --version" and return an integer representation of the version, suitable for comparisons.
	 * @param versionCommandOutput
	 * @return version integer or 0 on failure to parse
	 */
	public static int parseGitVersionAsInt(String versionCommandOutput) {
		Matcher m = Pattern.compile("git version ([0-9]+)\\.([0-9]+)\\.([0-9]+).*").matcher(versionCommandOutput.trim()); //$NON-NLS-1$
		if (m.matches()) {
			return Integer.parseInt(m.group(1)) * 10000 +
					Integer.parseInt(m.group(2)) * 100 + Integer.parseInt(m.group(3));
		} else {
			return 0;
		}
	}

	/**
	 * Parse raw output of "git --version" and return the version string, suitable for displaying to users.
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
