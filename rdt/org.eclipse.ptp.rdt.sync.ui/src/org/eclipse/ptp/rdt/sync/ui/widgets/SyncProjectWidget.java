/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Greg Watson - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.widgets;

import java.io.File;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.URIUtil;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.internal.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.internal.rdt.sync.ui.messages.Messages;
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

/**
 * Widget to allow the user to select a local and remote project location. Intended to be used by new sync
 * project wizards that require this information.
 * 
 * If title is supplied then the widget will be placed in a group.
 * 
 * @since 5.0
 * 
 */
public class SyncProjectWidget extends Composite {
	private static final int SIZING_TEXT_FIELD_WIDTH = 250;

	private static final String EMPTY_STRING = ""; //$NON-NLS-1$

	private Text fLocalProjectLocationText;
	private Button fUseDefaultLocationButton;
	private Button fLocalBrowseButton;
	private SyncFileFilter fCustomFilter;

	private ISynchronizeParticipant fSelectedParticipant;

	private String fMessage;
	private String fErrorMessage;
	private int fMessageType;

	private boolean fIsComplete;

	private String fProjectName;

	/**
	 * Constructor
	 * 
	 * @param parent
	 *            parent composite
	 * @param style
	 *            style or SWT.NONE
	 * @param context
	 *            runnable context, or null
	 * @since 7.0
	 */
	public SyncProjectWidget(Composite parent, int style, IRunnableContext context) {
		super(parent, style);

		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		setLayout(layout);

		createLocalGroup(this);
		createRemoteGroup(this, context);
		createFilterGroup(this);
	}

	/**
	 * @param parent
	 */
	private final void createFilterGroup(Composite parent) {
		// File filter button
		final Button filterButton = new Button(parent, SWT.PUSH);
		filterButton.setText(Messages.SyncProjectWidget_Modify_file_filtering);
		filterButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		filterButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				SyncFileFilter tmpFilter;
				if (fCustomFilter == null) {
					tmpFilter = SyncManager.getDefaultFileFilter();
				} else {
					tmpFilter = new SyncFileFilter(fCustomFilter);
				}
				int filterReturnCode = SyncFileFilterPage.openBlocking(tmpFilter, filterButton.getShell());
				if (filterReturnCode == Window.OK) {
					fCustomFilter = tmpFilter;
				}
			}
		});
	}

	/**
	 * @param parent
	 */
	private final void createLocalGroup(Composite parent) {
		Group localGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		localGroup.setText(Messages.SyncProjectWidget_Local_directory);
		localGroup.setLayout(new GridLayout(3, false));
		localGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		fUseDefaultLocationButton = new Button(localGroup, SWT.CHECK);
		fUseDefaultLocationButton.setText(Messages.SyncProjectWidget_Use_default_location);
		fUseDefaultLocationButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 3, 1));
		fUseDefaultLocationButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				setLocalProjectLocation();
			}
		});
		fUseDefaultLocationButton.setSelection(true);

		Label label = new Label(localGroup, SWT.NONE);
		label.setText(Messages.SyncProjectWidget_Local_directory_label);
		label.setFont(parent.getFont());

		fLocalProjectLocationText = new Text(localGroup, SWT.BORDER);
		GridData locationData = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		locationData.widthHint = SIZING_TEXT_FIELD_WIDTH;
		fLocalProjectLocationText.setLayoutData(locationData);
		fLocalProjectLocationText.setFont(parent.getFont());
		fLocalProjectLocationText.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setPageComplete(validatePage());
				notifyListeners(SWT.Modify, e);
			}
		});
		fLocalProjectLocationText.setEnabled(false);

		fLocalBrowseButton = new Button(localGroup, SWT.PUSH);
		fLocalBrowseButton.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		fLocalBrowseButton.setText(Messages.SyncProjectWidget_Browse);
		fLocalBrowseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				DirectoryDialog dirDialog = new DirectoryDialog(getShell());
				dirDialog.setText(Messages.SyncProjectWidget_Select_project_local_directory);
				String selectedDir = dirDialog.open();
				fLocalProjectLocationText.setText(selectedDir);
			}
		});
	}

	/**
	 * @param parent
	 * @param context
	 */
	private final void createRemoteGroup(Composite parent, IRunnableContext context) {
		Group remoteGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
		remoteGroup.setText(Messages.SyncProjectWidget_Remote_directory);
		remoteGroup.setLayout(new GridLayout(1, false));
		remoteGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

		// For now, assume only one provider, to reduce the number of GUI elements.

		// TODO: Add error handling if there are no providers
		ISynchronizeParticipantDescriptor[] providers = SynchronizeParticipantRegistry.getDescriptors();
		fSelectedParticipant = providers[0].getParticipant();
		fSelectedParticipant.createConfigurationArea(remoteGroup, context);
		// Without this, participant uses the old project name from the last time it was invoked.
		fSelectedParticipant.setProjectName(EMPTY_STRING);
	}

	/**
	 * @return
	 */
	public SyncFileFilter getCustomFileFilter() {
		return fCustomFilter;
	}

	/**
	 * @return
	 */
	public String getErrorMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return fErrorMessage;
	}

	/**
	 * @return
	 */
	public String getMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return fMessage;
	}

	/**
	 * @return
	 */
	public int getMessageType() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return fMessageType;
	}

	/**
	 * @return
	 */
	public String getProjectLocalLocation() {
		if (fLocalProjectLocationText == null) {
			return EMPTY_STRING;
		}
		return fLocalProjectLocationText.getText().trim();
	}

	private String getProjectName() {
		return fProjectName;
	}

	/**
	 * @return
	 */
	public ISynchronizeParticipant getSynchronizeParticipant() {
		return fSelectedParticipant;
	}

	/**
	 * @return
	 */
	public boolean isPageComplete() {
		return fIsComplete;
	}

	/**
	 * 
	 */
	private void setLocalProjectLocation() {
		// Build string if default location is indicated.
		if (fUseDefaultLocationButton.getSelection()) {
			String name = getProjectName();
			if (name != null) {
				fLocalProjectLocationText.setText(Platform.getLocation().toOSString() + File.separator + name);
			} else {
				fLocalProjectLocationText.setText(Platform.getLocation().toOSString());
			}
			// If user just unchecked default location, erase field contents.
		} else if (!fLocalProjectLocationText.isEnabled()) {
			fLocalProjectLocationText.setText(EMPTY_STRING);
		}

		// These two values should never match.
		fLocalProjectLocationText.setEnabled(!fUseDefaultLocationButton.getSelection());
	}

	/**
	 * @param complete
	 */
	private void setPageComplete(boolean complete) {
		fIsComplete = complete;
	}

	public void setProjectName(String name) {
		fProjectName = name;
		setLocalProjectLocation();
		if (fSelectedParticipant != null) {
			fSelectedParticipant.setProjectName(name);
		}
	}

	/**
	 * @return
	 */
	public boolean useDafaults() {
		return fUseDefaultLocationButton.getSelection();
	}

	/**
	 * @return
	 */
	private boolean validateLocalLocation() {
		IProject handle = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		URI location = URIUtil.toURI(fLocalProjectLocationText.getText());

		// Check if project exists
		if (handle.exists()) {
			fErrorMessage = Messages.SyncProjectWidget_Project_already_exists;
			return false;
		}

		// Validate location according to built-in rules
		if (!fUseDefaultLocationButton.getSelection()) {
			IStatus locationStatus = ResourcesPlugin.getWorkspace().validateProjectLocationURI(handle, location);
			if (!locationStatus.isOK()) {
				fErrorMessage = locationStatus.getMessage();
				return false;
			}
		}

		// Check if location is an existing file or directory
		try {
			IFileStore fs = EFS.getStore(location);
			IFileInfo f = fs.fetchInfo();
			if (f.exists()) {
				if (f.isDirectory()) {
					fMessage = Messages.SyncProjectWidget_Directory_already_exists;
					fMessageType = IMessageProvider.WARNING;
				} else {
					fErrorMessage = Messages.SyncProjectWidget_9;
					return false;
				}
			}
		} catch (CoreException e) {
			RDTSyncUIPlugin.log(e.getStatus());
		}

		return true;
	}

	/**
	 * @return
	 */
	private boolean validatePage() {
		fMessage = null;
		fMessageType = IMessageProvider.NONE;
		fErrorMessage = null;
		return (validateLocalLocation() && validateRemoteLocation());
	}

	/**
	 * @return
	 */
	private boolean validateRemoteLocation() {
		fErrorMessage = fSelectedParticipant.getErrorMessage();
		return fSelectedParticipant.getErrorMessage() == null && fSelectedParticipant.isConfigComplete();
	}
}
