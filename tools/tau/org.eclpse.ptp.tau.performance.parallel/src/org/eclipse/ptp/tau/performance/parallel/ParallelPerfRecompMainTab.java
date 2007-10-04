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
package org.eclipse.ptp.tau.performance.parallel;


import java.text.MessageFormat;

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
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.PMainTab;
import org.eclipse.ptp.tau.performance.internal.IPerformanceLaunchConfigurationConstants;
import org.eclipse.swt.SWT;
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
	protected Combo buildConfCombo=null;
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
    
	protected void handleProjectButtonSelected() {
		IProject project = chooseProject();
		if (project == null)
			return;
		
		String projectName = project.getName();
		projText.setText(projectName);
		
		int bDex=buildConfCombo.getSelectionIndex();
		String bString=buildConfCombo.getText();
		initConfCombo();
		if(bDex>=0&&buildConfCombo.getItemCount()>bDex&&buildConfCombo.getItem(bDex).equals(bString))
			buildConfCombo.select(bDex);
		else
		if(buildConfCombo.getItemCount()>0)
			buildConfCombo.select(0);
	}
	
	/**
	 * Create the UI for this tab
	 */
    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);
        
		comp.setLayout(new GridLayout());
		createVerticalSpacer(comp, 1);

		Composite projectComp = new Composite(comp, SWT.NONE);		
		projectComp.setLayout(createGridLayout(2, false, 0, 0));
		projectComp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label projLabel = new Label(projectComp, SWT.NONE);
		projLabel.setText(LaunchMessages.getResourceString("PMainTab.&Project_Label"));
		projLabel.setLayoutData(spanGridData(-1, 2));

		projText = new Text(projectComp, SWT.SINGLE | SWT.BORDER);
		projText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		projText.addModifyListener(listener);
		
		projButton = createPushButton(projectComp, LaunchMessages.getResourceString("Tab.common.&Browse_1"), null);
		projButton.addSelectionListener(listener);
		
		createVerticalSpacer(comp, 1);
		
		Label appLabel = new Label(projectComp, SWT.NONE);
		appLabel.setText("C/C++/Fortran Build Configuration");
		appLabel.setLayoutData(spanGridData(-1, 2));

		buildConfCombo=new Combo(projectComp,SWT.DROP_DOWN | SWT.READ_ONLY
				| SWT.BORDER);
		buildConfCombo.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		buildConfCombo.addModifyListener(listener);
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
	
    /**
     * Load the project and build-configuration selection fields with previously saved values, if available
     */
	public void initializeFrom(ILaunchConfiguration configuration) {
        try {
        	String curProj=projText.getText();
            projText.setText(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, EMPTY_STRING));
            if(!curProj.equals(projText.getText()))
    			initConfCombo();
            
            String programName=configuration.getAttribute(IPerformanceLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, EMPTY_STRING);
            buildConfCombo.select(buildConfCombo.indexOf(programName));
            
            if(!programName.equals(EMPTY_STRING))
				buildConfCombo.select(buildConfCombo.indexOf(programName));
			else
				buildConfCombo.select(0);
        
        } catch (CoreException e) {
            setErrorMessage(LaunchMessages.getFormattedResourceString("CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", e.getStatus().getMessage()));
        }
    }
	
	/**
	 * Save the content of the data fields
	 */
    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, getFieldContent(projText.getText()));
        
        configuration.setAttribute(IPerformanceLaunchConfigurationConstants.ATTR_PERFORMANCEBUILD_CONFIGURATION_NAME, buildConfCombo.getText());
        
        try {
        	
        	if(configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, EMPTY_STRING).equals(EMPTY_STRING))
        		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME,"Select an Executable");
//        	 TODO Add 'default' executable name from build configuration.
		} catch (CoreException e) {
			e.printStackTrace();
		}
    }    
	
    
    /**
     * Confirm that all data required to proceed with the launch is present and valid
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
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Project_not_exits"), new String[] {name}));
					return false;
				}
				if (!project.isOpen()) {
					setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Project_is_closed"), new String[] {name}));
					return false;
				}
			} else {
				setErrorMessage(MessageFormat.format(LaunchMessages.getResourceString("PMainTab.Illegal_project"), new String[]{status.getMessage()}));
				return false;
			}
		}
		
		name = getFieldContent(buildConfCombo.getText());
		if (name == null) {
			setErrorMessage("Build configuration not specified");
			return false;
		}
		return true;
	}
    
	
}
