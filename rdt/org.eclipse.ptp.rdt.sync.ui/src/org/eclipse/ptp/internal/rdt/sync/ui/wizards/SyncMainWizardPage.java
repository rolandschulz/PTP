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
package org.eclipse.ptp.internal.rdt.sync.ui.wizards;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.preferences.SyncFileFilterPage;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.SyncManager;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipantDescriptor;
import org.eclipse.ptp.rdt.sync.ui.SynchronizeParticipantRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Main wizard page for creating new synchronized projects. All elements needed for a synchronized project are configured here. This
 * includes: 1) Project name and workspace location 2) Remote connection and directory 3) Project type 4) Local and remote
 * toolchains
 * 
 * Since this wizard page's operation differs greatly from a normal CDT wizard page, this class simply reimplements (overrides) all
 * functionality in the two immediate superclasses (CDTMainWizardPage and WizardNewProjectCreationPage) but borrows much of the code
 * from those two classes. Thus, except for very basic functionality, such as jface methods, this class is self-contained.
 */
public class SyncMainWizardPage extends WizardNewProjectCreationPage {

	// widgets
	private Text projectNameField;
	private Button defaultLocationButton;
	private Text projectLocationField;
	private Button browseButton;

	private ISynchronizeParticipant fSelectedParticipant;
	private String message;
	private int messageType = IMessageProvider.NONE;
	private String errorMessage;
	private String initialProjectFieldValue;
	private SyncFileFilter customFilter;

	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public SyncMainWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	/**
	 * (non-Javadoc) Method declared on IDialogPage.
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(composite);

		createProjectBasicInfoGroup(composite);
		createProjectRemoteInfoGroup(composite);
		createFilterGroup(composite);

		setPageComplete(false);
		errorMessage = null;
		message = null;
		messageType = IMessageProvider.NONE;
		Dialog.applyDialogFont(composite);
	}

	private final void createFilterGroup(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout());

		// File filter button
		final Button filterButton = new Button(comp, SWT.PUSH);
		filterButton.setText("Modify file filtering..."); //$NON-NLS-1$
		filterButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		filterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				SyncFileFilter tmpFilter;
				if (customFilter == null) {
					tmpFilter = SyncManager.getDefaultFileFilter();
				} else {
					tmpFilter = new SyncFileFilter(customFilter);
				}
				int filterReturnCode = SyncFileFilterPage.openBlocking(tmpFilter, filterButton.getShell());
				if (filterReturnCode == Window.OK) {
					customFilter = tmpFilter;
				}
			}
		});
	}

	/**
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectBasicInfoGroup(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// new project name label
		Label projectNameLabel = new Label(projectGroup, SWT.NONE);
		projectNameLabel.setText("Project name:");//$NON-NLS-1$
		projectNameLabel.setFont(parent.getFont());

		// new project name entry field
		projectNameField = new Text(projectGroup, SWT.BORDER);
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.horizontalSpan = 2;
		nameData.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectNameField.setLayoutData(nameData);
		projectNameField.setFont(parent.getFont());

		projectNameField.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setProjectLocation();
				if (fSelectedParticipant != null) {
					fSelectedParticipant.setProjectName(getProjectName());
				}
				setPageComplete(validatePage());
				getWizard().getContainer().updateMessage();
			}
		});

		Group locationGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		locationGroup.setText("Local directory");//$NON-NLS-1$
		locationGroup.setLayout(new GridLayout(3, false));
		locationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Use default location button
		defaultLocationButton = new Button(locationGroup, SWT.CHECK);
		defaultLocationButton.setText("Use default location");//$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		defaultLocationButton.setLayoutData(gd);
		defaultLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setProjectLocation();
			}
		});
		defaultLocationButton.setSelection(true);

		// new project location label
		Label projectLocationLabel = new Label(locationGroup, SWT.NONE);
		projectLocationLabel.setText("Local directory:");//$NON-NLS-1$
		projectLocationLabel.setFont(parent.getFont());

		// new project location entry field
		projectLocationField = new Text(locationGroup, SWT.BORDER);
		GridData locationData = new GridData(GridData.FILL_HORIZONTAL);
		locationData.widthHint = SIZING_TEXT_FIELD_WIDTH;
		projectLocationField.setLayoutData(locationData);
		projectLocationField.setFont(parent.getFont());
		projectLocationField.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setPageComplete(validatePage());
				getWizard().getContainer().updateMessage();
			}
		});
		projectLocationField.setEnabled(false);
		this.setProjectLocation();

		if (initialProjectFieldValue != null) {
			updateProjectLocation(initialProjectFieldValue);
		}

		// Browse button
		browseButton = new Button(locationGroup, SWT.PUSH);
		browseButton.setText("Browse...");//$NON-NLS-1$
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(projectLocationField.getShell());
				dirDialog.setText("Select project local directory");//$NON-NLS-1$
				String selectedDir = dirDialog.open();
				projectLocationField.setText(selectedDir);
			}
		});
	}

	private final void createProjectRemoteInfoGroup(Composite parent) {
		Group locationGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		locationGroup.setText("Remote directory");//$NON-NLS-1$
		locationGroup.setLayout(new GridLayout(1, false));
		locationGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		// For now, assume only one provider, to reduce the number of GUI elements.

		// TODO: Add error handling if there are no providers
		ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();
		fSelectedParticipant = providers[0].getParticipant();
		fSelectedParticipant.createConfigurationArea(locationGroup, getWizard().getContainer());
		// Without this, participant uses the old project name from the last time it was invoked.
		fSelectedParticipant.setProjectName(""); //$NON-NLS-1$
	}

	public SyncFileFilter getCustomFileFilter() {
		return customFilter;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return errorMessage;
	}

	/**
	 * Returns the current project location path as entered by the user
	 * 
	 * @return the project location path or its anticipated initial value.
	 */
	@Override
	public IPath getLocationPath() {
		return new Path(projectLocationField.getText());
	}

	/**
	 * Get workspace URI
	 * 
	 * @return URI or null if location path is not a valid URI
	 */
	@Override
	public URI getLocationURI() {
		try {
			return new URI("file://" + getLocationPath().toString()); //$NON-NLS-1$
		} catch (URISyntaxException e) {
			RDTSyncUIPlugin.log(e);
			return null;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	@Override
	public String getMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessageType()
	 */
	@Override
	public int getMessageType() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return messageType;
	}

	/**
	 * Returns the current project name as entered by the user, or its anticipated
	 * initial value.
	 * 
	 * @return the project name, its anticipated initial value, or <code>null</code> if no project name is known
	 */
	@Override
	public String getProjectName() {
		if (projectNameField == null) {
			return initialProjectFieldValue;
		}

		return getProjectNameFieldValue();
	}

	/**
	 * Returns the value of the project name field with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (projectNameField == null) {
			return ""; //$NON-NLS-1$
		}

		return projectNameField.getText().trim();
	}

	/**
	 * Get the synchronize participant, which contains remote information
	 * 
	 * @return participant
	 */
	public ISynchronizeParticipant getSynchronizeParticipant() {
		return fSelectedParticipant;
	}

	/**
	 * Sets the initial project name that this page will use when
	 * created. The name is ignored if the createControl(Composite)
	 * method has already been called. Leading and trailing spaces
	 * in the name are ignored.
	 * Providing the name of an existing project will not necessarily
	 * cause the wizard to warn the user. Callers of this method
	 * should first check if the project name passed already exists
	 * in the workspace.
	 * 
	 * @param name
	 *            initial project name for this page
	 * 
	 * @see IWorkspace#validateName(String, int)
	 * 
	 */
	@Override
	public void setInitialProjectName(String name) {
		if (name == null) {
			initialProjectFieldValue = null;
		} else {
			initialProjectFieldValue = name.trim();
			updateProjectLocation(initialProjectFieldValue);
		}
	}

	// Decides what should appear in project location field and whether or not it should be enabled
	private void setProjectLocation() {
		// Build string if default location is indicated.
		if (defaultLocationButton.getSelection()) {
			projectLocationField.setText(Platform.getLocation().toOSString() + File.separator + getProjectName());
			// If user just unchecked default location, erase field contents.
		} else if (!projectLocationField.isEnabled()) {
			projectLocationField.setText(""); //$NON-NLS-1$
		}

		// These two values should never match.
		projectLocationField.setEnabled(!defaultLocationButton.getSelection());
	}

	/*
	 * see @DialogPage.setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.getControl().setVisible(visible);
		if (visible) {
			projectNameField.setFocus();
		}
	}

	private void updateProjectLocation(String name) {
		String path = Platform.getLocation().append(name).toOSString();
		projectLocationField.setText(path);
	}

	@Override
	public boolean useDefaults() {
		return defaultLocationButton.getSelection();
	}

	@Override
	protected boolean validatePage() {
		message = null;
		messageType = IMessageProvider.NONE;
		errorMessage = null;
		return (validateProjectNameAndLocation() && validateRemoteLocation());
	}

	protected boolean validateProjectNameAndLocation() {
		// Check if name is empty
		String projectFieldContents = getProjectNameFieldValue();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			message = "Project name must be specified";//$NON-NLS-1$
			messageType = IMessageProvider.NONE;
			return false;
		}

		// General name check
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			errorMessage = nameStatus.getMessage();
			return false;
		}

		// Do not allow # in the name
		if (getProjectName().indexOf('#') >= 0) {
			errorMessage = "Project name cannot contain '#' symbol"; //$NON-NLS-1$
			return false;
		}

		IProject handle = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		URI location = URIUtil.toURI(projectLocationField.getText());

		// Check if project exists
		if (handle.exists()) {
			errorMessage = "A project with that name already exists in the workspace";//$NON-NLS-1$
			return false;
		}

		// Validate location according to built-in rules
		if (!defaultLocationButton.getSelection()) {
			IStatus locationStatus = ResourcesPlugin.getWorkspace().validateProjectLocationURI(handle, location);
			if (!locationStatus.isOK()) {
				errorMessage = locationStatus.getMessage();
				return false;
			}
		}

		// Check if location is an existing file or directory
		try {
			IFileStore fs = EFS.getStore(location);
			IFileInfo f = fs.fetchInfo();
			if (f.exists()) {
				if (f.isDirectory()) {
					message = "Directory with specified name already exists";//$NON-NLS-1$
					messageType = IMessageProvider.WARNING;
				} else {
					errorMessage = "File with specified name already exists";//$NON-NLS-1$
					return false;
				}
			}
		} catch (CoreException e) {
			RDTSyncUIPlugin.log(e.getStatus());
		}

		return true;
	}

	protected boolean validateRemoteLocation() {
		errorMessage = fSelectedParticipant.getErrorMessage();
		return fSelectedParticipant.getErrorMessage() == null && fSelectedParticipant.isConfigComplete();
	}
}