/****************************************************************************
 *			Tuning and Analysis Utilities
 *			http://www.cs.uoregon.edu/research/paracomp/tau
 ****************************************************************************
 * Copyright (c) 1997-2006
 *    Department of Computer and Information Science, University of Oregon
 *    Advanced Computing Laboratory, Los Alamos National Laboratory
 *    Research Center Juelich, ZAM Germany	
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Wyatt Spear - initial API and implementation
 ****************************************************************************/
package org.eclipse.ptp.perf.parallel;


import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.PMainTab;
import org.eclipse.ptp.perf.IPerformanceLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Extends the default PTP main launch configuration tab, primarily changing the binary
 * selection to build-configuration selection, as existing binaries are not used by the
 * performance analysis launch system
 * TODO:  Add support for performance analysis of non-recompiled binaries
 * @author wspear
 *
 */
public class ParallelPerfRecompMainTab extends PMainTab{
	
    protected class WidgetListener extends SelectionAdapter implements ModifyListener {
	    public void modifyText(ModifyEvent e) {
            updateLaunchConfigurationDialog();
        }
        public void widgetSelected(SelectionEvent e) {
	        Object source = e.getSource();
			if (source == resourceManagerCombo) {
				rmSelectionChanged();
			} else if (source == projButton) {
	            handleProjectButtonSelected();
	            
	    		int bDex=buildConfCombo.getSelectionIndex();
	    		String bString=buildConfCombo.getText();
	    		initConfCombo();
	    		if(bDex>=0&&buildConfCombo.getItemCount()>bDex&&buildConfCombo.getItem(bDex).equals(bString))
	    			buildConfCombo.select(bDex);
	    		else
	    		if(buildConfCombo.getItemCount()>0)
	    			buildConfCombo.select(0);
	            
			} else if (source == appButton) {
	            handleApplicationButtonSelected();
	        } else {
	            handleProjectButtonSelected();
	        }
	    }
    }
	
	private Combo buildConfCombo=null;
	private Combo resourceManagerCombo = null;
	private IResourceManager resourceManager = null;
    private final Map<Integer, IResourceManager> resourceManagers = new HashMap<Integer, IResourceManager>();
    private final HashMap<IResourceManager, Integer> resourceManagerIndices = new HashMap<IResourceManager, Integer>();
    protected WidgetListener listener = new WidgetListener();
	
	private void createResourceControl(Composite comp)
	{
		Composite rmComp = new Composite(comp, SWT.NONE);		
		rmComp.setLayout(createGridLayout(2, false, 0, 0));
		rmComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		IModelManager modelManager = PTPCorePlugin.getDefault().getModelManager();
		IPUniverse universe = modelManager.getUniverse();
		IResourceManager[] rms = modelManager.getStartedResourceManagers(universe);
		new Label(rmComp, SWT.NONE).setText(LaunchMessages.getResourceString("PMainTab.RM_Selection_Label")); //$NON-NLS-1$
			
		resourceManagerCombo = new Combo(rmComp, SWT.READ_ONLY);
		for (int i = 0; i < rms.length; i++) {
			resourceManagerCombo.add(rms[i].getName());
			resourceManagers.put(i, rms[i]);
			resourceManagerIndices.put(rms[i], i);
		}
		resourceManagerCombo.addSelectionListener(listener);
		resourceManagerCombo.deselectAll();
	}
	
	private void createProjectControl(Composite mainComp){
		Label projLabel = new Label(mainComp, SWT.NONE);
		projLabel.setText(LaunchMessages.getResourceString("PMainTab.&Project_Label")); //$NON-NLS-1$
		projLabel.setLayoutData(spanGridData(-1, 2));

		projText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		projText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projText.addModifyListener(listener);
		
		projButton = createPushButton(mainComp, LaunchMessages.getResourceString("Tab.common.&Browse_1"), null); //$NON-NLS-1$
		projButton.addSelectionListener(listener);
	}
	
	private void createAppControl(Composite mainComp)
	{
		//appText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
		//appText.setVisible(false);
//		Label appLabel = new Label(mainComp, SWT.NONE);
//		appLabel.setText(LaunchMessages.getResourceString("PMainTab.&Application_Label")); //$NON-NLS-1$
//		appLabel.setLayoutData(spanGridData(-1, 2));
//
//		appText = new Text(mainComp, SWT.SINGLE | SWT.BORDER);
//		appText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
//		appText.addModifyListener(listener);
//		
//		appButton = createPushButton(mainComp, LaunchMessages.getResourceString("Tab.common.B&rowse_2"), null); //$NON-NLS-1$
//		appButton.addSelectionListener(listener);

		Label appLabel = new Label(mainComp, SWT.NONE);
		appLabel.setText("C/C++/Fortran Build Configuration");
		appLabel.setLayoutData(spanGridData(-1, 2));

		buildConfCombo=new Combo(mainComp,SWT.DROP_DOWN | SWT.READ_ONLY
				| SWT.BORDER);
		buildConfCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buildConfCombo.addModifyListener(listener);
	}
	
    public void createControl(Composite parent)
    {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        
		comp.setLayout(new GridLayout());
		createVerticalSpacer(comp, 1);
		
		createResourceControl(comp);
		
		createVerticalSpacer(comp, 1);

		Composite mainComp = new Composite(comp, SWT.NONE);		
		mainComp.setLayout(createGridLayout(2, false, 0, 0));
		mainComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		createProjectControl(mainComp);
		createVerticalSpacer(comp, 1);
		createAppControl(mainComp);
    }
    
    
    private void initializeConfCombo(ILaunchConfiguration configuration)
    {
    	//String curProj=projText.getText();
        try {
			projText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
		
        //if(!curProj.equals(projText.getText()))
			initConfCombo();
        
        String programName=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, EMPTY_STRING);
        
        
        buildConfCombo.select(buildConfCombo.indexOf(programName));
        
        if(!programName.equals(EMPTY_STRING))
			buildConfCombo.select(buildConfCombo.indexOf(programName));
		else
			buildConfCombo.select(0);
        
        } catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            projText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
            //appText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, EMPTY_STRING));

            resourceManager = getResourceManager(configuration);
			if (resourceManager == null) {
				setErrorMessage(LaunchMessages
						.getResourceString("PMainTab.No_Resource_Manager_Available")); //$NON-NLS-1$
				return;
			}

			setResourceManagerComboSelection(resourceManager);
        } catch (CoreException e) {
            setErrorMessage(LaunchMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage())); //$NON-NLS-1$
        }
    	initializeConfCombo(configuration);
    }
    
    
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
    	if (resourceManager != null) {
			configuration.setAttribute(
					IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
					resourceManager.getUniqueName());
    	}
		configuration.setAttribute(
				IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME,
				getFieldContent(projText.getText()));
		configuration.setAttribute(IPerformanceLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, buildConfCombo.getText());
        
		
//		configuration.setAttribute(
//				IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH,
//				getFieldContent(appText.getText()));
    }
    
	/**
	 * Return the ICProject corresponding to the project name in the project name text field, or
	 * null if the text does not match a project name.
	 */
	protected ICProject getCProject() {
		String projectName = projText.getText().trim();
		if (projectName.length() < 1) {
			return null;
		}
		return CoreModel.getDefault().getCModel().getCProject(projectName);
	}
    
    /**
     * Initialize the combo box listing the available build configurations for this project
     *
     */
    protected void initConfCombo()
	{
		buildConfCombo.removeAll();
		ICProject project = getCProject();
		if (project == null) {
			MessageDialog.openInformation(getShell(), org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Project_required"),
					org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Enter_project_before_searching_for_program")); 
			return;
		}
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project.getResource());
		if(info==null)
		{
			MessageDialog.openInformation(getShell(), org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Project_required"), 
					org.eclipse.cdt.launch.internal.ui.LaunchMessages.getString("CMainTab.Enter_project_before_searching_for_program"));
			return;
		}
		
		IConfiguration[] confs =info.getManagedProject().getConfigurations();
		
		for(int i=0;i<confs.length;i++)
		{
			buildConfCombo.add(confs[i].getName());
		}
	}
    
    public boolean isValid(ILaunchConfiguration config) {
		setErrorMessage(null);
		setMessage(null);

		if (resourceManager == null) {
			setErrorMessage(LaunchMessages
					.getResourceString("PMainTab.No_Resource_Manager_Available")); //$NON-NLS-1$
			return false;
		}
		
		String name = getFieldContent(projText.getText());
		if (name != null) {
			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			IStatus status = workspace.validateName(name, IResource.PROJECT);
			if (status.isOK()) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
				if (!project.exists()) {
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Project_not_exist"), new Object[] {name})); //$NON-NLS-1$
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Project_is_closed"), new Object[] {name})); //$NON-NLS-1$
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Illegal_project"), new Object[]{status.getMessage()})); //$NON-NLS-1$
				return false;
			}
		}
		
//		name = getFieldContent(appText.getText());
//		if (name == null) {
//			setErrorMessage(LaunchMessages.getResourceString("PMainTab.Application_program_not_specified")); //$NON-NLS-1$
//			return false;
//		}
		name = getFieldContent(buildConfCombo.getText());
		if (name == null) {
			setErrorMessage("Build configuration not specified");
			return false;
		}
		return true;
	}
    
    
    
    
    /**
	 * Find a default resource manager
	 * 
	 * @return resource manager
	 */
	private IResourceManager getResourceManagerDefault() {
		IModelManager modelManager = PTPCorePlugin.getDefault().getModelManager();
		IPUniverse universe = modelManager.getUniverse();
		if (universe != null) {
			IResourceManager[] rms = modelManager.getStartedResourceManagers(universe);
			if (rms.length == 0) {
				return null;
			}
			return rms[0];
		}
		return null;
	}    
    
	/**
	 * @return
	 */
	private IResourceManager getResourceManagerFromCombo() {
		if (resourceManagerCombo != null) {
			int i = resourceManagerCombo.getSelectionIndex();
			return resourceManagers.get(i);
		}
		return null;
	}

	/**
     * Handle selection of a resource manager
     */
    private void rmSelectionChanged() {
    	resourceManager = getResourceManagerFromCombo();
    }
    
    /**
	 * Given a resource manager, select it in the combo
	 * 
	 * @param resource manager
	 */
	private void setResourceManagerComboSelection(IResourceManager rm) {
		final Integer results = resourceManagerIndices.get(rm);
		int i = 0;
		if (results != null) {
			i = results.intValue();
		}
		resourceManagerCombo.select(i);
		rmSelectionChanged();
	}
	
}