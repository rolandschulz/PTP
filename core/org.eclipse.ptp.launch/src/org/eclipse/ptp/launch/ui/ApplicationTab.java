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
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
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
 * The Main tab is used to specify the resource manager for the launch,
 * select the project and executable to launch, and specify the location
 * of the executable if it is a remote launch.
 */
public class ApplicationTab extends LaunchConfigurationTab {
    protected class WidgetListener extends SelectionAdapter implements ModifyListener {
	    public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }
        public void widgetSelected(SelectionEvent e) {
	        Object source = e.getSource();
			if (source == projButton) {
	            handleProjectButtonSelected();
			} else if (source == appButton) {
	            handleApplicationButtonSelected();
	        } else if (source == consoleButton) {
	            updateLaunchConfigurationDialog();
	        }
	    }
    }
    
    protected Text projText = null;
	protected Text appText = null;
	protected Button projButton = null; 
    protected Button appButton = null;
    protected Button consoleButton = null;
	protected WidgetListener listener = new WidgetListener();

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
     */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        
		comp.setLayout(new GridLayout());

		Composite mainComp = new Composite(comp, SWT.NONE);		
		mainComp.setLayout(createGridLayout(2, false, 0, 0));
		mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projLabel = new Label(mainComp, SWT.NONE);
		projLabel.setText(LaunchMessages.getResourceString("ApplicationTab.&Project_Label")); //$NON-NLS-1$
		projLabel.setLayoutData(spanGridData(-1, 2));

		projText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		projText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projText.addModifyListener(listener);
		
		projButton = createPushButton(mainComp, LaunchMessages.getResourceString("Tab.common.&Browse_1"), null); //$NON-NLS-1$
		projButton.addSelectionListener(listener);
		
		createVerticalSpacer(comp, 1);
		
		Label appLabel = new Label(mainComp, SWT.NONE);
		appLabel.setText(LaunchMessages.getResourceString("ApplicationTab.&Application_Label")); //$NON-NLS-1$
		appLabel.setLayoutData(spanGridData(-1, 2));

		appText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		appText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		appText.addModifyListener(listener);
		
		appButton = createPushButton(mainComp, LaunchMessages.getResourceString("Tab.common.B&rowse_2"), null); //$NON-NLS-1$
		appButton.addSelectionListener(listener);

		createVerticalSpacer(mainComp, 2);

		consoleButton = createCheckButton(mainComp, LaunchMessages.getResourceString("ApplicationTab.Console")); //$NON-NLS-1$
		consoleButton.setSelection(false);
		consoleButton.addSelectionListener(listener);
   }

	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
     */
    public Image getImage() {
        return LaunchImages.getImage(LaunchImages.IMG_MAIN_TAB);
    }
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
     */
    public String getName() {
        return LaunchMessages.getResourceString("ApplicationTab.Main"); //$NON-NLS-1$
    }
 
	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
     */
    public void initializeFrom(ILaunchConfiguration configuration) {
    	super.initializeFrom(configuration);
    	
        try {
            projText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
            appText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, EMPTY_STRING));
            consoleButton.setSelection(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, false));
        } catch (CoreException e) {
            setErrorMessage(LaunchMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
        }
    }
	
    /* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
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
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("ApplicationTab.Project_not_exist"), new Object[] {name})); //$NON-NLS-1$
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("ApplicationTab.Project_is_closed"), new Object[] {name})); //$NON-NLS-1$
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("ApplicationTab.Illegal_project"), new Object[]{status.getMessage()})); //$NON-NLS-1$
				return false;
			}
		}
		
		name = getFieldContent(appText.getText());
		if (name == null) {
			setErrorMessage(LaunchMessages.getResourceString("ApplicationTab.Application_program_not_specified")); //$NON-NLS-1$
			return false;
		}
		return true;
	}
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				getFieldContent(projText.getText()));
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,
				getFieldContent(appText.getText()));
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_CONSOLE,
				consoleButton.getSelection());
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
     */
    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        IProject project = getDefaultProject(configuration);
        String projectName = null;
        if (project != null) {
            projectName = project.getName();
    		String name = getLaunchConfigurationDialog().generateName(projectName);
    		configuration.rename(name);
        }
        
        configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				projectName);
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,
				(String) null);
    }    

    /* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse.debug.ui.ILaunchConfigurationDialog)
     */
    public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
        super.setLaunchConfigurationDialog(dialog);
    }

    /**
     * Create a dialog that allows the user to select a file in the current project.
     * 
	 * @return selected file
	 */
	protected IResource chooseFile() {
	    final IProject project = getProject();
	    if (project == null) {
			MessageDialog.openInformation(getShell(), LaunchMessages.getResourceString("ApplicationTab.Project_required"), 
					LaunchMessages.getResourceString("ApplicationTab.Enter_project_before_browsing_for_program")); //$NON-NLS-1$
	        return null;	        
	    }
	    		
		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		BaseWorkbenchContentProvider contentProvider = new BaseWorkbenchContentProvider();
		ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(), labelProvider, contentProvider);
		dialog.setTitle(LaunchMessages.getResourceString("ApplicationTab.Program_selection"));
		dialog.setMessage(LaunchMessages.getFormattedResourceString("ApplicationTab.Choose_program_to_run_from_NAME", project.getName()));
		dialog.setBlockOnOpen(true);
		dialog.setAllowMultiple(false);
		dialog.setInput(project);	
		dialog.setValidator(new ISelectionStatusValidator() {
			public IStatus validate(Object[] selection) {
				if (selection.length == 0 || ! (selection[0] instanceof IFile)) {
					return new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, 
							LaunchMessages.getResourceString("ApplicationTab.Selection_must_be_file"), null); //$NON-NLS-1$
				}
				try {
					IResource resource = project.findMember( ((IFile)selection[0]).getProjectRelativePath());
					if (resource == null || resource.getType() != IResource.FILE) {
						return new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.INFO, 
								LaunchMessages.getResourceString("ApplicationTab.Selection_must_be_file"), null); //$NON-NLS-1$
					}

					return new Status(IStatus.OK, PTPCorePlugin.getUniqueIdentifier(), IStatus.OK, resource.getName(), null);
				} catch (Exception ex) {
					return new Status(IStatus.ERROR, PTPCorePlugin.PLUGIN_ID, IStatus.INFO, 
							LaunchMessages.getResourceString("ApplicationTab.Selection_must_be_file"), null); //$NON-NLS-1$
				}
			}
		});
		if (dialog.open() == Window.OK) {
		    return (IResource) dialog.getFirstResult();
		}
		return null;
	}
    
    /**
     * Create a dialog that allows the user to choose a project.
     * 
     * @return selected project
     */
    protected IProject chooseProject() {
		IProject[] projects = getWorkspaceRoot().getProjects();

		WorkbenchLabelProvider labelProvider = new WorkbenchLabelProvider();
		ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), labelProvider);
		dialog.setTitle(LaunchMessages.getResourceString("ApplicationTab.Project_Selection_Title")); //$NON-NLS-1$
		dialog.setMessage(LaunchMessages.getResourceString("ApplicationTab.Project_Selection_Message")); //$NON-NLS-1$
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
    
	/**
	 * Get a default project. This is either the project name that
	 * has been previously selected, or the project that is currently
	 * selected in the workspace.
	 * 
	 * @param configuration
	 * @return default project
	 */
	protected IProject getDefaultProject(ILaunchConfiguration configuration) {
		String projectName = null;
		try {
		    projectName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
		} catch (CoreException e) {
		    return null;
		}
		IWorkbenchPage page = PTPLaunchPlugin.getActivePage();
		if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
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
    
	/**
	 * Get the IProject the corresponds to the project name that is
	 * displayed in the projText control
	 * 
	 * @return project
	 */
	protected IProject getProject() {
		String projectName = projText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return getWorkspaceRoot().getProject(projectName);		
	}
	
	/**
     * Allow the user to choose the application to execute
     * 
     * Initial path does not work on MacOS X: see bug #153365
     */
    protected void handleApplicationButtonSelected() {
     	String initPath = appText.getText();
    	if (initPath.equals(EMPTY_STRING)) {
    	    final IProject project = getProject();
    	    if (project == null) {
    			MessageDialog.openInformation(getShell(), LaunchMessages.getResourceString("ApplicationTab.Project_required"), 
    					LaunchMessages.getResourceString("ApplicationTab.Enter_project_before_browsing_for_program")); //$NON-NLS-1$
    	        return;	        
    	    }
    	    initPath = getProject().getLocationURI().getPath();
    	}
       	IResourceManager rm = getResourceManager(getLaunchConfiguration());
    	AbstractRemoteResourceManagerConfiguration rmConf = (AbstractRemoteResourceManagerConfiguration) ((IResourceManagerControl)rm).getConfiguration();
    	IRemoteServices remServices = PTPRemotePlugin.getDefault().getRemoteServices(rmConf.getRemoteServicesId());
    	if (remServices != null) {
    		IRemoteConnection remCon = remServices.getConnectionManager().getConnection(rmConf.getConnectionName());
    		if (remCon != null) {
    			IRemoteFileManager fileMgr = remServices.getFileManager(remCon);
    			if (fileMgr != null) {
    				IPath path = fileMgr.browseFile(getShell(), "Select application to execute", initPath);
    				if (path != null) {
    					appText.setText(path.toString());
    				}
    			}
    		}
    	}
    }
	
	/**
	 * Allow the user to choose a project
	 */
	protected void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null)
			return;
		
		String projectName = project.getName();
		projText.setText(projectName);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#updateLaunchConfigurationDialog()
     */
    protected void updateLaunchConfigurationDialog() {
        super.updateLaunchConfigurationDialog();
    }	
}
