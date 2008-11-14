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

package org.eclipse.ptp.cell.alf.ui.wizard;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.cell.alf.ui.Messages;
//import org.eclipse.ptp.cell.utils.packagemanager.PackageManagementSystemManager;
//import org.eclipse.ptp.cell.utils.packagemanager.PackageManager;
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;


/**
 * This class is used to extend the standard WizardNewProjectCreationWizard page, because the ALF Wizard
 * will be creating two projects named ppu_<project_name> and spu_<project_name>. This wizard page must 
 * make sure there are no current projects in the workspace with these two names. The standard
 * WizardNewProjectCreationPage only checks to make sure there are no projects named <project_name>. 
 * 
 * @author spcurry
 */
public class ALFWizardNewProjectCreationPage extends WizardNewProjectCreationPage {
	
	/**
	 * Flag used to check if the RPM dependencies validation was made, so we can avoid
	 * redoing it again. If true, then the result of validation can be 
	 * retrieved by the value of "RPMValidated" flag.
	 */
	private boolean RPMsChecked = false;
	
	/**
	 * Flag that represents the state of the last RPM dependencies validation. Must
	 * be taken into account only if the "RPMCheck" flag is true. 
	 */
	private boolean RPMsValidated = false;
	
	/**
	 * This string stores the RPM error message generated. Thus it`s possible to update
	 * the upper info panel of the wizard without repeating the whole verification
	 * again.
	 */
	private String RPMErrorMessage = "";
	
	public ALFWizardNewProjectCreationPage(String pageName) {
		super(pageName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ui.dialogs.WizardNewProjectCreationPage#validatePage()
	 */
	protected boolean validatePage() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();

		if (! RPMsChecked) 
		{
			/*
			 * Do the RPM check.
			 */
			/*PackageManager manager = PackageManagementSystemManager.getPackageManager();
			boolean alfPackage1 = manager.query("alf-cross-devel"); //$NON-NLS-1$
			boolean alfPackage2 = manager.query("alf-hybrid-cross-devel"); //$NON-NLS-1$
			boolean alfPackage3 = manager.query("alf"); //$NON-NLS-1$
			boolean alfPackage4 = manager.query("alf-devel"); //$NON-NLS-1$
			boolean alfPackage5 = manager.query("alf-hybrid"); //$NON-NLS-1$
			boolean alfPackage6 = manager.query("alf-hybrid-devel"); //$NON-NLS-1$
			boolean alfPackage7 = manager.query("alf-ide-template"); //$NON-NLS-1$ */
			
			RPMsChecked = true;
			
			/*if(!alfPackage1 && !alfPackage2 && !alfPackage3 && !alfPackage4 && !alfPackage5 && !alfPackage6){
				RPMErrorMessage = Messages.ALFWizard_errorAlfNotInstalled;
				openErrorMessage(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_errorAlfNotInstalled);
				RPMsValidated = false;
				return false;
			}
			
			if(!alfPackage7){
				RPMErrorMessage = Messages.ALFWizard_errorALFTemplateNotInstalled;
				openErrorMessage(Messages.ALFWizard_projectNamePageTitle, Messages.ALFWizard_errorALFTemplateNotInstalled);
				RPMsValidated = false;
				return false;
			} */
			RPMsValidated = true;
		}
		/*
		 * RPM check was already made.
		 */
		else if (! RPMsValidated)
		{
			setErrorMessage(RPMErrorMessage);
			return false;
		}
		
        String projectFieldContents = getProjectName();
        if (projectFieldContents.equals("")) { //$NON-NLS-1$
            setErrorMessage(null);
            setMessage(Messages.ALFWizardNewProjectCreationPage_projectNameEmpty);
            return false;
        }

        // verify that the project name is valid
        IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
        if (!nameStatus.isOK()) {
            setErrorMessage(nameStatus.getMessage());
            return false;
        }
        
        // verify that a project named "ppu_<project_name>" does not already exist in the workspace
        IProject ppuHandle = workspace.getRoot().getProject("ppu_" + getProjectName()); //$NON-NLS-1$
        if(ppuHandle.exists()){
            setErrorMessage(Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart1 + "ppu_" + getProjectName() + //$NON-NLS-1$
            		Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart2);
            return false;
        }
        
        // verify that a project named "lib_<project_name>" does not already exist in the workspace
        IProject libHandle = workspace.getRoot().getProject("lib" + getProjectName()); //$NON-NLS-1$
        if(libHandle.exists()){
        	setErrorMessage(Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart1 + "lib" + getProjectName() + //$NON-NLS-1$
            		Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart2);
            return false;
        }
        
        // verify that a project named "spu_<project_name>" does not already exist in the workspace
        IProject spuHandle = workspace.getRoot().getProject("spu_" + getProjectName()); //$NON-NLS-1$
        if(spuHandle.exists()){
        	setErrorMessage(Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart1 + "spu_" + getProjectName() + //$NON-NLS-1$
            		Messages.ALFWizardNewProjectCreationPage_projectExistsMessagePart2);
            return false;
        }

        if(!useDefaults()){
        	// verify the ppu project's location
        	IStatus ppuLocationStatus = workspace.validateProjectLocationURI(ppuHandle, getLocationURI());
        	if(!ppuLocationStatus.isOK()){
        		setErrorMessage(ppuLocationStatus.getMessage());
            	return false;
        	}
        	
        	// verify the ppu shared library project's location
        	IStatus libLocationStatus = workspace.validateProjectLocationURI(libHandle, getLocationURI());
        	if(!libLocationStatus.isOK()){
        		setErrorMessage(libLocationStatus.getMessage());
        		return false;
        	}
        
        	// verify the spu project location
        	IStatus spuLocationStatus = workspace.validateProjectLocationURI(spuHandle, getLocationURI());
        	if(!spuLocationStatus.isOK()){
        		setErrorMessage(spuLocationStatus.getMessage());
        		return false;
        	}
        }
		setErrorMessage(null);
		setMessage(null);
		return true;
	}
	
	/**
	 * Method that opens a dialog with an error message.
	 * @param title
	 * @param message
	 */
	public void openErrorMessage(String title, String message){
		final MessageDialog dialog = new MessageDialog(getShell(), title, null, message, MessageDialog.ERROR, 
				new String[]{Messages.ALFWizard_ok}, 0);	
		// Run in syncExec because callback is from an operation,
        // which is probably not running in the UI thread.
        getShell().getDisplay().syncExec(new Runnable() {
            public void run() {
                dialog.open();
            }
        });
	}
}
