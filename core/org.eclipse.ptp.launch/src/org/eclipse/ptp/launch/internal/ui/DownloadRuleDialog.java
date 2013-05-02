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
package org.eclipse.ptp.launch.internal.ui;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.internal.ui.widgets.ComboGroup;
import org.eclipse.ptp.internal.ui.widgets.ComboMold;
import org.eclipse.ptp.internal.ui.widgets.Frame;
import org.eclipse.ptp.internal.ui.widgets.FrameMold;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.internal.rulesengine.DownloadRule;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.OverwritePolicies;
import org.eclipse.ptp.launch.ui.IRuleDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 * TODO: NEEDS TO BE DOCUMENTED
 */
public class DownloadRuleDialog extends TitleAreaDialog implements IRuleDialog {

	protected Composite content;

	protected Text localDirectoryText;
	protected Button localDirectoryButton;
	protected Button localWorkspaceButton;

	protected List fileList;
	protected Button addFilesButton;
	protected Button editFileButton;
	protected Button removeFilesButton;

	protected Button asReadOnlyButton;
	protected Button asExecutableButton;
	protected Button preserveTimeStampButton;
	protected ComboGroup overwritePolicyCombo;

	protected DownloadRule downloadRule;
	protected IPath lastSelectedDirectory = ResourcesPlugin.getWorkspace().getRoot().getLocation();

	private int listenersEnabled = 0;

	/**
	 * @since 5.0
	 */
	public DownloadRuleDialog(Shell parentShell, DownloadRule rule) {
		super(parentShell);

		downloadRule = rule;
	}

	public DownloadRuleDialog(Shell shell) {
		super(shell);
		downloadRule = new DownloadRule();
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		content = new Composite(parent, SWT.BORDER);
		GridLayout layout = new GridLayout();
		content.setLayout(layout);
		content.setLayoutData(new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL));
		setTitle(Messages.DownloadRuleDialog_Title);
		setMessage(Messages.DownloadRuleDialog_Message);

		createRemoteFilesComposite(content);
		createLocalDirectoryComposite(content);
		createOptionsComposite(content);

		putFieldContents();

		return content;
	}

	private void enableListeners() {
		listenersEnabled++;
	}

	private void disableListeners() {
		listenersEnabled--;
	}

	private void putFieldContents() {
		disableListeners();

		if (downloadRule.getLocalDirectory() != null) {
			localDirectoryText.setText(downloadRule.getLocalDirectory());
		} else {
			localDirectoryText.setText(""); //$NON-NLS-1$
		}
		overwritePolicyCombo.selectIndexUsingID(Integer.toString(downloadRule.getOverwritePolicy()));
		asExecutableButton.setSelection(downloadRule.isAsExecutable());
		asReadOnlyButton.setSelection(downloadRule.isAsReadOnly());
		preserveTimeStampButton.setSelection(downloadRule.isPreserveTimeStamp());
		String items[] = downloadRule.getRemoteFilesAsStringArray();
		Arrays.sort(items);
		fileList.setItems(items);

		enableListeners();
	}

	private void fetchFieldContents() {
		disableListeners();

		String path = localDirectoryText.getText().trim();
		if (path.length() > 0) {
			downloadRule.setLocalDirectory(path);
		} else {
			downloadRule.setLocalDirectory(null);
		}
		downloadRule.setOverwritePolicy(Integer.parseInt(overwritePolicyCombo.getSelectionId()));
		downloadRule.setAsExecutable(asExecutableButton.getSelection());
		downloadRule.setAsReadOnly(asReadOnlyButton.getSelection());
		downloadRule.setPreserveTimeStamp(preserveTimeStampButton.getSelection());
		downloadRule.setRemoteFiles(fileList.getItems());

		enableListeners();
	}

	@Override
	protected void okPressed() {
		fetchFieldContents();
		super.okPressed();
	}

	private Composite createLocalDirectoryComposite(Composite parent) {
		FrameMold frameMold = new FrameMold();
		frameMold.setColumns(3);
		frameMold.setTitle(Messages.DownloadRuleDialog_DestinationFrame_Title);
		Frame frame = new Frame(parent, frameMold);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		Composite contents = frame.getComposite();

		GridData localDirectoryGridData = new GridData();
		localDirectoryGridData.grabExcessHorizontalSpace = true;
		localDirectoryGridData.horizontalSpan = 3;
		localDirectoryGridData.horizontalAlignment = SWT.FILL;
		localDirectoryText = new Text(contents, SWT.SINGLE | SWT.BORDER);
		localDirectoryText.setLayoutData(localDirectoryGridData);

		GridData skipperGridData = new GridData();
		skipperGridData.grabExcessHorizontalSpace = true;
		skipperGridData.horizontalSpan = 1;
		skipperGridData.horizontalAlignment = SWT.FILL;
		Label skipper = new Label(contents, SWT.NONE); // Just to skip one
														// column
		skipper.setLayoutData(skipperGridData);

		localDirectoryButton = new Button(contents, SWT.PUSH);
		localDirectoryButton.setText(Messages.DownloadRuleDialog_DestinationFrame_FileSystemButton);
		localDirectoryButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				handleLocalDirectoryButtonEvent();
			}
		});
		localWorkspaceButton = new Button(contents, SWT.PUSH);
		localWorkspaceButton.setText(Messages.DownloadRuleDialog_DestinationFrame_WorkspaceButton);
		localWorkspaceButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				handleLocalWorkspaceButtonEvent();
			}
		});

		return frame;
	}

	private Composite createOptionsComposite(Composite parent) {
		FrameMold frameMold = new FrameMold();
		frameMold.setColumns(3);
		frameMold.addOption(FrameMold.COLUMNS_EQUAL_WIDTH);
		frameMold.setTitle(Messages.DownloadRuleDialog_OptionsFrame_Title);
		Frame frame = new Frame(parent, frameMold);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		Composite contents = frame.getComposite();
		asReadOnlyButton = new Button(contents, SWT.CHECK);
		asReadOnlyButton.setText(Messages.DownloadRuleDialog_OptionsFrame_ReadonlyCheck);
		asExecutableButton = new Button(contents, SWT.CHECK);
		asExecutableButton.setText(Messages.DownloadRuleDialog_OptionsFrame_ExecutableCheck);
		preserveTimeStampButton = new Button(contents, SWT.CHECK);
		preserveTimeStampButton.setText(Messages.DownloadRuleDialog_OptionsFrame_PreserveTimeStampCheck);

		ComboMold mold = new ComboMold(ComboMold.GRID_DATA_SPAN);
		mold.setLabel(Messages.DownloadRuleDialog_OptionsFrame_OverwriteLabel);
		mold.setTextFieldWidth(40);
		mold.addItem(Integer.toString(OverwritePolicies.SKIP), Messages.DownloadRuleDialog_OptionsFrame_OverwriteCombo_SkipOption);
		mold.addItem(Integer.toString(OverwritePolicies.ALWAYS),
				Messages.DownloadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteAlwaysOption);
		mold.addItem(Integer.toString(OverwritePolicies.NEWER),
				Messages.DownloadRuleDialog_OptionsFrame_OverwriteCombo_OverwriteIfNewerOption);
		overwritePolicyCombo = new ComboGroup(contents, mold);
		overwritePolicyCombo.getCombo().addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
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

	private Composite createRemoteFilesComposite(Composite parent) {
		FrameMold frameMold = new FrameMold();
		frameMold.setColumns(2);
		frameMold.setTitle(Messages.DownloadRuleDialog_FileListFrame_Title);
		Frame frame = new Frame(parent, frameMold);
		frame.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));

		Frame filesFrame = new Frame(frame.getComposite());
		fileList = new List(filesFrame, SWT.MULTI | SWT.V_SCROLL);
		GridData gridData = new GridData(GridData.VERTICAL_ALIGN_FILL | GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL
				| GridData.GRAB_VERTICAL);
		gridData.heightHint = 200;
		fileList.setLayoutData(gridData);
		fileList.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				removeFilesButton.setEnabled(fileList.getSelectionCount() != 0);
				editFileButton.setEnabled(fileList.getSelectionCount() == 1);
			}
		});
		fileList.deselectAll();

		Frame buttonFrame = new Frame(frame.getComposite());
		addFilesButton = new Button(buttonFrame, SWT.PUSH);
		addFilesButton.setText(Messages.DownloadRuleDialog_FileListFrame_AddButton);
		addFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		addFilesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				handleaddFilesButtonEvent();
			}
		});
		editFileButton = new Button(buttonFrame, SWT.PUSH);
		editFileButton.setText(Messages.DownloadRuleDialog_FileListFrame_EditButton);
		editFileButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		editFileButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				handleEditFileButtonEvent();
			}
		});
		removeFilesButton = new Button(buttonFrame, SWT.PUSH);
		removeFilesButton.setText(Messages.DownloadRuleDialog_FileListFrame_RemoveButton);
		removeFilesButton.setEnabled(false);
		removeFilesButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		removeFilesButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (listenersEnabled < 0) {
					return;
				}
				handleRemoveFilesButtonEvent();
			}

		});
		GridData gridData2 = new GridData();
		gridData2.grabExcessVerticalSpace = true;
		gridData2.verticalAlignment = SWT.TOP;
		buttonFrame.setLayoutData(gridData2);

		return frame;
	}

	private void handleLocalDirectoryButtonEvent() {
		if (listenersEnabled < 0) {
			return;
		}

		IPath selectedPath = null;
		/*
		 * First, try the current path.
		 */
		IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getLocation();
		IPath currentPath = new Path(localDirectoryText.getText());
		if (!currentPath.isAbsolute()) {
			currentPath = workspacePath.append(currentPath).removeTrailingSeparator();
		}
		File file = new File(currentPath.toOSString());
		if (file.isDirectory() && file.isDirectory()) {
			selectedPath = currentPath;
		}

		/*
		 * If not, try last directory.
		 */
		if (selectedPath == null) {
			File file2 = new File(lastSelectedDirectory.toOSString());
			if (file2.isDirectory() && file2.isDirectory()) {
				selectedPath = lastSelectedDirectory;
			}
		}

		/*
		 * Otherwise, use workspace root.
		 */
		if (selectedPath == null) {
			selectedPath = workspacePath;
		}

		DirectoryDialog directoryDialog = new DirectoryDialog(getShell(), SWT.OPEN);
		directoryDialog.setFilterPath(selectedPath.toOSString());
		directoryDialog.setText(Messages.DownloadRuleDialog_DirectoryDialog_Title);
		directoryDialog.setMessage(Messages.DownloadRuleDialog_DirectoryDialog_Message);

		String newPath = directoryDialog.open();

		if (newPath != null) {
			/*
			 * If path is inside the workspace, then make it relative.
			 */
			IPath workspace = ResourcesPlugin.getWorkspace().getRoot().getLocation();
			IPath path = new Path(newPath);
			if (workspace.isPrefixOf(path)) {
				path = path.removeFirstSegments(workspace.segmentCount());
				path = path.makeRelative();
			}
			localDirectoryText.setText(path.toOSString());

			/*
			 * Save lastSelectedDirectory. There is a bug in DirectoryDialog.
			 * The method getFilterPath does not return the expected path. The
			 * bug will be fixed in Eclipse 3.4. The workarount takes the parent
			 * of the selected directory as lastSelectedDirectory.
			 */
			lastSelectedDirectory = new Path(newPath);
			lastSelectedDirectory = lastSelectedDirectory.removeLastSegments(1).removeTrailingSeparator();
		}
	}

	private void handleLocalWorkspaceButtonEvent() {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		IContainer selectedContainer = null;

		/*
		 * First, try the current content as initial selection.
		 */
		IPath currentPath = new Path(localDirectoryText.getText());
		if (currentPath.isAbsolute()) {
			// Absolute path if file system
			IContainer currentContainer[] = workspace.findContainersForLocation(currentPath);
			if (currentContainer != null) {
				if (currentContainer.length == 1) {
					selectedContainer = currentContainer[0];
				}
			}
		} else {
			// Path relative to workspace
			IResource selectedResource = workspace.findMember(lastSelectedDirectory);
			if (selectedResource != null) {
				if (selectedResource instanceof IContainer) {
					selectedContainer = (IContainer) selectedResource;
				}
			}
		}

		/*
		 * Otherwise, try the last selected path as initial selection.
		 */
		if (selectedContainer == null) {
			IContainer lastContainer[] = workspace.findContainersForLocation(lastSelectedDirectory);
			if (lastContainer != null) {
				if (lastContainer.length == 1) {
					selectedContainer = lastContainer[0];
				}
			}
		}

		/*
		 * If nothing matches, use the workspace root.
		 */
		if (selectedContainer == null) {
			selectedContainer = workspace;
		}

		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), workspace, true,
				Messages.DownloadRuleDialog_WorkspaceDialog_Title);
		dialog.setBlockOnOpen(true);
		dialog.setInitialSelections(new Object[] { selectedContainer });

		if (dialog.open() == Dialog.OK) {
			/*
			 * Although not documented, getResult returns an array with exactly
			 * one element, that has type of IPath. The path is relative to
			 * workspace.
			 */
			Object r[] = dialog.getResult();
			IPath selectedPath = (IPath) r[0];
			// The path is returned as absolute! Make it relative!
			localDirectoryText.setText(selectedPath.makeRelative().toOSString());

			lastSelectedDirectory = selectedPath.removeLastSegments(1).removeTrailingSeparator();
		}
	}

	private void handleaddFilesButtonEvent() {
		InputDialog inputDialog = new InputDialog(getShell(), Messages.DownloadRuleDialog_AddFileDialog_Title,
				Messages.DownloadRuleDialog_AddFileDialog_Message, "", null); //$NON-NLS-1$
		inputDialog.setBlockOnOpen(true);
		if (inputDialog.open() == Dialog.OK) {
			ArrayList<String> list = new ArrayList<String>(Arrays.asList(fileList.getItems()));
			list.add(inputDialog.getValue());
			String s[] = new String[list.size()];
			s = list.toArray(s);
			Arrays.sort(s);
			fileList.setItems(s);
		}
	}

	private void handleEditFileButtonEvent() {
		if (fileList.getSelectionCount() != 1) {
			return;
		}
		int index = fileList.getSelectionIndex();
		String selectedItem = fileList.getItem(index);

		InputDialog inputDialog = new InputDialog(getShell(), Messages.DownloadRuleDialog_EditFileDialog_Title,
				Messages.DownloadRuleDialog_EditFileDialog_Message, selectedItem, null);
		inputDialog.setBlockOnOpen(true);
		if (inputDialog.open() == Dialog.OK) {
			String s[] = fileList.getItems();
			s[index] = inputDialog.getValue();
			Arrays.sort(s);
			fileList.setItems(s);
		}
	}

	private void handleRemoveFilesButtonEvent() {
		String selection[] = fileList.getSelection();
		if (selection.length == 0) {
			return;
		}

		HashSet<String> fileSet = new HashSet<String>(Arrays.asList(fileList.getItems()));
		for (String string : selection) {
			fileSet.remove(string);
		}

		String items[] = new String[fileSet.size()];
		items = fileSet.toArray(items);
		Arrays.sort(items);
		fileList.setItems(items);
	}

	public ISynchronizationRule getRuleWorkingCopy() {
		return downloadRule;
	}
}
