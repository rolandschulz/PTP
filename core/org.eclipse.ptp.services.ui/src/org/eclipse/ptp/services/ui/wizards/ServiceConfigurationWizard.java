/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.services.ui.wizards;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.IServiceContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

public class ServiceConfigurationWizard extends Wizard {

	private class IntroPage extends WizardPage {

		public IntroPage(ServiceConfigurationWizard wizard, String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.ServiceConfigurationWizard_2);
		}

		public void createControl(Composite parent) {
			Composite canvas = new Composite(parent, SWT.NONE);
			GridLayout canvasLayout = new GridLayout(1, false);
			canvas.setLayout(canvasLayout);
			
			Label label = new Label(canvas, SWT.NONE);
			label.setText(Messages.ServiceConfigurationWizard_3);
			
			Label label2 = new Label(canvas, SWT.NONE);
			label2.setText(Messages.ServiceConfigurationWizard_4);
			
			Button button = new Button(canvas, SWT.CHECK);
			button.setText(Messages.ServiceConfigurationWizard_5);
			GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
			button.setLayoutData(data);
			
			setControl(canvas);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
		 */
		@Override
		public boolean isPageComplete() {
			return true;
		}
		
	}
	
	private class ServicesPage extends WizardPage {

		public ServicesPage(ServiceConfigurationWizard wizard, String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.ServiceConfigurationWizard_0);
		}

		public void createControl(Composite parent) {
			setControl(new ServiceModelWidget(fServiceConfiguration).createContents(parent));
		}
	}

	private final IServiceConfiguration fServiceConfiguration;

	public ServiceConfigurationWizard(IServiceConfiguration serviceConfiguration) {
		setForcePreviousAndNextButtons(true);
		fServiceConfiguration = serviceConfiguration;
		addPage(new IntroPage(this, Messages.ServiceConfigurationWizard_6));
		setWizardPages(getWizardPagesFromServiceConfiguration(serviceConfiguration));
		addPage(new ServicesPage(this, Messages.ServiceConfigurationWizard_1));
	}

	/**
	 * @return
	 */
	public IServiceConfiguration getServiceConfiguration() {
		return fServiceConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}

	private WizardPage[] getWizardPagesFromServiceConfiguration(IServiceConfiguration serviceConfiguration) {
		List<WizardPage> wizardPages = new ArrayList<WizardPage>();
		for (IService service : getServicesByPriority(serviceConfiguration)) {
			IServiceContributor contrib = ServiceModelUIManager.getInstance().getServiceContributor(service);
			if (contrib != null) {
				WizardPage[] pages = contrib.getWizardPages(this, service);
				for (WizardPage page : pages) {
					wizardPages.add(page);
				}
			}
		}
		
		return wizardPages.toArray(new WizardPage[wizardPages.size()]);
	}
	
	private void setWizardPages(WizardPage[] pages) {
		for (IWizardPage page : pages) {
			addPage(page);
		}
	}
	
	/**
	 * Return the set of providers sorted by priority
	 * 
	 * @param service service containing providers
	 * @return sorted providers
	 */
	private SortedSet<IService> getServicesByPriority(IServiceConfiguration serviceConfiguration) {
		SortedSet<IService> sortedServices = 
			new TreeSet<IService>(new Comparator<IService>() {
				public int compare(IService o1, IService o2) {
					return o1.getPriority().compareTo(o2.getPriority());
				}
			});
		for (IService s : serviceConfiguration.getServices()) {
			sortedServices.add(s);
		}
		
		return sortedServices;
	}
}
