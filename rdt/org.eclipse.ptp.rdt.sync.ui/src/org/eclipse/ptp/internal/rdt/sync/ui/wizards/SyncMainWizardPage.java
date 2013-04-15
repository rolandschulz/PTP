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

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.widgets.SyncProjectWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
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

	private String message;
	private int messageType = IMessageProvider.NONE;
	private String errorMessage;

	private Text fProjectNameText;
	private SyncProjectWidget fSyncWidget;

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

		fSyncWidget = new SyncProjectWidget(composite, SWT.NONE, getWizard().getContainer());
		fSyncWidget.setLayoutData(new GridData(GridData.FILL_BOTH));
		fSyncWidget.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setPageComplete(validatePage());
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
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectBasicInfoGroup(Composite parent) {
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
				if (fSyncWidget != null) {
					fSyncWidget.setProjectName(getProjectName());
				}
				setPageComplete(validatePage());
				getWizard().getContainer().updateMessage();
			}
		});
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#getProjectName()
	 */
	@Override
	public String getProjectName() {
		return getProjectNameFieldValue();
	}

	/**
	 * Returns the value of the project name field with leading and trailing spaces removed.
	 * 
	 * @return the project name in the field
	 */
	private String getProjectNameFieldValue() {
		if (fProjectNameText == null) {
			return EMPTY_STRING;
		}

		return fProjectNameText.getText().trim();
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
		setPageComplete(validatePage()); // Necessary to update message when participant changes
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
	 * Get the synchronize participant, which contains remote information
	 * 
	 * @return participant
	 */
	public ISynchronizeParticipant getSynchronizeParticipant() {
		return fSyncWidget.getSynchronizeParticipant();
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
			fProjectNameText.setFocus();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#useDefaults()
	 */
	@Override
	public boolean useDefaults() {
		return fSyncWidget.useDafaults();
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
		String projectFieldContents = getProjectNameFieldValue();
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