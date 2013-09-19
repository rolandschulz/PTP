/*******************************************************************************
 * Copyright (c) 2008, 2011 IBM Corporation and others.
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

import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConstants;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
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
	
	private final IRunnableContext fContext;

	private RemoteBuildServiceProvider fProvider;

	private IRemoteConnection fSelectedConnection;

	private String fProjectName = IDEResourceInfoUtils.EMPTY_STRING;

	private Button fBrowseButton;

	private Text fLocationText;
	
	private RemoteConnectionWidget fRemoteConnectionWidget;

	// public RemoteProjectContentsLocationArea(IServiceProvider provider,
	// Composite composite) {
	// if(provider instanceof RemoteBuildServiceProvider)
	// fProvider = (RemoteBuildServiceProvider) provider;
	// else
	// throw new IllegalArgumentException(); // should never happen
	// createContents(composite);
	// }
	/**
	 * @since 3.1
	 */
	public RemoteProjectContentsLocationArea(IErrorMessageReporter reporter, Composite composite, IRunnableContext context) {
		fErrorReporter = reporter;
		fContext = context;
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
		return new Button[] { fBrowseButton, fRemoteConnectionWidget.getButton() };
	}

	/**
	 * Returns the name of the selected connection.
	 * @since 6.0
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
		if (fSelectedConnection == null)
			return null;
		return fSelectedConnection.getFileManager().toURI(fLocationText.getText());
	}

	/**
	 * @since 6.0
	 */
	public IRemoteConnection getRemoteConnection() {
		return fSelectedConnection;
	}

	/**
	 * @since 6.0
	 */
	public IRemoteServices getRemoteServices() {
		if (fSelectedConnection != null)
			return fSelectedConnection.getRemoteServices();
		return null;
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
			mgr.openConnectionWithProgress(fRemoteConnectionWidget.getShell(), fContext, fSelectedConnection);
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
			IRemoteFileManager fileMgr = getRemoteConnection().getFileManager();
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
		IRemoteUIConnectionManager connectionManager = RemoteUIServices.getRemoteUIServices(fSelectedConnection.getRemoteServices())
				.getUIConnectionManager();
		return connectionManager;
	}

	private void handleConnectionSelected() {
		fSelectedConnection = fRemoteConnectionWidget.getConnection();
		fLocationText.setText(getDefaultPathDisplayString());
	}

	protected Control createContents(Composite parent) {
		Group container = new Group(parent, SWT.SHADOW_ETCHED_IN);

		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		container.setLayout(layout);
		GridData gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		container.setLayoutData(gd);
		
		fRemoteConnectionWidget = new RemoteConnectionWidget(container, SWT.NONE, null, RemoteConnectionWidget.FLAG_FORCE_PROVIDER_SELECTION, null);
		gd = new GridData();
		gd.horizontalSpan = 3;
		fRemoteConnectionWidget.setLayoutData(gd);
		fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleConnectionSelected();
			}
		});

		Label locationLabel = new Label(container, SWT.LEFT);
		locationLabel.setText(Messages.getString("RemoteProjectContentsLocationArea.3")); //$NON-NLS-1$

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
		fBrowseButton.setText(Messages.getString("RemoteProjectContentsLocationArea.4")); //$NON-NLS-1$
		fBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (fSelectedConnection != null) {
					checkConnection();
					if (fSelectedConnection.isOpen()) {
						IRemoteUIServices remoteUIServices = RemoteUIServices.getRemoteUIServices(fSelectedConnection.getRemoteServices());
						if (remoteUIServices != null) {
							IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
							if (fileMgr != null) {
								fileMgr.setConnection(fSelectedConnection);
								String correctPath = fLocationText.getText();
								String selectedPath = fileMgr.browseDirectory(
										fLocationText.getShell(),
										Messages.getString("RemoteProjectContentsLocationArea.5", fSelectedConnection.getName()), correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$
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
		// set the connection
		fProvider.setRemoteToolsConnection(fSelectedConnection);

	}
}
