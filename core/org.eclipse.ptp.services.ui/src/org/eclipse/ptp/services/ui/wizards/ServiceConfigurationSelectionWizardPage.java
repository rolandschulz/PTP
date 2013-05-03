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
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Main page used by the NewServiceConfigurationProjectWizard to select an existing
 * service configuration or create a new one.
 * 
 * NOT CURRENTLY USED AND MAY BE DEPRECATED
 */
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
		setDescription(Messages.ServiceConfigurationSelectionWizardPage_0);
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
        
        IServiceModelManager manager = ServiceModelManager.getInstance();
        Set<IServiceConfiguration> configs = manager.getConfigurations();
        
        if (configs.size() > 0) {
	        Label label1 = new Label(container, SWT.LEFT);
	        label1.setText(Messages.ServiceConfigurationSelectionWizardPage_1);

	        Label label2 = new Label(container, SWT.LEFT);
	        label2.setText(Messages.ServiceConfigurationSelectionWizardPage_2);
	        
	        new ServiceConfigurationWidget(configs).createContents(container);
        } else {
	        Label label1 = new Label(container, SWT.LEFT);
	        label1.setText(Messages.ServiceConfigurationSelectionWizardPage_3);

	        Label label2 = new Label(container, SWT.LEFT);
	        label2.setText(Messages.ServiceConfigurationSelectionWizardPage_4);
	        
	        IServiceConfiguration config = manager.newServiceConfiguration(Messages.ServiceConfigurationSelectionWizardPage_5);
	        for (IService service : manager.getServices()) {
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
