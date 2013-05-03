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
import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardNode;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.jface.wizard.WizardSelectionPage;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.IServiceContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.ptp.services.ui.widgets.ServiceModelWidget;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;

/**
 * Wizard for configuring a service configuration using the old style configuration
 * widget (a table with multiple columns) provided by {@link #ServiceModelWiget}
 * 
 * NOT CURRENTLY USED AND MAY BE DEPRECATED
 */
public class ServiceConfigurationWizard extends Wizard implements INewWizard {

	private class ServicesPage extends WizardSelectionPage {
		private class WizardExtensionNode implements IWizardNode {
			private IWizard fWizard = null;
			
			public WizardExtensionNode(IWizard wizard) {
				fWizard = wizard;
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
				return fWizard;
			}

			/* (non-Javadoc)
			 * @see org.eclipse.jface.wizard.IWizardNode#isContentCreated()
			 */
			public boolean isContentCreated() {
				return fWizard != null;
			}
			
		}
		
		public ServicesPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.ServiceConfigurationWizard_0);
		}

		public void createControl(Composite parent) {
			setControl(new ServiceModelWidget(fServiceConfiguration).createContents(parent));
			INewWizard wizard = (INewWizard)ServiceModelUIManager.getInstance().getWizardExtensions();
			if (wizard != null) {
				wizard.init(getWorkbench(), getSelection());
				wizard.addPages();
				setSelectedNode(new WizardExtensionNode(wizard));
			}
		}
	}

	private final IServiceConfiguration fServiceConfiguration;
	private IWorkbench fWorkbench = null;
	private IStructuredSelection fSelection = null;

	public ServiceConfigurationWizard(IServiceConfiguration serviceConfiguration) {
		setForcePreviousAndNextButtons(true);
		fServiceConfiguration = serviceConfiguration;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		setWizardPages(getWizardPagesFromServiceConfiguration(fServiceConfiguration));
		addPage(new ServicesPage(Messages.ServiceConfigurationWizard_1));
	}
	
	/**
	 * @return the Selection
	 */
	public IStructuredSelection getSelection() {
		return fSelection;
	}

	/**
	 * @return
	 */
	public IServiceConfiguration getServiceConfiguration() {
		return fServiceConfiguration;
	}
	
	/**
	 * @return the Workbench
	 */
	public IWorkbench getWorkbench() {
		return fWorkbench;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.IWorkbenchWizard#init(org.eclipse.ui.IWorkbench, org.eclipse.jface.viewers.IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		fWorkbench = workbench;
		fSelection = selection;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		return true;
	}
	
	private WizardPage[] getWizardPagesFromServiceConfiguration(IServiceConfiguration serviceConfiguration) {
		List<WizardPage> wizardPages = new ArrayList<WizardPage>();
		for (IService service : serviceConfiguration.getServicesByPriority()) {
			IServiceContributor contrib = ServiceModelUIManager.getInstance().getServiceContributor(service);
			if (contrib != null) {
				WizardPage[] pages = contrib.getWizardPages(service);
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
}
