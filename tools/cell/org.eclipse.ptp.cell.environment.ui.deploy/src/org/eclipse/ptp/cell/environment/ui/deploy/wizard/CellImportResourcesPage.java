/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */

package org.eclipse.ptp.cell.environment.ui.deploy.wizard;

import java.io.File;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.ptp.cell.environment.ui.deploy.DeployPlugin;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.events.AbstractCellTargetJob;
import org.eclipse.ptp.cell.environment.ui.deploy.events.ImportJobWrapper;
import org.eclipse.ptp.cell.environment.ui.deploy.events.Messages;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;


public class CellImportResourcesPage extends AbstractCellWizardPage {

	class VerifyPathTargetJob extends AbstractCellTargetJob {

		String path;
		boolean isDir;
		boolean hasPath;
		
		public VerifyPathTargetJob(String p, boolean isDirectory){
			this.path = p;
			this.isDir = isDirectory;
			this.hasPath = false;
		}
		
		public void run() {
			try {
				IRemoteFileTools fileTools = executionManager.getRemoteFileTools();
				if(isDir)
					hasPath = fileTools.hasDirectory(path);
				else
					hasPath = fileTools.hasFile(path);
			} catch (RemoteConnectionException e) {
				exception = e;
				errorMessage = Messages.CellImportResourcesPage_0;
				hadError = true;
			} catch (RemoteOperationException e) {
				exception = e;
				errorMessage = Messages.CellImportResourcesPage_1;
				hadError = true;
			} catch (CancelException e) {
				exception = e;
				errorMessage = Messages.CellImportResourcesPage_2;
				hadError = true;
			}			
		}
		
		public boolean doesPathExist(){	return hasPath;	}
	}
	
	private Table resourceTable;
	private Button addFileButton;
	private Button addDirButton;
	private Button removeSelectedButton;
	private Text destinationNameField;
	private Button destinationBrowseButton;
	private Button createSelectedDir;
	private Button copyDirContentsOnly;
	private boolean sourceValid = true;
	
	public CellImportResourcesPage(String pageName, ITargetControl control) {
		super(pageName, control);
		setTitle(Messages.CellImportResourcesPage_3);
		setDescription(Messages.CellImportResourcesPage_4);
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
        destinationNameField.setFocus();
	}
	
	protected void createAdditionalFilesGroup(Composite parent) {
		Font font = parent.getFont();
		
		Group filesGroup = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		filesGroup.setLayout(groupLayout);
		filesGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL));
		filesGroup.setText(Messages.CellImportResourcesPage_5);
		filesGroup.setFont(font);
		
		resourceTable = new Table(filesGroup, SWT.MULTI);
		GridData tableData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tableData.widthHint = SIZING_TEXT_FIELD_WIDTH + 300;
		tableData.heightHint = SIZING_TEXT_FIELD_WIDTH - 175;
		resourceTable.setLayoutData(tableData);
		resourceTable.setFont(font);
		resourceTable.setLinesVisible(false);
		resourceTable.setHeaderVisible(true);
		resourceTable.setEnabled(true);
		
		TableColumn column1 = new TableColumn(resourceTable, SWT.LEFT, 0);
		TableColumn column2 = new TableColumn(resourceTable, SWT.RIGHT, 1);
		column1.setText(Messages.CellImportResourcesPage_6);
		column1.setWidth(SIZING_TEXT_FIELD_WIDTH + 250);
		column2.setText(Messages.CellImportResourcesPage_7);
		column2.setWidth(25);
		
		Composite comp = new Composite(filesGroup, SWT.NONE);
		GridLayout compLayout = new GridLayout();
		compLayout.numColumns = 3;
		comp.setLayout(compLayout);
		comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL));
		
		addFileButton = new Button(comp, SWT.PUSH);
		addFileButton.setText(Messages.CellImportResourcesPage_8);
		addFileButton.addListener(SWT.Selection, this);
		addFileButton.setFont(font);
        setButtonLayoutData(addFileButton);
        
        addDirButton = new Button(comp, SWT.PUSH);
        addDirButton.setText(Messages.CellImportResourcesPage_9);
        addDirButton.addListener(SWT.Selection, this);
        addDirButton.setFont(font);
        setButtonLayoutData(addDirButton);
        
        removeSelectedButton = new Button(comp, SWT.PUSH);
	    removeSelectedButton.setText(Messages.CellImportResourcesPage_10);
	    removeSelectedButton.addListener(SWT.Selection, this);
	    removeSelectedButton.setFont(font);
	    setButtonLayoutData(removeSelectedButton);
	    
	    new Label(parent, SWT.NONE); // vertical spacer
	}
	
	protected void createDestinationGroup(Composite parent) {
		Font font = parent.getFont();
		
		//Group which contains composite for copying to directory, and composite for project.
		Composite destination = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		layout.numColumns = 3;
		destination.setLayout(layout);
		destination.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destination.setFont(font);
		
		Label destinationLabel = new Label(destination, SWT.NONE);
		destinationLabel.setText(Messages.CellImportResourcesPage_11);
		destinationLabel.setFont(font);
		
		destinationNameField = new Text(destination, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
		destinationNameField.addListener(SWT.Modify, this);
		GridData data = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		data.widthHint = SIZING_TEXT_FIELD_WIDTH;
		destinationNameField.setLayoutData(data);
		destinationNameField.setFont(font);
		
		destinationBrowseButton = new Button(destination, SWT.PUSH);
		destinationBrowseButton.setText(Messages.CellImportResourcesPage_12);
		destinationBrowseButton.addListener(SWT.Selection, this);
		destinationBrowseButton.setFont(font);
		setButtonLayoutData(destinationBrowseButton);
		
        new Label(parent, SWT.NONE); // vertical spacer
	}
	
	protected void createOptionsGroupButtons(Group optionsGroup) {
		Font font = optionsGroup.getFont();
		
		createDirectoryStructureOptions(optionsGroup, font);
	}
	
	protected void createDirectoryStructureOptions(Composite optionsGroup, Font font){
		//create directory structure radios
		createSelectedDir = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
        createSelectedDir.setText(Messages.CellImportResourcesPage_13);
        createSelectedDir.setFont(font);
        createSelectedDir.setSelection(true);
		
		copyDirContentsOnly = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
		copyDirContentsOnly.setText(Messages.CellImportResourcesPage_14);
		copyDirContentsOnly.setFont(font);
	}
	
	protected void createResourcesGroup(Composite parent) {
		createAdditionalFilesGroup(parent);
	}
	
	private boolean doesResourceTableContain(String name) {
		if(name == null || name.length() == 0)
			return true;		
		
		TableItem[] items = resourceTable.getItems();
		for(int i = 0; i < items.length; ++i){
			if((items[i].getText(0) != null) && (items[i].getText(0).equals(name)))
				return true;
		} 
		return false;
	}
	
	public boolean finish() {
		String[][] resources = getResources();
		String destination = getDestinationValue();
		
		if(resources.length < 1){
			MessageDialog.openInformation(getContainer().getShell(), Messages.CellImportResourcesPage_15, Messages.CellImportResourcesPage_16);
            return false;
		}
		
		File destDir = new File(destination);
		//Make sure target directory exists. If it does not, prompt user for creation of the directory.
		if(!destDir.exists()){
			boolean create = MessageDialog.openQuestion(getContainer().getShell(), Messages.CellImportResourcesPage_17, 
					Messages.CellImportResourcesPage_18);
			if(create)
				destDir.mkdirs();
			else
				return false;
		}
		//Check if the specified path exists, but is a file. If so, prompt user for deletion of file, and creation of the directory.
		if(destDir.exists() && destDir.isFile()){
			boolean createDir = MessageDialog.openQuestion(getContainer().getShell(), Messages.CellImportResourcesPage_19, 
					Messages.CellImportResourcesPage_20);
			if(createDir){
				destDir.delete();
				destDir.mkdirs();
			}
			else
				return false;
		}
		//Make sure that we can write to the target directory
		if(!destDir.canWrite()){
			MessageDialog.openInformation(getContainer().getShell(), Messages.CellImportResourcesPage_21, 
					Messages.CellImportResourcesPage_22);
			return false;
		}
		
		//Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        //about to invoke the operation so save our state
        saveWidgetValues(); 
		
        try{        	
        	//create copy job wrapper class, and initialize its data. 
			ImportJobWrapper copyWrapper = new ImportJobWrapper();
			ProgressMonitorDialog jobMonitor = new ProgressMonitorDialog(getContainer().getShell());
			copyWrapper.init(cellControl, resources, destination, this);
			copyWrapper.setCopyEntireDir(createSelectedDir.getSelection());
			
			jobMonitor.setOpenOnRun(true);
			jobMonitor.run(true, true, copyWrapper);
        	
        } catch (InvocationTargetException e) {
			if (e.getTargetException() == null) {
				MessageDialog.openError(getContainer().getShell(), Messages.CellImportResourcesPage_23, e.getMessage());
				return false;
			}
			Debug.POLICY.logError(e);
			MessageDialog.openError(getContainer().getShell(), e.getMessage(), e.getTargetException().toString());
			return false;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(getContainer().getShell(), Messages.CellImportResourcesPage_24, Messages.CellImportResourcesPage_25);
			return false;
		}
		
		MessageDialog.openInformation(getContainer().getShell(), Messages.CellImportResourcesPage_26, 
		Messages.CellImportResourcesPage_27);
		return true;
	}
	
	protected String getDestinationValue() {
		String path = destinationNameField.getText().trim();
		return removeTrailingSlashes(path);
	}
	
	private String[][] getResources(){
		TableItem[] items = resourceTable.getItems();
		String[][] resources = new String[items.length][2];
		for(int i = 0; i < items.length; ++i){
			resources[i] = new String[]{items[i].getText(0), items[i].getText(1)};
		}
		return resources;
	}
	
	private void handleAddDirPressed() {
		InputDialog dialog = new InputDialog(getContainer().getShell(), Messages.CellImportResourcesPage_28, Messages.CellImportResourcesPage_29, Messages.CellImportResourcesPage_30, null);
		dialog.open();
		String path = dialog.getValue().trim();
		if(path != null && path.length() > 0){
			String newPath = removeTrailingSlashes(path);
			
			if(validatePath(newPath, true)){
				if(!doesResourceTableContain(newPath)){
					TableItem item = new TableItem(resourceTable, SWT.LEFT);
					item.setText(0, newPath);
					item.setText(1, "Directory"); //$NON-NLS-1$
				}
			}
		}
		else{
			setErrorMessage(Messages.CellImportResourcesPage_31);
			sourceValid = false;
		}
	}
	
	private void handleAddFilePressed() {
		InputDialog dialog = new InputDialog(getContainer().getShell(), Messages.CellImportResourcesPage_32, Messages.CellImportResourcesPage_33, Messages.CellImportResourcesPage_34, null);
		dialog.open();
		String path = dialog.getValue().trim();
		if(path != null && path.length() > 0){
			if(validatePath(path, false)){
				if(!doesResourceTableContain(path)){
					TableItem item = new TableItem(resourceTable, SWT.LEFT);
					item.setText(0, path);
					item.setText(1, "File"); //$NON-NLS-1$
				}
			}
		}
		else{
			setErrorMessage(Messages.CellImportResourcesPage_35);
			sourceValid = false;
		}
	}
	
	private void handleRemoveSelectedPressed() {
		int[] selected = resourceTable.getSelectionIndices();
		resourceTable.deselectAll();
		resourceTable.remove(selected);
	}
		
	protected void handleDestinationBrowseButtonPressed() {
        DirectoryDialog dialog = new DirectoryDialog(getContainer().getShell(), SWT.SAVE);
        dialog.setMessage(Messages.CellImportResourcesPage_36);
        dialog.setText(Messages.CellImportResourcesPage_37);
        dialog.setFilterPath(getDestinationValue());
        String selectedDirectoryName = dialog.open();

        if (selectedDirectoryName != null) {
            destinationNameField.setText(selectedDirectoryName);
        }
    }
	
	public void handleEvent(Event e) {
		try {
		Widget source = e.widget;
		
		if(source == destinationBrowseButton){
			handleDestinationBrowseButtonPressed();
		}
		else if(source == addFileButton){ //handle Add File pressed
			handleAddFilePressed();
		}
		else if(source == addDirButton){
			handleAddDirPressed();
		}
		else if(source == removeSelectedButton){ //handle Remove File pressed
			handleRemoveSelectedPressed();
		}
			
		updatePageCompletion();
		if (sourceValid)
			setErrorMessage(null);
		else
			sourceValid = true;
		} catch (Exception ee) {
			Debug.POLICY.logError(ee);
		}
	}
	
	protected boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }
	
	protected boolean validateDestinationGroup() {
		String dest = getDestinationValue().trim();
		if (dest.length() == 0) {
            setMessage(Messages.CellImportResourcesPage_38);
            return false;
        }
		setMessage(null);
		return true;
	}
	
	protected boolean validateOptionsGroup() {
        return true;
    }
	
	protected boolean validatePath(String path, boolean isDir){
		VerifyPathTargetJob verifyJob = new VerifyPathTargetJob(path, isDir);
		try {
			cellControl.startJob(verifyJob);
			verifyJob.waitFor();
			
			if(verifyJob.didHaveError()){
				MessageDialog.openError(getContainer().getShell(), verifyJob.getErrorMessage(), verifyJob.getException().toString());
				return false;
			}
			boolean exists = verifyJob.doesPathExist();
			if(!exists){
				if(isDir){
					sourceValid = false;
					setErrorMessage(Messages.CellImportResourcesPage_39);
				}
				else{
					sourceValid = false;
					setErrorMessage(Messages.CellImportResourcesPage_40);
				}
			}
			return exists;
		} catch (CoreException e) {
			MessageDialog.openError(getContainer().getShell(), Messages.CellImportResourcesPage_41, Messages.CellImportResourcesPage_42);
		} catch (InterruptedException e) {
			MessageDialog.openError(getContainer().getShell(), Messages.InterruptedErrorMessageDialogTitle, Messages.InterruptedErrorMessageDialogDescription);
		}
		
		return false;
	}
	
	protected boolean validateSourceGroup() {
		return sourceValid;
    }
	
	private String removeTrailingSlashes(String path){
		if(path == null || path.length() == 0)
			return ""; //$NON-NLS-1$
		
		if(path.charAt(path.length() - 1) == '/')
			return removeTrailingSlashes(path.substring(0, path.length() - 1));
		else
			return path;
		
	}
	
	protected void restoreWidgetValues() {
		IDialogSettings settings = DeployPlugin.getDefault().getDialogSettings();
		
		destinationNameField.setText(settings.get(DeployPlugin.SETTING_IMPORT_DESTINATION));
	}
	
	protected void saveWidgetValues(){
		IDialogSettings settings = DeployPlugin.getDefault().getDialogSettings();
		
		settings.put(DeployPlugin.SETTING_IMPORT_DESTINATION, getDestinationValue());
	}
}
