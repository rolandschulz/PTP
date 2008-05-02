/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.ui;

import java.io.File;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

/**
 *
 */
public class WorkingDirectoryBlock extends LaunchConfigurationTab {
    protected Button useDefaultWorkingDirButton = null;
    protected Button localDirButton = null;
    protected Text workingDirText = null;
    protected Button workingDirBrowseButton = null;
    protected Button workspaceDirButton = null;
    protected Text workspaceDirText = null;
    protected Button workspaceDirBrowseButton = null;
	protected ILaunchConfiguration launchConfiguration;

    protected class WidgetListener extends SelectionAdapter implements ModifyListener {
	    public void widgetSelected(SelectionEvent e) {
	        Object source = e.getSource();
	        if (source == useDefaultWorkingDirButton)
	            handleUseDefaultWorkingDirButtonSelected();
	        else if (source == localDirButton || source == workspaceDirButton) {
	            handleLocationButtonSelected();
	    		updateLaunchConfigurationDialog();
	        } else if (source == workingDirBrowseButton)
	            handleWorkingDirBrowseButtonSelected();
	        else if (source == workspaceDirBrowseButton)
	            handleWorkspaceDirBrowseButtonSelected();
	        else
	            updateLaunchConfigurationDialog();
	    }
		public void modifyText(ModifyEvent evt) {
			updateLaunchConfigurationDialog();
		}        
    }
    
    protected WidgetListener listener = new WidgetListener();

    /**
     * @see ILaunchConfigurationTab#createControl(Composite)
     */
    public void createControl(Composite parent) {
		Composite workingDirComp = new Composite(parent, SWT.NONE);
		workingDirComp.setLayout(createGridLayout(3, false, 0, 0));
		workingDirComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		setControl(workingDirComp);
		
		Label workingDirLabel = new Label(workingDirComp, SWT.NONE);
		workingDirLabel.setText(LaunchMessages.getResourceString("WorkingDirectoryBlock.Working_directory_colon"));
		workingDirLabel.setLayoutData(spanGridData(-1, 3));

		useDefaultWorkingDirButton = new Button(workingDirComp, SWT.CHECK);
		useDefaultWorkingDirButton.setText(LaunchMessages.getResourceString("WorkingDirectoryBlock.Use_de&fault_working_directory"));
		useDefaultWorkingDirButton.setLayoutData(spanGridData(-1, 3));
		useDefaultWorkingDirButton.addSelectionListener(listener);
		
		localDirButton = createRadioButton(workingDirComp, LaunchMessages.getResourceString("WorkingDirectoryBlock.&Local_directory"));
		localDirButton.addSelectionListener(listener);
				
		workingDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		workingDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		workingDirText.addModifyListener(listener);
		
		workingDirBrowseButton = createPushButton(workingDirComp, LaunchMessages.getResourceString("Tab.common.&Browse_1"), null);
		workingDirBrowseButton.addSelectionListener(listener);
		
		workspaceDirButton = createRadioButton(workingDirComp, LaunchMessages.getResourceString("WorkingDirectoryBlock.Works&pace"));
		workspaceDirButton.addSelectionListener(listener);
		
		workspaceDirText = new Text(workingDirComp, SWT.SINGLE | SWT.BORDER);
		workspaceDirText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		workspaceDirText.addModifyListener(listener);
		
		workspaceDirBrowseButton = createPushButton(workingDirComp, LaunchMessages.getResourceString("Tab.common.B&rowse_2"), null);
		workspaceDirBrowseButton.addSelectionListener(listener);        
    }

    /**
     * Defaults are empty.
     * 
     * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String) null);
    }
    
    /**
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        setLaunchConfiguration(configuration);
        try {            
			String wd = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String) null);
			workspaceDirText.setText(EMPTY_STRING);
			workingDirText.setText(EMPTY_STRING);
			if (wd == null) {
				useDefaultWorkingDirButton.setSelection(true);
			} else {
				IPath path = new Path(wd);
				if (path.isAbsolute()) {
					workingDirText.setText(wd);
					localDirButton.setSelection(true);
					workspaceDirButton.setSelection(false);
				} else {
					workspaceDirText.setText(wd);
					workspaceDirButton.setSelection(true);
					localDirButton.setSelection(false);
				}
				useDefaultWorkingDirButton.setSelection(false);
			}
			handleUseDefaultWorkingDirButtonSelected();            
        } catch (CoreException e) {
            setErrorMessage(LaunchMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }
    
    /**
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		String wd = null;
		if (!isDefaultWorkingDirectory()) {
			if (isLocalWorkingDirectory()) {
				wd = getFieldContent(workingDirText.getText());
			} else {
				IPath path = new Path(workspaceDirText.getText());
				wd = path.makeRelative().toOSString();
			}
		} 
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, wd);
    }    

    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
        
		if (isLocalWorkingDirectory()) {		    
			String workingDirPath = getFieldContent(workingDirText.getText());
			if (workingDirPath != null) {
				File dir = new File(workingDirPath);
				if (!dir.exists()) {
					setErrorMessage(LaunchMessages.getResourceString("WorkingDirectoryBlock.Working_directory_does_not_exist"));
					return false;
				}
				if (!dir.isDirectory()) {
					setErrorMessage(LaunchMessages.getResourceString("WorkingDirectoryBlock.Working_directory_is_not_a_directory"));
					return false;
				}
			}
		} else {
			if (getContainer(workingDirText.getText()) == null) {
				setErrorMessage(LaunchMessages.getResourceString("WorkingDirectoryBlock.Project_or_folder_does_not_exist"));
				return false;
			}
		}		
		return true;
	}    
    
    /**
     * @see ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return LaunchMessages.getResourceString("WorkingDirectoryBlock.Working_directory");
    }

    /**
     * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
     */
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
    }

    /**
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
     */
    protected void updateLaunchConfigurationDialog() {
        super.updateLaunchConfigurationDialog();
    }
    
	/**
	 * Returns whether the default working directory is to be used
	 */
	protected boolean isDefaultWorkingDirectory() {
		return useDefaultWorkingDirButton.getSelection();
	}

	/**
	 * Returns whether the working directory is local
	 */
	protected boolean isLocalWorkingDirectory() {
		return localDirButton.getSelection();
	}
	
	/**
	 * Sets the default working directory
	 */
	protected void setDefaultWorkingDir(ILaunchConfiguration configuration) {
		if (configuration == null) {
			workingDirText.setText(System.getProperty("user.dir"));
			localDirButton.setSelection(true);
			workspaceDirButton.setSelection(false);
			return;
		}

		IProject project = getProject(configuration);
		if (project == null)
		    return;
		
		workspaceDirText.setText(project.getFullPath().makeRelative().toString());
		localDirButton.setSelection(false);
		workspaceDirButton.setSelection(true);
	}
	
	/**
	 * The "local directory" or "workspace directory" button has been selected.
	 */
	protected void handleLocationButtonSelected() {
		if (!isDefaultWorkingDirectory()) {
			boolean local = isLocalWorkingDirectory();
			workingDirText.setEnabled(local);
			workingDirBrowseButton.setEnabled(local);
			workspaceDirText.setEnabled(!local);
			workspaceDirBrowseButton.setEnabled(!local);
		}
		updateLaunchConfigurationDialog();
	}

	/**
	 * The default working dir check box has been toggled.
	 */
	protected void handleUseDefaultWorkingDirButtonSelected() {
		if (isDefaultWorkingDirectory()) {
			setDefaultWorkingDir(getLaunchConfiguration());
			localDirButton.setEnabled(false);
			workingDirText.setEnabled(false);
			workingDirBrowseButton.setEnabled(false);
			workspaceDirButton.setEnabled(false);
			workspaceDirText.setEnabled(false);
			workspaceDirBrowseButton.setEnabled(false);
		} else {
			localDirButton.setEnabled(true);
			workspaceDirButton.setEnabled(true);
			handleLocationButtonSelected();
		}
	}
	
	/**
	 * Show a dialog that lets the user select a working directory
	 */
	protected void handleWorkingDirBrowseButtonSelected() {
		DirectoryDialog dialog = new DirectoryDialog(getShell());
		dialog.setMessage(LaunchMessages.getResourceString("WorkingDirectoryBlock.Select_&working_directory_for_launch_configuration"));
		String currentWorkingDir = workingDirText.getText();
		if (currentWorkingDir != null && !currentWorkingDir.trim().equals(EMPTY_STRING)) {
			File path = new File(currentWorkingDir);
			if (path.exists())
				dialog.setFilterPath(currentWorkingDir);
		}
		
		String selectedDirectory = dialog.open();
		if (selectedDirectory != null)
			workingDirText.setText(selectedDirectory);
	}
	
	/**
	 * Show a dialog that lets the user select a working directory from 
	 * the workspace
	 */
	protected void handleWorkspaceDirBrowseButtonSelected() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), getWorkspaceRoot(), false, LaunchMessages.getResourceString("WorkingDirectoryBlock.Select_&workspace_relative_working_directory"));		
		IContainer currentContainer = getContainer(workspaceDirText.getText());
		if (currentContainer != null) {
			IPath path = currentContainer.getFullPath();
			dialog.setInitialSelections(new Object[] {path});
		}
		
		dialog.showClosedProjects(false);
		dialog.open();
		Object[] results = dialog.getResult();		
		if ((results != null) && (results.length > 0) && (results[0] instanceof IPath)) {
			IPath path = (IPath)results[0];
			String containerName = path.makeRelative().toString();
			workspaceDirText.setText(containerName);
		}			
	}
    
    /**
     * @return Returns the launchConfiguration.
     */
    public ILaunchConfiguration getLaunchConfiguration() {
        return launchConfiguration;
    }
    /**
     * @param launchConfiguration The launchConfiguration to set.
     */
    public void setLaunchConfiguration(ILaunchConfiguration launchConfiguration) {
        this.launchConfiguration = launchConfiguration;
    }	
}
