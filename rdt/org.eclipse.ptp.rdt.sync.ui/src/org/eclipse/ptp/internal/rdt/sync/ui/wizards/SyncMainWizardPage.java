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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.SynchronizeWizardExtensionRegistry;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeWizardExtension;
import org.eclipse.ptp.rdt.sync.ui.widgets.SyncProjectWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
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
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;
	private static final String EMPTY_STRING = ""; //$NON-NLS-1$
	private static final String syncConfigSetKey = "sync-config-set"; //$NON-NLS-1$
	private static final String projectNameKey = "project-name"; //$NON-NLS-1$

	private String message;
	private int messageType = IMessageProvider.NONE;
	private String errorMessage;

	private Text fProjectNameText;
	private Combo fProjectSelectionCombo;
	private SyncProjectWidget fSyncWidget;
	private IWizardPage nextPage;

	/**
	 * Creates a new wizard page for creating a new sync project.
	 * 
	 * @param pageName
	 *            the name of this page
	 * @return the page
	 */
	public static SyncMainWizardPage newProjectPage(String pageName) {
		return new SyncMainWizardPage(pageName, true);
	}

	/**
	 * Creates a new wizard page for converting an existing project to a sync project.
	 * 
	 * @param pageName
	 *            the name of this page
	 * @return the page
	 */
	public static SyncMainWizardPage convertProjectPage(String pageName) {
		return new SyncMainWizardPage(pageName, false);
	}

	/**
	 * Private constructor. Clients should use provided static factory methods.
	 * 
	 * @param pageName
	 *            the name of this page
	 * @param newProjectFlag
	 *            whether a page for a new project or to convert an existing project
	 */
	private SyncMainWizardPage(String pageName, boolean isForNewProject) {
		super(pageName);
		isNewProject = isForNewProject;
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

		if (isNewProject) {
			createProjectInfoGroupForNewProject(composite);
		} else {
			createProjectInfoGroupForExistingProject(composite);
		}

		if (isNewProject) {
			fSyncWidget = SyncProjectWidget.newProjectWidget(composite, SWT.NONE, getWizard().getContainer());
		} else {
			fSyncWidget = SyncProjectWidget.convertProjectWidget(composite, SWT.NONE, getWizard().getContainer());
		}
		fSyncWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		fSyncWidget.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				update();
				getWizard().getContainer().updateMessage();
			}
		});

		setPageComplete(false);
		errorMessage = null;
		message = null;
		messageType = IMessageProvider.NONE;
		Dialog.applyDialogFont(composite);
	}

	/**
	 * Creates controls for selecting an existing project
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectInfoGroupForExistingProject(Composite parent) {
		Group localGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		localGroup.setText(Messages.SyncProjectWidget_Project_to_convert);
		localGroup.setLayout(new GridLayout(2, false));
		localGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		Label label = new Label(localGroup, SWT.NONE);
		label.setText(Messages.SyncProjectWidget_Project_to_convert_label);
		label.setFont(parent.getFont());

		fProjectSelectionCombo = new Combo(localGroup, SWT.READ_ONLY);
		this.populateProjectCombo();
		fProjectSelectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		fProjectSelectionCombo.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// nothing to do
			}

			@Override
			public void widgetSelected(SelectionEvent arg0) {
				if (fSyncWidget != null && (validateProjectName() || getProjectName().equals(EMPTY_STRING))) {
					fSyncWidget.setProjectName(getProjectName());
					handleProjectSelected(getProject());
				}
				update();
				getWizard().getContainer().updateMessage();
			}
		});
	}

	/**
	 * Creates controls for entering a new project name
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectInfoGroupForNewProject(Composite parent) {
		Composite projectGroup = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 2;
		projectGroup.setLayout(layout);
		projectGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// new project name label
		Label projectNameLabel = new Label(projectGroup, SWT.NONE);
		projectNameLabel.setText(Messages.SyncMainWizardPage_Project_name);
		projectNameLabel.setFont(parent.getFont());

		// new project name entry field
		fProjectNameText = new Text(projectGroup, SWT.BORDER);
		GridData nameData = new GridData(GridData.FILL_HORIZONTAL);
		nameData.widthHint = SIZING_TEXT_FIELD_WIDTH;
		fProjectNameText.setLayoutData(nameData);
		fProjectNameText.setFont(parent.getFont());

		fProjectNameText.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				if (fSyncWidget != null && (validateProjectName() || getProjectName().equals(EMPTY_STRING))) {
					fSyncWidget.setProjectName(getProjectName());
				}
				update();
				getWizard().getContainer().updateMessage();
			}
		});
	}

	/**
	 * Returns the currently selected project.
	 * This only works for project conversion. For new projects, this function always returns null.
	 * @return project
	 */
	public IProject getProject() {
		if (isNewProject) {
			return null;
		} else {
			return ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		}
	}

	/**
	 * Returns the current project name specified by the user
	 * 
	 * @return the project name
	 */
	@Override
	public String getProjectName() {
		if (isNewProject) {
			if (fProjectNameText == null) {
				return EMPTY_STRING;
			} else {
				return fProjectNameText.getText().trim();
			}
		} else {
			return fProjectSelectionCombo.getText();
		}
	}

	/**
	 * Get the synchronize filter specified for the project
	 * 
	 * @return sync filter
	 */
	public SyncFileFilter getCustomFileFilter() {
		return fSyncWidget.getCustomFileFilter();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		update(); // Necessary to update message when participant changes
		return errorMessage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#getLocationPath()
	 */
	@Override
	public IPath getLocationPath() {
		return new Path(fSyncWidget.getProjectLocalLocation());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#getLocationURI()
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
		update(); // Necessary to update message when participant changes
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessageType()
	 */
	@Override
	public int getMessageType() {
		update(); // Necessary to update message when participant changes
		return messageType;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.jface.wizard.WizardPage#getNextPage()
	 */
	@Override
	public IWizardPage getNextPage() {
		return nextPage;
	}

	private String[] getSyncConfigNames() {
		ArrayList<String> configNames = new ArrayList<String>();
		configNames.add("Local"); //$NON-NLS-1$
		String remoteConfigName = fSyncWidget.getSyncConfigName();
		if (remoteConfigName != null) {
			configNames.add(remoteConfigName);
		}
		return configNames.toArray(new String[0]);
	}

	/**
	 * Get the synchronize participant, which contains remote information
	 * 
	 * @return participant
	 */
	public ISynchronizeParticipant getSynchronizeParticipant() {
		return fSyncWidget.getSynchronizeParticipant();
	}

	/**
	 * Add any extended wizard pages for the given project type
	 * @param project
	 */
	private void handleProjectSelected(IProject project) {
		ISynchronizeWizardExtension ext = SynchronizeWizardExtensionRegistry.getSynchronizeWizardExtensionForProject(project);
		if (ext == null) {
			nextPage = null;
		} else {
			nextPage = ext.createConvertProjectWizardPage();
			IWizard wizard = getWizard();
			assert(wizard instanceof Wizard);
			((Wizard) wizard).addPage(nextPage);
		}
	}
	private void populateProjectCombo() {
		IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
		for (IProject p : allProjects) {
			if ((p.isOpen()) && (!RemoteSyncNature.hasNature(p))) {
				fProjectSelectionCombo.add(p.getName());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#setInitialProjectName(java.lang.String)
	 */
	@Override
	public void setInitialProjectName(String name) {
		// Not possible
	}

	/*
	 * see @DialogPage.setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.getControl().setVisible(visible);
		if (visible) {
			if (isNewProject) {
				fProjectNameText.setFocus();
			} else {
				fProjectSelectionCombo.setFocus();
			}
		}
	}

	/**
	 * Numerous tasks to refresh the page:
	 * 1) Validate the page, which updates messages
	 * 2) Set whether or not page is complete
	 * 3) Store data to sync cache for other wizard pages
	 */
	private void update() {
		boolean isValid = validatePage();
		setPageComplete(isValid);
		if (isValid) {
			Set<String> configNamesSet = new HashSet<String>();
			for (String name : this.getSyncConfigNames()) {
				configNamesSet.add(name);
			}
			SyncWizardDataCache.setProperty(getWizard().hashCode(), projectNameKey, getProjectName());
			SyncWizardDataCache.setMultiValueProperty(getWizard().hashCode(), syncConfigSetKey, configNamesSet);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#useDefaults()
	 */
	@Override
	public boolean useDefaults() {
		return fSyncWidget.useDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		message = null;
		messageType = IMessageProvider.NONE;
		errorMessage = null;
		if (!validateProjectName()) {
			return false;
		}
		if (!fSyncWidget.isPageComplete()) {
			message = fSyncWidget.getMessage();
			messageType = fSyncWidget.getMessageType();
			errorMessage = fSyncWidget.getErrorMessage();
			return false;
		}
		return true;
	}

	/**
	 * @return
	 */
	private boolean validateProjectName() {
		// Check if name is empty
		String projectFieldContents = getProjectName();
		if (projectFieldContents.equals(EMPTY_STRING)) {
			message = Messages.SyncMainWizardPage_Project_name_must_be_specified;
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
			errorMessage = Messages.SyncMainWizardPage_Project_name_cannot_contain_hash;
			return false;
		}

		return true;
	}
}