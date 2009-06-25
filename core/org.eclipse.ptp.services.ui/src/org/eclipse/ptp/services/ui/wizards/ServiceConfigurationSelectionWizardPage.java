/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.services.ui.wizards;

import java.util.Set;
import java.util.SortedSet;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

public class ServiceConfigurationSelectionWizardPage extends WizardSelectionPage {

	private class ServiceNode implements IWizardNode {
		private INewWizard fWizard = null;
		private IWorkbench fWorkbench = null;
		private IStructuredSelection fSelection = null;

		public ServiceNode(IWorkbench workbench, IStructuredSelection selection) {
			fWorkbench = workbench;
			fSelection = selection;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardNode#dispose()
		 */
		public void dispose() {
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardNode#getExtent()
		 */
		public Point getExtent() {
			return new Point(-1, -1);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardNode#getWizard()
		 */
		public IWizard getWizard() {
			if (fWizard == null) {
				fWizard = new ServiceConfigurationWizard(getServiceConfiguration());
				fWizard.addPages();
				fWizard.init(fWorkbench, fSelection);
			}
			return fWizard;
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
		 */
		public boolean isContentCreated() {
			return fWizard != null;
		}
		
	}
	
	private IServiceConfiguration fServiceConfiguration;

	/**
	 * @param fWizard
	 * @param pageName
	 */
	public ServiceConfigurationSelectionWizardPage(String pageName) {
		super(pageName);
		setTitle(pageName);
		setDescription("Create or select a service configuration for the project");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        setControl(container);
        
        Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getWorkspaceConfigurations();
        
        if (configs.size() > 0) {
	        Label label1 = new Label(container, SWT.LEFT);
	        label1.setText("One or more service configurations already exist.");

	        Label label2 = new Label(container, SWT.LEFT);
	        label2.setText("If you wish to use an existing configuration, select it from the list.");
	        
	        new ServiceConfigurationWidget(configs).createContents(container);
        } else {
	        Label label1 = new Label(container, SWT.LEFT);
	        label1.setText("You do not have any existing service configurations, so this wizard will help you create a new one.");

	        Label label2 = new Label(container, SWT.LEFT);
	        label2.setText("The new configuration will be saved with your project, and can be used when creating new projects in the future.");
	        
	        IServiceConfiguration config = new ServiceConfiguration("Default");
	        for (IService service : ServiceModelManager.getInstance().getServices()) {
				SortedSet<IServiceProviderDescriptor> providers = service.getProvidersByPriority();
				if (providers.size() > 0) {
					IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(providers.iterator().next());
					config.setServiceProvider(service, provider);
				}
			}
	        
	        setServiceConfiguration(config);
	        setSelectedNode(new ServiceNode(((NewServiceConfigurationProjectWizard)getWizard()).getWorkbench(), ((NewServiceConfigurationProjectWizard)getWizard()).getSelection()));
        }
	}

	/**
	 * @return the fServiceConfiguration
	 */
	private IServiceConfiguration getServiceConfiguration() {
		return fServiceConfiguration;
	}
	
	/**
	 * @param fServiceConfiguration the fServiceConfiguration to set
	 */
	private void setServiceConfiguration(IServiceConfiguration fServiceConfiguration) {
		this.fServiceConfiguration = fServiceConfiguration;
	}
}
