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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.ptp.cell.environment.ui.deploy.DeployPlugin;
import org.eclipse.ptp.cell.environment.ui.deploy.debug.Debug;
import org.eclipse.ptp.cell.environment.ui.deploy.events.ExportJobWrapper;
import org.eclipse.ptp.cell.environment.ui.deploy.events.Messages;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.DialogUtil;
import org.eclipse.ui.internal.ide.dialogs.ResourceTreeAndListGroup;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 * Class that creates UI page. 
 * 
 * @author Sean Curry
 * @since 1.1
 */
public class CellExportResourcesPage extends AbstractCellWizardPage {
	
	private ResourceTreeAndListGroup resourceGroup;
	private Label destinationLabel;
	private Text destinationText;
	private Table additionalItemsTable;
	private Button selectAdditionalFileButton;
	private Button selectAdditionalDirButton;
	private Button removeSelectedButton;
	private Button autoOverwriteCheckbox;
	private Button createDirStructure;
	private Button createSelectedDir;
	private int numberOfFiles;

	public CellExportResourcesPage(String pageName, ITargetControl control) {
        super(pageName, control);
        setTitle(Messages.CellExportResourcesPage_0);
        setDescription(Messages.CellExportResourcesPage_1);
    }

	public void createControl(Composite parent) {
		super.createControl(parent);
        destinationText.setFocus();
    }
	
	protected void createAdditionalFilesGroup(Composite parent){
		Font font = parent.getFont();
		
		Group filesGroup = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 1;
		filesGroup.setLayout(groupLayout);
		filesGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		filesGroup.setText(Messages.CellExportResourcesPage_2);
		filesGroup.setFont(font);
		
		additionalItemsTable = new Table(filesGroup, SWT.MULTI);
		GridData tableData = new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL);
		tableData.widthHint = SIZING_TEXT_FIELD_WIDTH + 275;
		tableData.heightHint = SIZING_TEXT_FIELD_WIDTH - 175;
		additionalItemsTable.setLayoutData(tableData);
		additionalItemsTable.setFont(font);
		additionalItemsTable.setLinesVisible(false);
		additionalItemsTable.setHeaderVisible(true);
		additionalItemsTable.setEnabled(true);
		
		TableColumn column1 = new TableColumn(additionalItemsTable, SWT.LEFT, 0);
		TableColumn column2 = new TableColumn(additionalItemsTable, SWT.RIGHT, 1);
		column1.setText(Messages.CellExportResourcesPage_3);
		column1.setWidth(SIZING_TEXT_FIELD_WIDTH + 265);
		column2.setText(Messages.CellExportResourcesPage_4);
		column2.setWidth(10);
		
		Composite comp = new Composite(filesGroup, SWT.NONE);
		GridLayout compLayout = new GridLayout();
		compLayout.numColumns = 3;
		comp.setLayout(compLayout);
		comp.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_FILL));
		
		selectAdditionalFileButton = new Button(comp, SWT.PUSH);
		selectAdditionalFileButton.setText(Messages.CellExportResourcesPage_5);
		selectAdditionalFileButton.addListener(SWT.Selection, this);
		selectAdditionalFileButton.setFont(font);
        setButtonLayoutData(selectAdditionalFileButton);
        
        selectAdditionalDirButton = new Button(comp, SWT.PUSH);
        selectAdditionalDirButton.setText(Messages.CellExportResourcesPage_6);
        selectAdditionalDirButton.addListener(SWT.Selection, this);
        selectAdditionalDirButton.setFont(font);
        setButtonLayoutData(selectAdditionalDirButton);
        
        removeSelectedButton = new Button(comp, SWT.PUSH);
	    removeSelectedButton.setText(Messages.CellExportResourcesPage_7);
	    removeSelectedButton.addListener(SWT.Selection, this);
	    removeSelectedButton.setFont(font);
	    setButtonLayoutData(removeSelectedButton);
	    
	    new Label(parent, SWT.NONE); // vertical spacer
	}
	
	protected void createDestinationGroup(Composite parent) {
		Font font = parent.getFont();
		
		Group destGroup = new Group(parent, SWT.NONE);
		GridLayout groupLayout = new GridLayout();
		groupLayout.numColumns = 2;
		destGroup.setLayout(groupLayout);
		destGroup.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
		destGroup.setText(Messages.CellExportResourcesPage_8);
		destGroup.setFont(font);
		
		destinationLabel = new Label(destGroup, SWT.LEFT);
    	destinationLabel.setText(Messages.CellExportResourcesPage_9);
    	destinationLabel.setFont(font);
    
    	destinationText = new Text(destGroup, SWT.SINGLE | SWT.BORDER);
    	destinationText.addListener(SWT.Modify, this);
    	GridData dirData =	new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.GRAB_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL | GridData.GRAB_VERTICAL);
    	dirData.widthHint = SIZING_TEXT_FIELD_WIDTH + 275;
    	destinationText.setLayoutData(dirData);
    	destinationText.setFont(font);
    	destinationText.setEnabled(true);
		
        new Label(parent, SWT.NONE); // vertical spacer
	}
	
	protected void createOptionsGroupButtons(Group optionsGroup) {
		Font font = optionsGroup.getFont();
        createOverwriteExisting(optionsGroup, font);
        createDirectoryStructureOptions(optionsGroup, font);
    }
	
	protected void createDirectoryStructureOptions(Composite optionsGroup, Font font){
		createDirStructure = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
		createDirStructure.setText(Messages.CellExportResourcesPage_10);
		createDirStructure.setFont(font);

        // create directory structure radios
		createSelectedDir = new Button(optionsGroup, SWT.RADIO
                | SWT.LEFT);
        createSelectedDir.setText(Messages.CellExportResourcesPage_11);
        createSelectedDir.setFont(font);
	}
	
	protected void createOverwriteExisting(Group optionsGroup, Font font) {
		autoOverwriteCheckbox = new Button(optionsGroup, SWT.CHECK | SWT.LEFT);
		autoOverwriteCheckbox.setText(Messages.CellExportResourcesPage_12);
		autoOverwriteCheckbox.setFont(font);
		autoOverwriteCheckbox.setEnabled(true);
	}
	
	protected void createResourcesGroup(Composite parent) {

        //create the input element, which has the root resource
        //as its only child
        List input = new ArrayList();
        IProject[] projects = ResourcesPlugin.getWorkspace().getRoot()
                .getProjects();
        for (int i = 0; i < projects.length; ++i) {
            if (projects[i].isOpen())
                input.add(projects[i]);
        }
        
        Group filesGroup = new Group(parent, SWT.NONE);
        filesGroup.setLayout(parent.getLayout());
        filesGroup.setLayoutData(parent.getLayoutData());
        filesGroup.setText(Messages.CellExportResourcesPage_13);
        filesGroup.setFont(parent.getFont());
        
        this.resourceGroup = new ResourceTreeAndListGroup(filesGroup, input,
                getResourceProvider(IResource.FOLDER | IResource.PROJECT),
                WorkbenchLabelProvider.getDecoratingWorkbenchLabelProvider(),
                getResourceProvider(IResource.FILE), WorkbenchLabelProvider
                        .getDecoratingWorkbenchLabelProvider(), SWT.NONE,
                DialogUtil.inRegularFontMode(parent));
        
        createAdditionalFilesGroup(parent);
    }
	
	public boolean finish() {
		List whiteResources = getWhiteCheckedResources();
		
		List resourcesToCopy = getSelectedResources();
		
		//if no files are selected, notify user, and cancel "Finish" operation //$NON-NLS-1$
        if (resourcesToCopy.isEmpty()){
        	MessageDialog.openInformation(getContainer().getShell(), Messages.CellExportResourcesPage_14, Messages.CellExportResourcesPage_15);
            return false;
        }  	
		
		//Save dirty editors if possible but do not stop if not all are saved
        saveDirtyEditors();
        //about to invoke the operation so save our state
        saveWidgetValues();        
		
		try {
			//create copy job wrapper class, and initialize its data. 
			ExportJobWrapper copyWrapper = new ExportJobWrapper();
			ProgressMonitorDialog jobMonitor = new ProgressMonitorDialog(getContainer().getShell());
			if(createDirStructure.getSelection()){
				copyWrapper.setCreateDirStructure(true);
				copyWrapper.init(cellControl, resourcesToCopy, getDestinationValue(), this, numberOfFiles);
			}
			else
				copyWrapper.init(cellControl, whiteResources, getDestinationValue(), this, numberOfFiles);
			copyWrapper.setAutoOverwrite(autoOverwriteCheckbox.getSelection());
				
			jobMonitor.setOpenOnRun(true);
			jobMonitor.run(true, true, copyWrapper);
			
		} catch (InvocationTargetException e) {
			if(e.getTargetException() == null){
				MessageDialog.openError(getContainer().getShell(), Messages.CellExportResourcesPage_16, e.getMessage());
				return false;
			}
			Debug.POLICY.logError(e);
			MessageDialog.openError(getContainer().getShell(), e.getMessage(), e.getTargetException().toString());
			return false;
		} catch (InterruptedException e) {
			MessageDialog.openInformation(getContainer().getShell(), Messages.CellExportResourcesPage_17, Messages.CellExportResourcesPage_18);
			return false;
		}
		
		MessageDialog.openInformation(getContainer().getShell(), Messages.CellExportResourcesPage_19, 
		Messages.CellExportResourcesPage_20);
		return true;		        
	}
	
	protected String getDestinationValue() {
		String ret = destinationText.getText().trim();
		if(ret.length() < 2)
			return ret;
		if(ret.charAt(ret.length()-1) == File.separatorChar)
			return ret.substring(0, ret.length()-1);
		else
			return ret;
	}

	private ITreeContentProvider getResourceProvider(final int resourceType) {
        return new WorkbenchContentProvider() {
            public Object[] getChildren(Object o) {
                if (o instanceof IContainer) {
                    IResource[] members = null;
                    try {
                        members = ((IContainer) o).members();
                    } catch (CoreException e) {
                        //just return an empty set of children
                        return new Object[0];
                    }

                    //filter out the desired resource types
                    ArrayList results = new ArrayList();
                    for (int i = 0; i < members.length; ++i) {
                        //And the test bits with the resource types to see if they are what we want
                        if ((members[i].getType() & resourceType) > 0) {
                            results.add(members[i]);
                        }
                    }
                    return results.toArray();
                } else {
                    //input element case
                    if (o instanceof ArrayList) {
                        return ((ArrayList) o).toArray();
                    } else {
                        return new Object[0];
                    }
                }
            }
        };
    }
	
	protected List getSelectedResources() {
		numberOfFiles = 0;
		
        Iterator iterator = this.getSelectedResourcesIterator();
        List resources = new ArrayList();
        
        //add files selected from the workspace
        while (iterator.hasNext()){
        	resources.add(iterator.next());
        	numberOfFiles++;
        }
        
        //add files selected from outside the workspace
        TableItem[] items = additionalItemsTable.getItems();
        for(int i = 0; i < items.length; ++i){
        	File file = new File(items[i].getText(0));
        	resources.add(file);
        	if(file.isDirectory())
        		tallyDirectory(file);
        	else
        		numberOfFiles++;
        }
        return resources;
    }
	
	private void tallyDirectory(File file){		
		File[] files = file.listFiles();
		
		for(int i = 0; i < files.length; ++i){
			if(files[i].isDirectory())
				tallyDirectory(files[i]);
			else
				numberOfFiles++;
		}
	}
	
	protected Iterator getSelectedResourcesIterator() {
        return this.resourceGroup.getAllCheckedListItems().iterator();
    }
	
	protected List getWhiteCheckedResources() {
		List resources = this.resourceGroup.getAllWhiteCheckedItems();
		
		//add files and folders selected from outside the workspace
        TableItem[] items = additionalItemsTable.getItems();
        for(int i = 0; i < items.length; ++i){
        	resources.add(new File(items[i].getText(0)));
        }		
        return resources;
    }

	public void handleEvent(Event e) {
		try {
			Widget source = e.widget;
			if(source == selectAdditionalFileButton){
				handleAddFilePressed();
			}
			else if(source == selectAdditionalDirButton){
				handleAddDirPressed();
			}
			else if(source == removeSelectedButton){
				handleRemoveSelectedPressed();
			}
			updatePageCompletion();
		} catch (Exception ee) {
    		Debug.POLICY.logError(ee);
    	}
	}
	
	private void handleRemoveSelectedPressed() {
		int[] selected = additionalItemsTable.getSelectionIndices();
		additionalItemsTable.deselectAll();
		additionalItemsTable.remove(selected);		
	}
	
	private void handleAddDirPressed(){
		DirectoryDialog dialog = new DirectoryDialog(getShell(), SWT.SAVE);
		dialog.setMessage(Messages.CellExportResourcesPage_21);
		dialog.setFilterPath(""); //$NON-NLS-1$
		String selectedDirName = dialog.open().trim();
		
		if(selectedDirName != null){
			File file = new File(selectedDirName);
			if(!(file.exists())){
				setErrorMessage(Messages.CellExportResourcesPage_22);
				return;
			}
			//check if the directory has already been added to the table
			if(!doesAdditionalItemsContain(selectedDirName)){
				TableItem item = new TableItem(additionalItemsTable, SWT.LEFT);
				item.setText(0, selectedDirName);
				item.setText(1, "Directory"); //$NON-NLS-1$
			}
		}		
	}
	
	private void handleAddFilePressed(){
		FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
        dialog.setText(Messages.CellExportResourcesPage_23);
        dialog.setFilterPath(""); //$NON-NLS-1$
        String selectedFileName = dialog.open().trim();

        if (selectedFileName != null) {
        	File file = new File(selectedFileName);
        	if(!file.exists()){
        		setErrorMessage(Messages.CellExportResourcesPage_24);
        		return;
        	}
        	      
            //check if the file has already been added to the table
        	if(!doesAdditionalItemsContain(selectedFileName)){
        		TableItem item = new TableItem(additionalItemsTable, SWT.NONE);
        		item.setText(0, selectedFileName);
        		item.setText(1, "File"); //$NON-NLS-1$
			}
        }
	}
	
	protected void restoreWidgetValues() {
		IDialogSettings settings = DeployPlugin.getDefault().getDialogSettings();
		
		autoOverwriteCheckbox.setSelection(settings.getBoolean(DeployPlugin.SETTING_EXPORT_OVERWRITE));
		createDirStructure.setSelection(settings.getBoolean(DeployPlugin.SETTING_EXPORT_CREATE_DIR_STRUCTURE));
		createSelectedDir.setSelection(settings.getBoolean(DeployPlugin.SETTING_EXPORT_CREATE_SELECTED_DIR));
		destinationText.setText(settings.get(DeployPlugin.SETTING_EXPORT_DESTINATION));
	}
	
	protected boolean saveDirtyEditors() {
        return PlatformUI.getWorkbench().saveAllEditors(true);
    }
	
	protected void saveWidgetValues(){
		IDialogSettings settings = DeployPlugin.getDefault().getDialogSettings();
		
		settings.put(DeployPlugin.SETTING_EXPORT_OVERWRITE, autoOverwriteCheckbox.getSelection());
		settings.put(DeployPlugin.SETTING_EXPORT_CREATE_DIR_STRUCTURE, createDirStructure.getSelection());
		settings.put(DeployPlugin.SETTING_EXPORT_CREATE_SELECTED_DIR, createSelectedDir.getSelection());
		settings.put(DeployPlugin.SETTING_EXPORT_DESTINATION, getDestinationValue());
	}
	
	protected boolean validateDestinationGroup() {
		String destinationValue = getDestinationValue();
		if (destinationValue.length() == 0) {
            setMessage(Messages.CellExportResourcesPage_25);
            return false;
        }
		setMessage(null);
		setErrorMessage(null);
		return true;
	}
	
	protected boolean validateSourceGroup() {
        return true;
    }
	
	private boolean doesAdditionalItemsContain(String item){
		if(item == null || item.length() == 0)
			return true;		
		
		TableItem[] items = additionalItemsTable.getItems();
		for(int i = 0; i < items.length; ++i){
			if((items[i].getText(0) != null) && (items[i].getText(0).equals(item)))
				return true;
		} 
		return false;
	}	
}//end of class CellWizardCopyToEnvPage
