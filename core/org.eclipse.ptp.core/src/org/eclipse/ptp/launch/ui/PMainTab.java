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

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.ParallelPlugin;
import org.eclipse.ptp.core.IPDTLaunchConfigurationConstants;
import org.eclipse.ptp.ui.ParallelImages;
import org.eclipse.ptp.ui.UIMessage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.dialogs.ISelectionStatusValidator;
import org.eclipse.ui.model.BaseWorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;

/**
 *
 */
public class PMainTab extends PLaunchConfigurationTab {
    protected Text projText = null;
    protected Text appText = null;
    protected Button stopInMainCheckButton = null;
    protected Button projButton = null;
    protected Button appButton = null;

    protected class WidgetListener extends SelectionAdapter implements ModifyListener {
	    public void widgetSelected(SelectionEvent e) {
	        Object source = e.getSource();
	        if (source == projButton)
	            handleProjectButtonSelected();
	        else if (source == appButton)
	            handleApplicationButtonSelected();
	        else
	            handleProjectButtonSelected();
	    }
        public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }
    }
    
    protected WidgetListener listener = new WidgetListener();
    
    /**
     * @see ILaunchConfigurationTab#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        //WorkbenchHelp.setHelp(getControl(), ICDTLaunchHelpContextIds.LAUNCH_CONFIGURATION_DIALOG_ARGUMNETS_TAB);
        
		comp.setLayout(new GridLayout());
		createVerticalSpacer(comp, 1);

		Composite projectComp = new Composite(comp, SWT.NONE);		
		projectComp.setLayout(createGridLayout(2, false, 0, 0));
		projectComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projLabel = new Label(projectComp, SWT.NONE);
		projLabel.setText(UIMessage.getResourceString("PMainTab.&Project_Label"));
		projLabel.setLayoutData(spanGridData(-1, 2));

		projText = new Text(projectComp, SWT.SINGLE | SWT.BORDER);
		projText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projText.addModifyListener(listener);
		
		projButton = createPushButton(projectComp, UIMessage.getResourceString("Tab.common.&Browse_1"), null);
		projButton.addSelectionListener(listener);
		
		createVerticalSpacer(comp, 1);
		
		Label appLabel = new Label(projectComp, SWT.NONE);
		appLabel.setText(UIMessage.getResourceString("PMainTab.&Application_Label"));
		appLabel.setLayoutData(spanGridData(-1, 2));

		appText = new Text(projectComp, SWT.SINGLE | SWT.BORDER);
		appText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appText.addModifyListener(listener);
		
		appButton = createPushButton(projectComp, UIMessage.getResourceString("Tab.common.B&rowse_2"), null);
		appButton.addSelectionListener(listener);
		
		createVerticalSpacer(projectComp, 2);
		
		stopInMainCheckButton = createCheckButton(projectComp, UIMessage.getResourceString("PMainTab.St&op_in_main"));
		stopInMainCheckButton.setLayoutData(spanGridData(-1, 2));
		stopInMainCheckButton.addSelectionListener(new SelectionAdapter() {
		    public void widgetSelected(SelectionEvent e) {
		        updateLaunchConfigurationDialog();
		    }
		});
    }
    
    /**
     * Defaults are empty.
     * 
     * @see ILaunchConfigurationTab#setDefaults(ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        IProject project = getDefaultProject(configuration);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
    		String name = getLaunchConfigurationDialog().generateName(projectName);
    		configuration.rename(name);
        }
        
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, projectName);       
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String) null);
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
    }    

    /**
     * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            projText.setText(configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
            appText.setText(configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_APPLICATION_NAME, EMPTY_STRING));
            stopInMainCheckButton.setSelection(configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false));
        } catch (CoreException e) {
            setErrorMessage(UIMessage.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }

    /**
     * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, getFieldContent(projText.getText()));
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_APPLICATION_NAME, getFieldContent(appText.getText()));
        configuration.setAttribute(IPDTLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, stopInMainCheckButton.getSelection());
    }    
    
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		String name = getFieldContent(projText.getText());
		if (name != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (!project.exists()) {
					setErrorMessage(MessageFormat.format(UIMessage.getResourceString("PMainTab.Project_not_exits"), new String[] {name}));
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(UIMessage.getResourceString("PMainTab.Project_is_closed"), new String[] {name}));
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(UIMessage.getResourceString("PMainTab.Illegal_project"), new String[]{status.getMessage()}));
				return false;
			}
		}
		
		name = getFieldContent(appText.getText());
		if (name == null) {
			setErrorMessage(UIMessage.getResourceString("PMainTab.Application_program_not_specified"));
			return false;
		}
		return true;
	}
	
    /**
     * @see ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return UIMessage.getResourceString("PMainTab.Main");
    }

    /**
     * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
     */
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
    }
    
    /**
     * @see ILaunchConfigurationTab#getImage()
     */
    public Image getImage() {
        return ParallelImages.getImage(ParallelImages.IMG_MAIN_TAB);
    }

    /**
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
     */
    protected void updateLaunchConfigurationDialog() {
        super.updateLaunchConfigurationDialog();
    }
    
    protected void handleApplicationButtonSelected() {
        IResource file = chooseFile();
        if (file == null)
            return;
        
        String fileName = file.getProjectRelativePath().toString();
        appText.setText(fileName);
    }
    
	protected void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null)
			return;
		
		String projectName = project.getName();
		projText.setText(projectName);
	}
    
	protected IProject getProject() {
		String projectName = projText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getWorkspaceRoot().getProject(projectName);		
	}
	
	protected IResource chooseFile() {
	    final IProject project = getProject();
	    if (project == null) {
			MessageDialog.openInformation(getShell(), UIMessage.getResourceString("PMainTab.Project_required"), UIMessage.getResourceString("PMainTab.Enter_project_before_browsing_for_program"));
	        return null;	        
	    }
	    		
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		BaseWorkbenchContentProvider contentProvider = new BaseWorkbenchContentProvider();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, contentProvider);
		dialog.setTitle(UIMessage.getResourceString("PMainTab.Program_selection"));
		dialog.setMessage(UIMessage.getFormattedResourceString("PMainTab.Choose_program_to_run_from_NAME", project.getName()));
		dialog.setBlockOnOpen(true);
		dialog.setAllowMultiple(false);
		dialog.setInput(project);	
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0 || ! (selection[0] instanceof IFile)) {
					return new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, UIMessage.getResourceString("PMainTab.Selection_must_be_file"), null);
				}
				try {
					IResource resource = project.findMember( ((IFile)selection[0]).getProjectRelativePath());
					if (resource == null || resource.getType() != IResource.FILE) {
						return new Status(IStatus.ERROR, ParallelPlugin.getUniqueIdentifier(), IStatus.INFO, UIMessage.getResourceString("PMainTab.Selection_must_be_file"), null);
					}

					return new Status(IStatus.OK, ParallelPlugin.getUniqueIdentifier(), IStatus.OK, resource.getName(), null);
				} catch (Exception ex) {
					return new Status(IStatus.ERROR, ParallelPlugin.PLUGIN_ID, IStatus.INFO, UIMessage.getResourceString("PMainTab.Selection_must_be_file"), null);
				}
			}
		});
		if (dialog.open() == Window.OK) {
		    return (IResource) dialog.getFirstResult();
		}
		return null;
	}
    
	protected IProject chooseProject() {
		IProject[] projects = getWorkspaceRoot().getProjects();

		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(UIMessage.getResourceString("PMainTab.Project_Selection_Title"));
		dialog.setMessage(UIMessage.getResourceString("PMainTab.Project_Selection_Message"));
		dialog.setElements(projects);
		
		IProject project = getProject();
		if (project != null) {
			dialog.setInitialSelections(new Object[] { project });
		}
		if (dialog.open() == Window.OK) {			
			return (IProject) dialog.getFirstResult();
		}			
		return null;		
	}
	
	protected IProject getDefaultProject(ILaunchConfiguration configuration) {
		String projectName = null;
		try {
		    projectName = configuration.getAttribute(IPDTLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		} catch (CoreException e) {
		    return null;
		}
		IWorkbenchPage page = ParallelPlugin.getActivePage();
		if (projectName != null && !projectName.equals("")) {
			IProject project = getWorkspaceRoot().getProject(projectName);
			if (project != null && project.exists())
			    return project;
		} else {
			if (page != null) {
				ISelection selection = page.getSelection();
				if (selection instanceof IStructuredSelection) {
					IStructuredSelection ss = (IStructuredSelection)selection;
					if (!ss.isEmpty()) {
					    Object obj = ss.getFirstElement();
					    if (obj instanceof IAdaptable) {
					        Object o = ((IAdaptable)obj).getAdapter(IResource.class);
					        if (o instanceof IResource) 
					           return ((IResource)o).getProject();
					    }
					}
				}
			}
		}

		IEditorPart part = page.getActiveEditor();
		if (part != null) {
			IEditorInput input = part.getEditorInput();
			IFile file = (IFile) input.getAdapter(IFile.class);
			if (file != null)
			    return file.getProject();
		}
		return null;
	}	
}
