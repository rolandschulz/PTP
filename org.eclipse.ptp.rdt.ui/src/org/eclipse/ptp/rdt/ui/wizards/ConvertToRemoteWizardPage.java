/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.wizards;

 
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.Map;

import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.ptp.rdt.core.resources.RemoteNature;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Listener;

/**
 * Converts existing CDT projects to RDT projects.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 */
public class ConvertToRemoteWizardPage extends ConvertProjectWizardPage {
    
    private static final String WZ_TITLE = "WizardProjectConversion.title"; //$NON-NLS-1$
    private static final String WZ_DESC = "WizardProjectConversion.description"; //$NON-NLS-1$
    ConvertToRemoteServiceModelWidget fServiceModelWidget;
	Group remoteServices;
	
	/**
	 * Constructor for ConvertToRemoteWizardPage.
	 * @param pageName
	 */
	public ConvertToRemoteWizardPage(String pageName) {
		super(pageName);
		fServiceModelWidget = new ConvertToRemoteServiceModelWidget();
		fServiceModelWidget.setConfigChangeListener(new Listener() {
			public void handleEvent(Event event) {
				setPageComplete(validatePage());				
			}			
		});
	}
    
    /**
     * Method getWzTitleResource returns the correct Title Label for this class
     * overriding the default in the superclass.
     */
    protected String getWzTitleResource(){
        return Messages.getString(WZ_TITLE);
    }
    
	/**
     * Method getWzDescriptionResource returns the correct description
     * Label for this class overriding the default in the superclass.
     */
    protected String getWzDescriptionResource(){
        return Messages.getString(WZ_DESC);
    }
       
    /**
     * Returns true for:
     * - non-hidden projects
     * - non-RDT projects 
     * - projects that does not have remote systems temporary nature
     */
    public boolean isCandidate(IProject project) {
    	boolean a = false;
    	boolean b = false;
    	boolean c = false;
    	a = !project.isHidden();    	
		try {
			b = !project.hasNature(RemoteNature.REMOTE_NATURE_ID);
			c = !project.hasNature("org.eclipse.rse.ui.remoteSystemsTempNature"); //$NON-NLS-1$
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return a && b && c; 
    }
    
    /**
     * Add remote nature and configure remote services for the project
     */
    public void convertProject(IProject project, String bsId, IProgressMonitor monitor) throws CoreException{
		monitor.beginTask(Messages.getString("WizardProjectConversion.monitor.convertingToRemoteProject"), 3); //$NON-NLS-1$
		try {
			RemoteNature.addRemoteNature(project, monitor);
			configureServicesForRemoteProject(project);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.done();
		}
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#convertProject(org.eclipse.core.resources.IProject, org.eclipse.core.runtime.IProgressMonitor, java.lang.String)
	 */
	@Override
	public void convertProject(IProject project, IProgressMonitor monitor, String projectID) throws CoreException {
		monitor.beginTask(Messages.getString("WizardProjectConversion.monitor.convertingToRemoteProject"), 3); //$NON-NLS-1$
		try {
			RemoteNature.addRemoteNature(project, monitor);
			configureServicesForRemoteProject(project);
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			monitor.done();
		}
	}

	@Override
	protected void addToMainPage(Composite container) {
		remoteServices = new Group(container, SWT.SHADOW_IN);
		remoteServices.setText(Messages.getString("WizardProjectConversion.servicesTableLabel")); //$NON-NLS-1$
		remoteServices.setLayout(new FillLayout());
		remoteServices.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		//create the table for remote services
		fServiceModelWidget.createContents(remoteServices);
		//remove all the services in the table for now and add them back as project gets selected in the project list
		fServiceModelWidget.emptyTable();
	}

	public void createControl(Composite parent) {
		super.createControl(parent);
		tableViewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent e) {
            	IProject project = (IProject) ((IStructuredSelection)e.getSelection()).getFirstElement();
                if (project != null) {
	            	//update the table with remote services available for the project selected
	            	fServiceModelWidget.addServicesToTable(project);
	            	remoteServices.setText(MessageFormat.format(Messages.getString("WizardProjectConversion.servicesTableForProjectLabel"), new Object[] {project.getName()}));  //$NON-NLS-1$
                }
                fServiceModelWidget.updateConfigureButton(false);
            }
        });		
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {				
				IProject project = (IProject) e.getElement();
				if (e.getChecked() && project != null) {
	            	//update the table with remote services available for the project selected
	            	fServiceModelWidget.addServicesToTable(project);
	            	remoteServices.setText(MessageFormat.format(Messages.getString("WizardProjectConversion.servicesTableForProjectLabel"), new Object[] {project.getName()}));  //$NON-NLS-1$
				}							
			}			
		});
	}
	
	private void configureServicesForRemoteProject(IProject project) throws InvocationTargetException,
			InterruptedException {
		Map<IProject, Map<String,String>> projectToServices = fServiceModelWidget.getProjectToServices();
		Map<IProject, Map<String,IServiceProvider>> projectToProviders = fServiceModelWidget.getProjectToProviders();
		
		Map<String, String> serviceIDToProviderIDMap = projectToServices.get(project);
		Map<String, IServiceProvider> providerIDToProviderMap = projectToProviders.get(project);
		
		ConfigureRemoteServices.configure(project, serviceIDToProviderIDMap, providerIDToProviderMap);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		return super.validatePage() && fServiceModelWidget.isConfigured(getCheckedElements());
	}
}
