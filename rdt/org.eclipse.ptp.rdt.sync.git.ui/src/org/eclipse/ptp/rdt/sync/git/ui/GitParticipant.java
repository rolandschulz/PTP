/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.git.ui;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.ptp.rdt.sync.core.CommandRunner;
import org.eclipse.ptp.rdt.sync.core.CommandRunner.CommandResults;
import org.eclipse.ptp.rdt.sync.core.RemoteExecutionException;
import org.eclipse.ptp.rdt.sync.core.RemoteSyncException;
import org.eclipse.ptp.rdt.sync.core.serviceproviders.ISyncServiceProvider;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.git.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.git.core.GitServiceProvider;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.ServiceModelManager;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Launches a dialog that configures a remote sync target with OK and Cancel
 * buttons. Also has a text field to allow the name of the configuration to be
 * changed.
 */
public class GitParticipant implements ISynchronizeParticipant {
	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	// private IServiceConfiguration fConfig;
	private IRemoteConnection fSelectedConnection;
	private IRemoteServices fSelectedProvider;
	// private final IRunnableContext fContext;
	private String fProjectName = ""; //$NON-NLS-1$

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();
	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();

//	private Control fDialogControl;
//	private Point fDialogSize;
//	private Text fNameText;
	private Button fRemoteLocationBrowseButton;
	private Button fGitLocationBrowseButton;
	private Button fUseGitDefaultLocationButton;
	private Button fNewConnectionButton;
	private Combo fProviderCombo;
	private Combo fConnectionCombo;
	private Text fLocationText;
	private Text fGitLocationText;

	private IWizardContainer container;
	
	// If false, automatically select "Remote Tools" provider instead of letting the user select the provider.
	private boolean showProviderCombo = false;

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), null, fSelectedConnection);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#createConfigurationArea
	 * (org.eclipse.swt.widgets.Composite,
	 * org.eclipse.jface.operation.IRunnableContext)
	 */
	public void createConfigurationArea(Composite parent, IRunnableContext context) {
		this.container = (IWizardContainer)context;
		final Composite configArea = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		configArea.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		configArea.setLayoutData(gd);

		if (showProviderCombo) {
			// Label for "Provider:"
			Label providerLabel = new Label(configArea, SWT.LEFT);
			providerLabel.setText(Messages.GitParticipant_remoteProvider);

			// combo for providers
			fProviderCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
			gd = new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false);
			gd.horizontalSpan = 2;
			fProviderCombo.setLayoutData(gd);
			fProviderCombo.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleServicesSelected();
				}
			});
		}

		// attempt to restore settings from saved state
		// IRemoteServices providerSelected = fProvider.getRemoteServices();

		// populate the combo with a list of providers
		IRemoteServices[] providers = PTPRemoteUIPlugin.getDefault().getRemoteServices(context);
		int toSelect = 0;

		for (int k = 0; k < providers.length; k++) {
			if (showProviderCombo) {
				fProviderCombo.add(providers[k].getName(), k);
			}
			if  (providers[k].getName().equals("Remote Tools")) { //$NON-NLS-1$
				toSelect = k;
			}
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);
		}

		if (showProviderCombo) {
			fProviderCombo.select(toSelect);
		}
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(toSelect);

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(configArea, SWT.LEFT);
		connectionLabel.setText(Messages.GitParticipant_connection);

		// combo for providers
		fConnectionCombo = new Combo(configArea, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fConnectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fConnectionCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		// populate the combo with a list of providers
		populateConnectionCombo(fConnectionCombo);

		// new connection button
		fNewConnectionButton = new Button(configArea, SWT.PUSH);
		fNewConnectionButton.setText(Messages.GitParticipant_new);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		fNewConnectionButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
				if (connectionManager != null) {
					connectionManager.newConnection(fNewConnectionButton.getShell());
				}
				// refresh list of connections
				populateConnectionCombo(fConnectionCombo);
				update();
			}
		});

		// Remote directory location
		// Label for "Remote directory:"
		Label locationLabel = new Label(configArea, SWT.LEFT);
		locationLabel.setText(Messages.GitParticipant_location);

		// Remote directory textbox
		fLocationText = new Text(configArea, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fLocationText.setLayoutData(gd);
		fLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				// MBSCustomPageManager.addPageProperty(REMOTE_SYNC_WIZARD_PAGE_ID,setGitLocation
				// PATH_PROPERTY, fLocationText.getText());
				update();
			}
		});
		
		// Remote location browse button
		fRemoteLocationBrowseButton = new Button(configArea, SWT.PUSH);
		fRemoteLocationBrowseButton.setText(Messages.GitParticipant_browse);
		fRemoteLocationBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider);
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String correctPath = fLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});
		
		// Git location
		// "Use default location" button
		fUseGitDefaultLocationButton = new Button(configArea, SWT.CHECK);
		fUseGitDefaultLocationButton.setText("Use default Git location");
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		fUseGitDefaultLocationButton.setLayoutData(gd);
		fUseGitDefaultLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setGitLocation();
			}
		});
		fUseGitDefaultLocationButton.setSelection(true);

		// Git location label
		Label gitLocationLabel = new Label(configArea, SWT.NONE);
		gitLocationLabel.setText("Git location: ");

		// Git location entry field
		fGitLocationText = new Text(configArea, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fGitLocationText.setLayoutData(gd);
		fGitLocationText.setEnabled(false);
		
		// Git location browse button
		fGitLocationBrowseButton = new Button(configArea, SWT.PUSH);
		fGitLocationBrowseButton.setText(Messages.GitParticipant_browse);
		fGitLocationBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider);
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String selectedPath = fileMgr.browseFile(
										fGitLocationText.getShell(),
										"Project Location (" + fSelectedConnection.getName() + ")", null, IRemoteUIConstants.NONE); //$NON-NLS-1$ //$NON-NLS-2$
								if (selectedPath != null) {
									fGitLocationText.setText(selectedPath);
								}
							}
						}
					}
				}
			}
		});
		
		handleConnectionSelected();
	}

	/**
	 * Return the path we are going to display. If it is a file URI then remove
	 * the file prefix.
	 * 
	 * Only do this if the connection is open. Otherwise we will attempt to
	 * connect to the first machine in the list, which is annoying.
	 * 
	 * @return String
	 */
	private String getDefaultPathDisplayString() {
//		String projectName = ""; //$NON-NLS-1$
		// IWizardPage page = getWizard().getStartingPage();
		// if (page instanceof CDTMainWizardPage) {
		// projectName = ((CDTMainWizardPage) page).getProjectName();
		// }
		if (fSelectedConnection != null && fSelectedConnection.isOpen()) {
			IRemoteFileManager fileMgr = fSelectedProvider.getFileManager(fSelectedConnection);
			URI defaultURI = fileMgr.toURI(fSelectedConnection.getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
				return Platform.getLocation().append(fProjectName).toString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(fProjectName).toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	/** 
	 * @see ISynchronizeParticipant#getErrorMessage()
	 */
	public String getErrorMessage() {
		if (fSelectedProvider == null) 
			return Messages.GitParticipant_0;
		if (fSelectedConnection == null) 
			return Messages.GitParticipant_1;
		if (fLocationText.getText().length() == 0 ) 
			return Messages.GitParticipant_2;
		IRemoteFileManager fileManager = fSelectedProvider.getFileManager(fSelectedConnection);
		if (fileManager.toURI(fLocationText.getText()) == null)
			return Messages.GitParticipant_3;
		if (fileManager.toURI(fGitLocationText.getText()) == null)
			return "invalid Git path";
		// should we check permissions of: fileManager.getResource(fLocationText.getText()).getParent() ?
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#getProvider(org.eclipse.core.resources.IProject)
	 */
	public ISyncServiceProvider getProvider(IProject project) {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		IService syncService = smm.getService(IRemoteSyncServiceConstants.SERVICE_SYNC);
		GitServiceProvider provider = (GitServiceProvider) smm.getServiceProvider(syncService
				.getProviderDescriptor(GitServiceProvider.ID));
		provider.setLocation(fLocationText.getText());
		provider.setToolLocation(fGitLocationText.getText());
		provider.setRemoteConnection(fSelectedConnection);
		provider.setRemoteServices(fSelectedProvider);
		return provider;
	}

	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider)
				.getUIConnectionManager();
		return connectionManager;
	}

	/**
	 * Handle new connection selected
	 */
	private void handleConnectionSelected() {
		int selectionIndex = fConnectionCombo.getSelectionIndex();
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		fLocationText.setText(getDefaultPathDisplayString());
		// Assume users want to select this too whenever they change the connection
		fUseGitDefaultLocationButton.setSelection(true);
		this.setGitLocation();
		update();
	}

	/**
	 * Handle new remote services selected
	 */
	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		handleConnectionSelected();
		update();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#isConfigComplete()
	 */
	public boolean isConfigComplete() {
		return getErrorMessage() == null;
	}

	/**
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();

		IRemoteConnection[] connections = fSelectedProvider.getConnectionManager().getConnections();

		for (int k = 0; k < connections.length; k++) {
			connectionCombo.add(connections[k].getName(), k);
			fComboIndexToRemoteConnectionMap.put(k, connections[k]);
		}

		connectionCombo.select(0);
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(0);
	}

	private void update() {
		container.updateMessage();
		// updateButtons() may fail if current page is null. This can happen if update() is called during wizard/page creation. 
		if (container.getCurrentPage() == null) {
			return;
		}
		container.updateButtons();
	}

	/**
	 * @param button
	 */
	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant#setProjectName(String projectName)
	 */
	public void setProjectName(String projectName) {
		fProjectName = projectName;
		fLocationText.setText(getDefaultPathDisplayString());
	}
	
	// Fill in Git location text and set the related UI elements.
	private void setGitLocation() {
		// If "use default" not selected - enable and clear textbox only if not yet enabled.
		if (!fUseGitDefaultLocationButton.getSelection()) {
			if (!fGitLocationText.isEnabled()) {
				fGitLocationText.setText(""); //$NON-NLS-1$
				fGitLocationText.setEnabled(true);
			}
		// Otherwise, ask remote machine for location.
		// Textbox must be either enabled or disabled depending on if the request is successful.
		} else {
			List<String> args = Arrays.asList("which", "git");
			String errorMessage;
			CommandResults cr = null;
			try {
				cr = this.runRemoteCommand(args);
				errorMessage = this.buildErrorMessage(cr, "Unable to find Git on remote", null);
			} catch (RemoteExecutionException e) {
				errorMessage = this.buildErrorMessage(null, "Unable to find Git on remote", e);
			}

			// Unable to find Git location
			if (errorMessage != null) {
				fGitLocationText.setText("");
				fUseGitDefaultLocationButton.setSelection(false);
				fGitLocationText.setEnabled(true);
				MessageDialog.openError(null, "Remote Execution", errorMessage);
			// Git location found
			} else {
				fGitLocationText.setText(cr.getStdout().trim());
				fGitLocationText.setEnabled(false);
			}
		}
	}
	
	// Wrapper for using command runner - primarily wrapping all of the exceptions.
	private CommandResults runRemoteCommand(List<String> command) throws RemoteExecutionException {
		try {
			return CommandRunner.executeRemoteCommand(fSelectedConnection, command, fLocationText.getText(), null);
		} catch (RemoteSyncException e) {
			throw new RemoteExecutionException(e);
		} catch (IOException e) {
			throw new RemoteExecutionException(e);
		} catch (InterruptedException e) {
			throw new RemoteExecutionException(e);
		} catch (RemoteConnectionException e) {
			throw new RemoteExecutionException(e);
		}
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
			return baseMessage + ": " + cr.getStderr();
		}

		// Command did not run - exception thrown
		String errorMessage = baseMessage;
		if (e.getMessage() != null) {
			errorMessage += ": " + e.getMessage();
		}

		return errorMessage;
	}
}
