/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.pdt.xml.wizard.ui;

import java.io.File;

import org.eclipse.cdt.core.model.ICContainer;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.cell.pdt.xml.Activator;
import org.eclipse.ptp.utils.ui.swt.Frame;
import org.eclipse.ptp.utils.ui.swt.FrameMold;
import org.eclipse.ptp.utils.ui.swt.TextGroup;
import org.eclipse.ptp.utils.ui.swt.TextMold;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IPathEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;


/**
 * Page where the user configures the generation of the configuration file.
 * 
 * @author Richard Maciel
 *
 */
public class PdtWizardConfigurationFilePage extends WizardPage {

	Frame configFileOptions;
	TextGroup configDir;
	TextGroup configFileName;
	
	/*Frame architectureOptions;
	Button archX86;
	Button archCell;*/
	
	public PdtWizardConfigurationFilePage() {
		super(PdtWizardConfigurationFilePage.class.getName());
		setTitle(Messages.PdtWizardConfigurationFilePage_Title);
		setDescription(Messages.PdtWizardConfigurationFilePage_Description);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		initializeDialogUnits(parent);
		
		Font font = parent.getFont();
		
		// create the composite to hold this wizard page's widgets
		Composite composite = new Composite(parent, SWT.NONE);
		
		//create desired layout for this wizard page
		GridLayout layout = new GridLayout();
		layout.numColumns = 1;
 		composite.setLayout(layout);
 		composite.setFont(font);
 		
 		// configuration file properties group
 		FrameMold frMold = new FrameMold(FrameMold.HAS_FRAME);
 		frMold.setColumns(1);
 		frMold.setTitle(Messages.PdtWizardConfigurationFilePage_CreateControls_FrameLabel);
 		configFileOptions = new Frame(composite, frMold);
 		
 		/*// Architecture selection group
 		frMold.setColumns(2);
 		frMold.setTitle("Architecture selection");
 		architectureOptions = new Frame(composite, frMold);*/
 		
 		// Create modify listener class to be used in the text controls
 		ModifyListener textListener = new TextModifyListener();
 		
 		// configuration file properties controls
 		TextMold workspaceBrowseMold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.GRID_DATA_SPAN | TextMold.LABELABOVE | TextMold.HASBUTTON, Messages.PdtWizardConfigurationFilePage_CreateControls_Textbox_FolderLabel);
 		configDir = new TextGroup(configFileOptions.getTopUserReservedComposite(), workspaceBrowseMold);
 		configDir.addModifyListener(textListener);
 		
 		Button browse = configDir.getButton();
 		browse.setText(Messages.PdtWizardConfigurationFilePage_CreateControls_Button_WorkspaceLabel);
 		browse.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				handleWorkspaceButtonEvent();
 			}
 		});
 		
 		TextMold tmold = new TextMold(TextMold.GRID_DATA_ALIGNMENT_FILL | TextMold.GRID_DATA_GRAB_EXCESS_SPACE | TextMold.GRID_DATA_SPAN | TextMold.LABELABOVE, Messages.PdtWizardConfigurationFilePage_CreateControls_Textbox_FileLabel);
 		configFileName = new TextGroup(configFileOptions.getTopUserReservedComposite(), tmold);
 		configFileName.addModifyListener(textListener); 
 		
 		setControl(composite);
 		
 		initializeControls();
	}
	
	/**
	 * Generates a {@link ContainerSelectionDialog} which lets the user select a directory on the 
	 * workspace.
	 * 
	 */
	protected void handleWorkspaceButtonEvent() {
		IWorkspaceRoot workspace = ResourcesPlugin.getWorkspace().getRoot();
		
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(getShell(), workspace, true, Messages.PdtWizardConfigurationFilePage_HandleWorkspaceButton_Dialog_SelectWorkspaceResource);
		dialog.setBlockOnOpen(true);
		//dialog.setInitialSelections(new Object[] { selectedContainer });
		
		if (dialog.open() == Dialog.OK) {
			/*
			 * Although not documented, getResult returns an array with exactly
			 * one element, that has type of IPath.
			 * The path is relative to workspace.
			 */
			Object r[] = dialog.getResult();
			IPath selectedPath = (IPath)r[0];
			// The path is returned as absolute! Make it relative!
			configDir.setString(selectedPath.makeRelative().toOSString());
			
		}
		
	}

	public IPath getFilePath() {
		IPath path = new Path(configDir.getString()).append(configFileName.getString()).makeAbsolute();
			
		return path;
	}
	
	/**
	 * Check if page controls are valid.
	 * 
	 * @throws CoreException if not valid
	 */
	protected void validateControls() throws CoreException {
		MultiStatus interfaceStatus = new MultiStatus(Activator.PLUGIN_ID, 0, "", null); //$NON-NLS-1$
		
		// Check if directory where config file will be created exists.
		String configDirWorkspaceRel = configDir.getString();
		IPath workspacePath = ResourcesPlugin.getWorkspace().getRoot().getRawLocation();
		File dir = new File(workspacePath.append(configDirWorkspaceRel).toOSString());
		if(configDirWorkspaceRel.trim().length() == 0 || !dir.exists()) {
			interfaceStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK, Messages.PdtWizardConfigurationFilePage_ValidateControls_Error_InvalidDirectory, null));
//			interfaceStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.PdtWizardConfigurationFilePage_ValidateControls_Error_InvalidDirectory));
		}
		
		// Check if name is not null
		if((configFileName.getString() == null) 
				|| (configFileName.getString().trim().length() == 0)) {
			interfaceStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, Status.OK, Messages.PdtWizardConfigurationFilePage_ValidateControls_Error_EmptyFilename, null));
//			interfaceStatus.add(new Status(Status.ERROR, Activator.PLUGIN_ID, Messages.PdtWizardConfigurationFilePage_ValidateControls_Error_EmptyFilename));
		}
		
		if(interfaceStatus.matches(IStatus.ERROR)) {
			throw new CoreException(interfaceStatus);
		}
	}
	
	/**
	 * Check if page status is valid (by using validateControls). If not, it sets appropriate error message
	 * and disables the interface. 
	 * 
	 */
	protected void validatePage() {
		setErrorMessage(null);
		setPageComplete(true);
		
		try {
			validateControls();
		} catch (CoreException e) {
			setErrorMessage(e.getStatus().getChildren()[0].getMessage());
			setPageComplete(false);
		}
	}
	 
	/**
	 * Initialize configuration file location and architecture type
	 * 
	 */
	private void initializeControls() {
		// Fetch project information from workbench, if possible
		IProject project = getProjectFromWorkbench();
		
		// Initialize the directory with the project (if not null)
		if(project != null) {
			configDir.setString(project.getName());
		}
		
		/*// Default is cell arch
		archCell.setSelection(true);*/
		
	}
	
	/**
	 * Extract the project from the associated active window in the workbench.
	 * This can be an editor window or a selection window.
	 * 
	 * @return An IProject or null if selection is not file (or project) related.
	 */
	private IProject getProjectFromWorkbench() {
		IProject project = null;
		
		// TODO: Change this to the init methods of the class that implements INewWizard,
		// because it receives a structuredselection as a parameter.
		// First find which window is active.
		IWorkbenchWindow activeWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if(activeWindow != null && activeWindow.getSelectionService().getSelection() != null) {
			
			// Get active window selection
			ISelection sel = activeWindow.getSelectionService().getSelection();
			if(sel instanceof TreeSelection) {
				TreeSelection tSel = (TreeSelection)sel;
				if(!tSel.isEmpty()) {
					// If tree selection, get first element that refers to directly to a project 
					// or to file inside the project. Ignore the rest 
					if(tSel.getFirstElement() instanceof IProject) {
						project = (IProject)tSel.getFirstElement();
					} else if(tSel.getFirstElement() instanceof IFile) {
						project = ((IFile)tSel.getFirstElement()).getProject();
					} else if(tSel.getFirstElement() instanceof IContainer) {
						project = ((IContainer)tSel.getFirstElement()).getProject();
					} else if(tSel.getFirstElement() instanceof ICContainer) {
						ICProject cproj = ((ICContainer)tSel.getFirstElement()).getCProject(); 
						project = cproj.getProject();
					}
				}
			} else if(sel instanceof TextSelection) {
				
				// We're in a text selection. Do some sanity checks on the editor where the selection occurred. 
				if(activeWindow.getActivePage() != null && activeWindow.getActivePage().getActiveEditor() != null) {
					IEditorInput editorInput = activeWindow.getActivePage().getActiveEditor().getEditorInput();
					if(editorInput instanceof IPathEditorInput) {
						// Editor must implement the IPathEditorInput interface, so we can extract the path of the edited file
						IPathEditorInput pathEditInput = (IPathEditorInput)activeWindow.getActivePage().getActiveEditor().getEditorInput();
						IPath fileAbsPath = pathEditInput.getPath().makeAbsolute();						
						
						// Path available, so extract the project which contains this file
						IFile file = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(fileAbsPath);
						
						project = file.getProject();
					}
				}
				
			}
		}
		return project;
	}
	
	protected class TextModifyListener implements ModifyListener {
		public void modifyText(ModifyEvent e) {
			validatePage();
		}
	}

}
