/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.internal.ide.IDEWorkbenchMessages;
import org.eclipse.ui.internal.ide.dialogs.IDEResourceInfoUtils;

/**
 * Allows the user to select a provider of Remote Services for a
 * RemoteBuildServiceProvider.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * @author crecoskie
 * @see org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider
 * @since 2.0
 */
public class RemoteProjectContentsLocationArea {

	/**
	 * IErrorMessageReporter is an interface for type that allow message
	 * reporting.
	 * 
	 */
	public interface IErrorMessageReporter {
		/**
		 * Report the error message
		 * 
		 * @param errorMessage
		 *            String or <code>null</code>. If the errorMessage is null
		 *            then clear any error state.
		 * @param infoOnly
		 *            the message is an informational message, but the dialog
		 *            cannot continue
		 * 
		 */
		public void reportError(String errorMessage, boolean infoOnly);
	}

	private static final String FILE_SCHEME = "file"; //$NON-NLS-1$

	private IProject fExistingProject;

	private final IErrorMessageReporter fErrorReporter;

	private RemoteBuildServiceProvider fProvider;

	private final Map<Integer, IRemoteServices> fComboIndexToRemoteServicesProviderMap = new HashMap<Integer, IRemoteServices>();

	private IRemoteServices fSelectedProvider;

	private final Map<Integer, IRemoteConnection> fComboIndexToRemoteConnectionMap = new HashMap<Integer, IRemoteConnection>();

	private IRemoteConnection fSelectedConnection;

	private String fProjectName = IDEResourceInfoUtils.EMPTY_STRING;

	private Button fBrowseButton;

	private Button fNewConnectionButton;

	private Combo fProviderCombo;

	private Combo fConnectionCombo;

	private Text fLocationText;

	// public RemoteProjectContentsLocationArea(IServiceProvider provider,
	// Composite composite) {
	// if(provider instanceof RemoteBuildServiceProvider)
	// fProvider = (RemoteBuildServiceProvider) provider;
	// else
	// throw new IllegalArgumentException(); // should never happen
	// createContents(composite);
	// }
	public RemoteProjectContentsLocationArea(IErrorMessageReporter reporter, Composite composite) {
		fErrorReporter = reporter;
		createContents(composite);
	}

	/**
	 * Check if the entry in the widget location is valid. If it is valid return
	 * null. Otherwise return a string that indicates the problem.
	 * 
	 * @return String
	 */
	public String checkValidLocation() {

		String locationFieldContents = fLocationText.getText();
		if (locationFieldContents.length() == 0) {
			return IDEWorkbenchMessages.WizardNewProjectCreationPage_projectLocationEmpty;
		}

		URI newPath = getProjectLocationURI();
		if (newPath == null) {
			return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationError;
		}

		if (fExistingProject != null) {
			URI projectPath = fExistingProject.getLocationURI();
			if (projectPath != null && URIUtil.equals(projectPath, newPath)) {
				return IDEWorkbenchMessages.ProjectLocationSelectionDialog_locationIsSelf;
			}
		}

		return null;
	}

	/**
	 * Return the browse button. Usually referenced in order to set the layout
	 * data for a dialog.
	 * 
	 * @return Button
	 */
	public Button[] getButtons() {
		return new Button[] { fBrowseButton, fNewConnectionButton };
	}

	/**
	 * Returns the name of the selected connection.
	 */
	public IRemoteConnection getConnection() {
		return fSelectedConnection;
	}

	/**
	 * Return the location for the project.
	 * 
	 * @return String
	 */
	public String getProjectLocation() {
		return fLocationText.getText();
	}

	/**
	 * Get the URI for the location field if possible.
	 * 
	 * @return URI or <code>null</code> if it is not valid.
	 */
	public URI getProjectLocationURI() {
		return fSelectedProvider.getFileManager(fSelectedConnection).toURI(fLocationText.getText());
	}

	public IRemoteConnection getRemoteConnection() {
		return fSelectedConnection;
	}

	public IRemoteServices getRemoteServices() {
		return fSelectedProvider;
	}

	/**
	 * Return whether or not we are currently showing the default location for
	 * the project.
	 * 
	 * @return boolean
	 */
	public boolean isDefault() {
		// return useDefaultsButton.getSelection();
		return false;
	}

	/**
	 * Set the project to base the contents off of.
	 * 
	 * @param existingProject
	 */
	public void setExistingProject(IProject existingProject) {
		fProjectName = existingProject.getName();
		fExistingProject = existingProject;
	}

	/**
	 * Set the text to the default or clear it if not using the defaults.
	 * 
	 * @param newName
	 *            the name of the project to use. If <code>null</code> use the
	 *            existing project name.
	 */
	public void updateProjectName(String newName) {
		fProjectName = newName;
		if (isDefault()) {
			fLocationText.setText(getDefaultPathDisplayString());
		}

	}

	/**
	 * Attempt to open a connection.
	 */
	private void checkConnection() {
		IRemoteUIConnectionManager mgr = getUIConnectionManager();
		if (mgr != null) {
			mgr.openConnectionWithProgress(fConnectionCombo.getShell(), fSelectedConnection);
		}
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
		if (getRemoteConnection() != null && getRemoteConnection().isOpen()) {
			IRemoteFileManager fileMgr = getRemoteServices().getFileManager(getRemoteConnection());
			URI defaultURI = fileMgr.toURI(getRemoteConnection().getWorkingDirectory());

			// Handle files specially. Assume a file if there is no project to
			// query
			if (defaultURI != null && defaultURI.getScheme().equals(FILE_SCHEME)) {
				return Platform.getLocation().append(fProjectName).toOSString();
			}
			if (defaultURI == null) {
				return ""; //$NON-NLS-1$
			}
			return new Path(defaultURI.getPath()).append(fProjectName).toOSString();
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * @return
	 */
	private IRemoteUIConnectionManager getUIConnectionManager() {
		IRemoteUIConnectionManager connectionManager = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(fSelectedProvider)
				.getUIConnectionManager();
		return connectionManager;
	}

	private void handleConnectionSelected() {
		int selectionIndex = fConnectionCombo.getSelectionIndex();
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(selectionIndex);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		fLocationText.setText(getDefaultPathDisplayString());
	}

	private void handleServicesSelected() {
		int selectionIndex = fProviderCombo.getSelectionIndex();
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(selectionIndex);
		populateConnectionCombo(fConnectionCombo);
		updateNewConnectionButtonEnabled(fNewConnectionButton);
		handleConnectionSelected();
	}

	/**
	 * @param connectionCombo
	 */
	private void populateConnectionCombo(final Combo connectionCombo) {
		connectionCombo.removeAll();

		// attempt to restore settings from saved state
		// IRemoteConnection connectionSelected = fProvider.getConnection();

		IRemoteConnection[] connections = fSelectedProvider.getConnectionManager().getConnections();
		int toSelect = 0;

		for (int k = 0; k < connections.length; k++) {
			connectionCombo.add(connections[k].getName(), k);
			fComboIndexToRemoteConnectionMap.put(k, connections[k]);

			// if (connectionSelected != null &&
			// connectionSelected.getName().compareTo(connections[k].getName())
			// == 0) {
			// toSelect = k;
			// }
		}

		// set selected connection to be the first one if we're not restoring
		// from settings
		connectionCombo.select(toSelect);
		fSelectedConnection = fComboIndexToRemoteConnectionMap.get(toSelect);
	}

	/**
	 * @param button
	 */
	private void updateNewConnectionButtonEnabled(Button button) {
		IRemoteUIConnectionManager connectionManager = getUIConnectionManager();
		button.setEnabled(connectionManager != null);
	}

	protected Control createContents(Composite parent) {
		Group container = new Group(parent, SWT.SHADOW_ETCHED_IN);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gd);

		// Label for "Provider:"
		Label providerLabel = new Label(container, SWT.LEFT);
		providerLabel.setText("Remote Provider:"); //$NON-NLS-1$

		// combo for providers
		fProviderCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
		// set layout to grab horizontal space
		fProviderCombo.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		gd = new GridData();
		gd.horizontalSpan = 2;
		fProviderCombo.setLayoutData(gd);
		fProviderCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleServicesSelected();
			}
		});

		// attempt to restore settings from saved state
		// IRemoteServices providerSelected = fProvider.getRemoteServices();

		// populate the combo with a list of providers
		IRemoteServices[] providers = PTPRemoteCorePlugin.getDefault().getAllRemoteServices();
		int toSelect = 0;

		for (int k = 0; k < providers.length; k++) {
			fProviderCombo.add(providers[k].getName(), k);
			fComboIndexToRemoteServicesProviderMap.put(k, providers[k]);

			// if (providerSelected != null &&
			// providerSelected.getName().compareTo(providers[k].getName()) ==
			// 0) {
			// toSelect = k;
			// }
		}

		// set selected host to be the first one if we're not restoring from
		// settings
		fProviderCombo.select(toSelect);
		fSelectedProvider = fComboIndexToRemoteServicesProviderMap.get(toSelect);

		// connection combo
		// Label for "Connection:"
		Label connectionLabel = new Label(container, SWT.LEFT);
		connectionLabel.setText("Connection:"); //$NON-NLS-1$

		// combo for providers
		fConnectionCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
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
		fNewConnectionButton = new Button(container, SWT.PUSH);
		fNewConnectionButton.setText("New..."); //$NON-NLS-1$
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
			}
		});

		Label locationLabel = new Label(container, SWT.LEFT);
		locationLabel.setText("Location:"); //$NON-NLS-1$

		fLocationText = new Text(container, SWT.SINGLE | SWT.BORDER);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		gd.grabExcessHorizontalSpace = true;
		gd.widthHint = 250;
		fLocationText.setLayoutData(gd);
		fLocationText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				fErrorReporter.reportError(checkValidLocation(), false);
			}
		});

		// new connection button
		fBrowseButton = new Button(container, SWT.PUSH);
		fBrowseButton.setText("Browse..."); //$NON-NLS-1$
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
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

		return container;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	protected void okPressed() {

		// set the provider
		fProvider.setRemoteToolsProviderID(fSelectedProvider.getId());
		fProvider.setRemoteToolsConnection(fSelectedConnection);

	}
}
