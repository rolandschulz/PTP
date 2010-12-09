/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.ui;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;
import org.eclipse.ptp.remotetools.environment.launcher.data.OverwritePolicies;
import org.eclipse.ptp.remotetools.environment.launcher.data.UploadRule;
import org.eclipse.ptp.remotetools.environment.launcher.messages.Messages;
import org.eclipse.ptp.utils.ui.swt.ComboGroup;
import org.eclipse.ptp.utils.ui.swt.ComboMold;
import org.eclipse.ptp.utils.ui.swt.ControlsRelationshipHandler;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.ResourceSelectionDialog;


public class UploadRuleDialog extends TitleAreaDialog implements IRuleDialog {

	Composite content;
	Button defaultRemoteDirectoryButton;
	ControlsRelationshipHandler remoteDirectoryRelationshipHandler;
	TextGroup remoteDirectoryText;
	Button asReadOnlyButton, asExecutableButton;
	Button downloadBackButton, preserveTimeStampButton;
	ComboGroup overwritePolicyCombo;
	Button removeFilesButton;
	Button addFilesFromFilesystemButton;
	Button addDirectoriesFromFilesystemButton;
	Button addFilesFromWorkspaceButton;
	List fileList;
	
	UploadRule uploadRule;
	
	IPath lastSelectedDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation();
	private int listenersEnabled = 0;
	
	public UploadRuleDialog(Shell parentShell, UploadRule rule) {
		super(parentShell);
		
		uploadRule = rule;
	}
	
//	public UploadRuleDialog(Shell shell) {
//		super(shell);
//		uploadRule = new UploadRule();
//		// set defaults
//		uploadRule.setDefaultRemoteDirectory(true);
//		uploadRule.setRemoteDirectory(null);
//		uploadRule.setOverwritePolicy(OverwritePolicies.ALWAYS);
//	}

	protected Control createDialogArea(Composite parent) {
		content = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		content.setLayout(layout);
//		Layout l = content.getParent().getLayout();
		content.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL));
		setTitle(Messages.UploadRuleDialog_Title);
		setMessage(Messages.UploadRuleDialog_Message);
		
		createRemoteDirectoryComposite(content);
		createFilesComposite(content);
		createOptionsComposite(content);
		
		putFieldContents();
		
		return content;
	}

	private void putFieldContents() {
		disableListeners();
		defaultRemoteDirectoryButton.setSelection(uploadRule.isDefaultRemoteDirectory());
		remoteDirectoryRelationshipHandler.manageDependentControls(defaultRemoteDirectoryButton);
		if (uploadRule.getRemoteDirectory() != null) {
			remoteDirectoryText.setString(uploadRule.getRemoteDirectory());
		} else {
			remoteDirectoryText.setString(""); //$NON-NLS-1$
		}
		asReadOnlyButton.setSelection(uploadRule.isAsReadOnly()); 
		asExecutableButton.setSelection(uploadRule.isAsExecutable());
		downloadBackButton.setSelection(uploadRule.isDownloadBack()); 
		preserveTimeStampButton.setSelection(uploadRule.isPreserveTimeStamp());
		overwritePolicyCombo.selectIndexUsingID(Integer.toString(uploadRule.getOverwritePolicy()));
		String items[] = uploadRule.getLocalFilesAsStringArray();
		Arrays.sort(items);
		fileList.setItems(items);
		enableListeners();
	}
	
	private void enableListeners() {
		listenersEnabled ++;		
	}

	private void disableListeners() {
		listenersEnabled--;
	}

	private void fetchFieldContents() {
		disableListeners();
		uploadRule.setDefaultRemoteDirectory(defaultRemoteDirectoryButton.getSelection());
		uploadRule.setRemoteDirectory(remoteDirectoryText.getString());
		uploadRule.setAsExecutable(asExecutableButton.getSelection());
		uploadRule.setAsReadOnly(asReadOnlyButton.getSelection());
		uploadRule.setDownloadBack(downloadBackButton.getSelection());
		uploadRule.setPreserveTimeStamp(preserveTimeStampButton.getSelection());
		uploadRule.setOverwritePolicy(Integer.parseInt(overwritePolicyCombo.getSelectionId()));
		uploadRule.setLocalFiles(fileList.getItems());
		String path =remoteDirectoryText.getString().trim();
		if (path.length() > 0) {
			uploadRule.setRemoteDirectory(path);
		} else {
			uploadRule.setRemoteDirectory(null);
		}
		enableListeners();
	}
	
	protected void okPressed() {
		fetchFieldContents();
		super.okPressed();
	}

	private Composite createRemoteDirectoryComposite(Composite parent) {
		Frame frame = new Frame(parent, Messages.UploadRuleDialog_RemoteDirectoryFrame_Title);
		Composite contents = frame.getComposite();

		defaultRemoteDirectoryButton = new Button(contents, SWT.CHECK);
		defaultRemoteDirectoryButton.setText(Messages.UploadRuleDialog_RemoteDirectoryFrame_LabelDefaultButton);
		defaultRemoteDirectoryButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		TextMold mold = new TextMold(TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.GRID_DATA_ALIGNMENT_FILL, Messages.UploadRuleDialog_RemoteDirectoryFrame_LabelDirectory);
		remoteDirectoryText = new TextGroup(contents, mold);

		remoteDirectoryRelationshipHandler = new ControlsRelationshipHandler(defaultRemoteDirectoryButton, new Control[] {remoteDirectoryText}, false);
		return remoteDirectoryText;
	}

	private Composite createFilesComposite(Composite parent) {
		FrameMold frameMold = new FrameMold();
		frameMold.setColumns(2);
		frameMold.setTitle(Messages.UploadRuleDialog_FileButtonsFrame_Title);
		Frame frame = new Frame(parent, frameMold);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		Frame filesFrame = new Frame(frame.getComposite());
		fileList = new List(filesFrame, SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
		gridData.heightHint=200;
		fileList.setLayoutData(gridData);
		fileList.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) return;
				removeFilesButton.setEnabled(fileList.getSelectionCount() != 0);
			}
		});
		fileList.deselectAll();
		
		Frame buttonFrame = new Frame(frame.getComposite());
		Label label = new Label(buttonFrame, SWT.NONE);
		label.setText(Messages.UploadRuleDialog_FileButtonsFrame_Description);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addFilesFromFilesystemButton = new Button(buttonFrame, SWT.PUSH);
		addFilesFromFilesystemButton.setText(Messages.UploadRuleDialog_FileButtonsFrame_AddFilesButton);
		addFilesFromFilesystemButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addFilesFromFilesystemButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) return;
				handleFilesFromFilesystemButtonEvent();
			}			
		});
		addDirectoriesFromFilesystemButton = new Button(buttonFrame, SWT.PUSH);
		addDirectoriesFromFilesystemButton.setText(Messages.UploadRuleDialog_FileButtonsFrame_AddDirectoryButton);
		addDirectoriesFromFilesystemButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addDirectoriesFromFilesystemButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) return;
				handleDirectoryFromFilesystemButtonEvent();
			}			
		});
		addFilesFromWorkspaceButton = new Button(buttonFrame, SWT.PUSH);
		addFilesFromWorkspaceButton.setText(Messages.UploadRuleDialog_FileButtonsFrame_AddWorkspaceButton);
		addFilesFromWorkspaceButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addFilesFromWorkspaceButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) return;
				handleFilesFromWorkspaceButtonEvent();
			}
		});
		label = new Label(buttonFrame, SWT.NONE);
		label.setText(Messages.UploadRuleDialog_FileButtonsFrame_RemoveFilesLabel);
		label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		removeFilesButton = new Button(buttonFrame, SWT.PUSH);
		removeFilesButton.setText(Messages.UploadRuleDialog_FileButtonsFrame_RemoveButton);
		removeFilesButton.setEnabled(false);
		removeFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		removeFilesButton.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0)
					return;
				handleRemoveFilesButtonEvent();
			}
			
		});
		GridData gridData2 = new GridData();
		gridData2.grabExcessVerticalSpace = true;
		gridData2.verticalAlignment = SWT.TOP;
		buttonFrame.setLayoutData(gridData2);
		
		return frame;
	}

	private Composite createOptionsComposite(Composite parent) {
		FrameMold frameMold = new FrameMold();
		frameMold.setColumns(2);
		frameMold.addOption(FrameMold.COLUMNS_EQUAL_WIDTH);
		frameMold.setTitle(Messages.UploadRuleDialog_OptionsFrame_Title);
		Frame frame = new Frame(parent, frameMold);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		
		Composite contents = frame.getComposite();
		asReadOnlyButton = new Button(contents, SWT.CHECK);
		asReadOnlyButton.setText(Messages.UploadRuleDialog_OptionsFrame_ReadonlyCheck);
		asExecutableButton = new Button(contents, SWT.CHECK);
		asExecutableButton.setText(Messages.UploadRuleDialog_OptionsFrame_ExecutableCheck);
		downloadBackButton = new Button(contents, SWT.CHECK);
		downloadBackButton.setText(Messages.UploadRuleDialog_OptionsFrame_DownloadBackCheck);
		preserveTimeStampButton = new Button(contents, SWT.CHECK);
		preserveTimeStampButton.setText(Messages.UploadRuleDialog_OptionsFrame_PreserveTimeStampCheck);
		
		ComboMold mold = new ComboMold(ComboMold.GRID_DATA_SPAN);
		mold.setLabel(Messages.UploadRuleDialog_OptionsFrame_OverwriteLabel);
		mold.setTextFieldWidth(40);
		mold.addItem(Integer.toString(OverwritePolicies.SKIP), Messages.UploadRuleDialog_OptionsFrame_OverwriteCombo_SkipOption);
		mold.addItem(Integer.toString(OverwritePolicies.ALWAYS), Messages.UploadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteOption);
		mold.addItem(Integer.toString(OverwritePolicies.NEWER), Messages.UploadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteIfNewerOption);
		overwritePolicyCombo = new ComboGroup(contents, mold);
		overwritePolicyCombo.getCombo().addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0)
					return;
				disableListeners();
				if (overwritePolicyCombo.getSelectionId().equals(Integer.toString(OverwritePolicies.NEWER))) {
					preserveTimeStampButton.setEnabled(false);
					preserveTimeStampButton.setSelection(true);
				} else {
					preserveTimeStampButton.setEnabled(true);					
				}
				enableListeners();
			}
			
		});
		return frame;
	}
	
	class PathIterator implements Iterator {
		Iterator internalIterator;
		
		public PathIterator(Iterator internalIterator) {
			this.internalIterator = internalIterator;
		}
		
		public PathIterator(String [] array) {
			this.internalIterator = Arrays.asList(array).iterator();
		}

		public PathIterator(java.util.List list) {
			this.internalIterator = list.iterator();
		}

		public boolean hasNext() {
			return internalIterator.hasNext();
		}
		
		public Object next() {
			return new Path((String)internalIterator.next());
		}
		
		public void remove() {
			internalIterator.remove();
		}
	}
	
	private void handleFilesFromFilesystemButtonEvent() {
		FileDialog fileDialog = new FileDialog(getShell(), SWT.OPEN | SWT.MULTI);

		/*
		 * Filter path must be set to the directory that contains the file.
		 */
		fileDialog.setFilterPath(lastSelectedDirectory.toOSString());
		fileDialog.setText(Messages.UploadRuleDialog_OptionsFrame_AddFileDialog_Title);
				
		String result = fileDialog.open();

		if (result != null) {
			lastSelectedDirectory = new Path(fileDialog.getFilterPath());
			/*
			 * Only add files that are not already in the list.
			 * If file is inside the workspace, add relative path.
			 */
			IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			IPath fileRoot = new Path(fileDialog.getFilterPath());
			HashSet fileSet = new HashSet(Arrays.asList(fileList.getItems()));
			for (Iterator iter = new PathIterator(fileDialog.getFileNames()); iter.hasNext();) {
				/*
				 * If not absolute, the make it absolute according to path in dialog.
				 * This is due behaviour how FileDialog returns selected files.
				 */
				IPath path = (IPath) iter.next();
				if (! path.isAbsolute()) {
					path = fileRoot.append(path);
				}
				
				/*
				 * If path is inside the workspace, then make it relative.
				 */
				if (workspace.isPrefixOf(path)) {
					path = path.removeFirstSegments(workspace.segmentCount());
					path = path.makeRelative();
				}
				
				/*
				 * If already in the list, the ignore.
				 */
				String fullPath = path.toOSString();
				if (! fileSet.contains(fullPath)) {
					fileSet.add(fullPath);
				}
			}
			
			String items[] = new String[fileSet.size()];
			items = (String[]) fileSet.toArray(items);
			Arrays.sort(items);
			fileList.setItems(items);
		}
	}

	private void handleDirectoryFromFilesystemButtonEvent() {
		DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
		
		directoryDialog.setFilterPath(lastSelectedDirectory.toOSString());
		directoryDialog.setText(Messages.UploadRuleDialog_OptionsFrame_AddDirectoryDialog_Title);
		directoryDialog.setMessage(Messages.UploadRuleDialog_OptionsFrame_AddFileDialog_Description);

		String newPath = directoryDialog.open();

		if (newPath != null) {
			HashSet fileSet = new HashSet(Arrays.asList(fileList.getItems()));
			IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			IPath path = new Path(newPath);
			
			/*
			 * If path is inside the workspace, then make it relative.
			 */
			if (workspace.isPrefixOf(path)) {
				path = path.removeFirstSegments(workspace.segmentCount());
				path = path.makeRelative();
			}
			
			/*
			 * If already in the list, the ignore.
			 */
			String fullPath = path.toOSString();
			if (! fileSet.contains(fullPath)) {
				fileSet.add(fullPath);
				
				String items[] = new String[fileSet.size()];
				items = (String[]) fileSet.toArray(items);
				Arrays.sort(items);
				fileList.setItems(items);
			}
			
			/*
			 * Save lastSelectedDirectory. There is a bug in DirectoryDialog.
			 * The method getFilterPath does not return the expected path.
			 * The bug will be fixed in Eclipse 3.4.
			 * The workarount takes the parent of the selected directory as lastSelectedDirectory.
			 */
			lastSelectedDirectory = new Path(newPath);
			lastSelectedDirectory = lastSelectedDirectory.removeLastSegments(1).removeTrailingSeparator();
		}
	}


	ResourceSelectionDialog resourceDialog = null;
	private void handleFilesFromWorkspaceButtonEvent() {
		/*
		 * Create a list a resource for all relative paths. The must be valid resources in the workspace.
		 * Otherwise, they are ignored and removed.
		 */
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ArrayList resourceList = new ArrayList();
		for (Iterator iter = new PathIterator(fileList.getItems()); iter.hasNext();) {
			IPath path = (IPath) iter.next();
			if (path.isAbsolute()) {
				continue;
			}
			IResource resource = root.findMember(path);
			if (resource != null) {
				resourceList.add(resource);
			}
		}
		Object initialSelection[] = resourceList.toArray();
		
		/*
		 * Show the dialog.
		 */
		if (resourceDialog == null) {
			resourceDialog = new ResourceSelectionDialog(getShell(), ResourcesPlugin.getWorkspace().getRoot(), null);
		}
		resourceDialog.setInitialSelections( initialSelection );
		resourceDialog.setBlockOnOpen(true);
		resourceDialog.setMessage(Messages.UploadRuleDialog_OptionsFrame_AddWorkspaceDialog_Description);
		resourceDialog.setTitle(Messages.UploadRuleDialog_OptionsFrame_AddWorkspaceDialog_Title);
		resourceDialog.open();

		/*
		 * Get results and updated local files.
		 */
		Object[] results = resourceDialog.getResult();

		if (results != null) {
			/*
			 * Filter only absolute path. Relative paths are removed.
			 */
			HashSet newFileList = new HashSet();
			for (Iterator iter = new PathIterator(fileList.getItems()); iter.hasNext();) {
				IPath path = (IPath) iter.next();
				if (! path.isAbsolute()) {
					continue;
				}
				newFileList.add(path.toOSString());
			}

			/*
			 * Add items that a selected in the dialog but not in the file list.
			 */
			for (int i = 0; i < results.length; i++) {
				IResource resource = (IResource) results[i];
				String entry = resource.getFullPath().makeRelative().toOSString();
				newFileList.add(entry);
			}
			
			String items[] = new String[newFileList.size()];
			items = (String[]) newFileList.toArray(items);
			Arrays.sort(items);
			fileList.setItems(items);
		}
	}
	
	private void handleRemoveFilesButtonEvent() {		
		String selection [] = fileList.getSelection();
		if (selection.length == 0) {
			return;
		}
		
		HashSet fileSet = new HashSet(Arrays.asList(fileList.getItems()));
		for (int i = 0; i < selection.length; i++) {
			String string = selection[i];
			fileSet.remove(string);
		}
		
		String items[] = new String[fileSet.size()];
		items = (String[]) fileSet.toArray(items);
		Arrays.sort(items);
		fileList.setItems(items);
	}

	public ISynchronizationRule getRuleWorkingCopy() {
		return uploadRule;
	}
}

