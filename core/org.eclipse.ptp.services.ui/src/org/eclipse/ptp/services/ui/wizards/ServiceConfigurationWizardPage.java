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

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Wizard page used by {@link #ServiceConfigurationWizard} to select a service provider for 
 * a particular service.
 * 
 * NOT CURRENTLY USED AND MAY BE DEPRECATED
 *
 */
public class ServiceConfigurationWizardPage extends WizardPage {

	private final IService fService;
	private IWizard fChildWizard = null;

	private ArrayList<IServiceProviderDescriptor> fProviderComboList = new ArrayList<IServiceProviderDescriptor>();
	
	/**
	 * @param fWizard
	 * @param pageName
	 */
	public ServiceConfigurationWizardPage(IService service, String pageName) {
		super(pageName);
		fService = service;
		setPageComplete(false);
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
        providerLabel.setText(Messages.ServiceConfigurationWizardPage_0);
        
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
				handleComboSelection(providerCombo);
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
	 * @param combo
	 */
	private void handleComboSelection(Combo combo) {
		int index = combo.getSelectionIndex();
		IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(fProviderComboList.get(index));
		IServiceConfiguration config = ((ServiceConfigurationWizard)getWizard()).getServiceConfiguration();
		config.setServiceProvider(getService(), provider);
		fChildWizard = null; // need to set fChildWizard to null so we get the next service configuration page
		if (!provider.isConfigured()) {
			IWizardPage page = getNextPage();
			IServiceProviderContributor contrib = ServiceModelUIManager.getInstance().getServiceProviderContributor(provider);
			if (contrib != null) {
				fChildWizard = contrib.getWizard(provider, page);
			}
		}
		setPageComplete(true);
	}
	
	/**
	 * @param combo
	 */
	protected void createComboContents(Combo combo) {
		IServiceConfiguration config = ((ServiceConfigurationWizard)getWizard()).getServiceConfiguration();
		IServiceProvider provider = config.getServiceProvider(getService());
		int index = 0;
		int selection = 0;
		combo.removeAll();
		for (IServiceProviderDescriptor descriptor : getService().getProvidersByPriority()) {
			combo.add(descriptor.getName());
			fProviderComboList.add(index, descriptor);
			if (descriptor.getId().equals(provider.getId())) {
				selection = index;
			}
			index++;
		}
		combo.select(selection);
		handleComboSelection(combo);
	}
	
    /**
	 * @return
	 */
	protected IService getService() {
		return fService;
	}
}
