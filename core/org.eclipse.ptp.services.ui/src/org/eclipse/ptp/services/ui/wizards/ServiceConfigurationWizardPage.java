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

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ServiceConfigurationWizardPage extends WizardPage {

	private final ServiceConfigurationWizard fWizard;
	private final IService fService;
	private IWizard fChildWizard = null;

	private ArrayList<IServiceProviderDescriptor> fProviderComboList = new ArrayList<IServiceProviderDescriptor>();
	
	/**
	 * @param fWizard
	 * @param pageName
	 */
	public ServiceConfigurationWizardPage(ServiceConfigurationWizard wizard, IService service, String pageName) {
		super(pageName);
		fWizard = wizard;
		fService = service;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        container.setLayout(layout);
        setControl(container);
        
        Label providerLabel = new Label(container, SWT.LEFT);
        providerLabel.setText("Select a provider");
        
        final Combo providerCombo = new Combo(container, SWT.DROP_DOWN | SWT.READ_ONLY);
        providerCombo.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
        providerCombo.addSelectionListener(new SelectionListener() {

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			/* (non-Javadoc)
			 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt.events.SelectionEvent)
			 */
			public void widgetSelected(SelectionEvent e) {
				int index = providerCombo.getSelectionIndex();
				IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(fProviderComboList.get(index));
				IServiceConfiguration config = getConfigurationWizard().getServiceConfiguration();
				fChildWizard = null; // need to set fChildWizard to null so we get the next service configuration page
				IWizardPage page = getNextPage();
				fChildWizard = new ServiceProviderConfigurationWizard(config, provider, page);
			}
        });

        createComboContents(providerCombo);
	}

	/**
     * The <code>WizardSelectionPage</code> implementation of 
     * this <code>IWizardPage</code> method returns the first page 
     * of the currently selected wizard if there is one.
     */
    public IWizardPage getNextPage() {
    	if (fChildWizard != null) {
    		return fChildWizard.getStartingPage();
    	}
    	return super.getNextPage();
    }

	/**
	 * Return the set of providers sorted by priority
	 * 
	 * @param service service containing providers
	 * @return sorted providers
	 */
	private Set<IServiceProviderDescriptor> getProvidersByPriority(IService service) {
		SortedSet<IServiceProviderDescriptor> sortedProviders = 
			new TreeSet<IServiceProviderDescriptor>(new Comparator<IServiceProviderDescriptor>() {
				public int compare(IServiceProviderDescriptor o1, IServiceProviderDescriptor o2) {
					int res = o1.getPriority().compareTo(o2.getPriority());
					return res;
				}
			});
		for (IServiceProviderDescriptor p : service.getProviders()) {
			sortedProviders.add(p);
		}
		
		return sortedProviders;
	}
	
	/**
	 * @param combo
	 */
	protected void createComboContents(Combo combo) {
		int index = 0;
		combo.removeAll();
		for (IServiceProviderDescriptor descriptor : getProvidersByPriority(getService())) {
			combo.add(descriptor.getName());
			fProviderComboList.add(index++, descriptor);
		}
	}
	
	/**
	 * @return
	 */
	protected ServiceConfigurationWizard getConfigurationWizard() {
		return fWizard;
	}
	
    /**
	 * @return
	 */
	protected IService getService() {
		return fService;
	}
}
