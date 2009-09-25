/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTMainWizardPage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.widgets.NewServiceModelWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author crecoskie
 *
 */
public class ServiceModelWizardPage extends MBSCustomPage {

	public static final String SERVICE_MODEL_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.ui.serviceModelWizardPage"; //$NON-NLS-1$
	public static final String DEFAULT_CONFIG = Messages.getString("ConfigureRemoteServices.0"); //$NON-NLS-1$
	
	public static final String SERVICE_MODEL_WIDGET_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.serviceModelWidget"; //$NON-NLS-1$

	boolean fbVisited;
	private String fTitle;
	private String fDescription;
	private ImageDescriptor fImageDescriptor;
	private Image fImage;
	private Control fCanvas;
	private IServiceConfiguration fConfig;
	private NewServiceModelWidget fModelWidget;
	

	public ServiceModelWizardPage(String pageID) {
		super(pageID);
	}

	/**
	 * Find available remote services and service providers for a given project
	 * 
	 * If project is null, the C and C++ natures are used to determine which services
	 * are available
	 */
	protected Set<IService> getContributedServices() {		
		ServiceModelManager smm = ServiceModelManager.getInstance();
		Set<IService> cppServices = smm.getServices(CCProjectNature.CC_NATURE_ID);
		Set<IService> cServices   = smm.getServices(CProjectNature.C_NATURE_ID);
		
		Set<IService> allApplicableServices = new LinkedHashSet<IService>();
		allApplicableServices.addAll(cppServices);
		allApplicableServices.addAll(cServices);
		
		return allApplicableServices;
	}
	
	/**
	 * 
	 */
	public ServiceModelWizardPage() {
		this(SERVICE_MODEL_WIZARD_PAGE_ID);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete()
	 */
	protected boolean isCustomPageComplete() {
		return fbVisited;// && fModelWidget.isConfigured();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return Messages.getString("ServiceModelWizardPage_0"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		fCanvas = parent; // TODO
		fModelWidget = new NewServiceModelWidget(parent, SWT.NONE);
		
		MBSCustomPageManager.addPageProperty(pageID, SERVICE_MODEL_WIDGET_PROPERTY, fModelWidget);
		
		String configName = DEFAULT_CONFIG;
		IWizardPage page = getWizard().getStartingPage();
		if(page instanceof CDTMainWizardPage) {
			CDTMainWizardPage cdtPage = (CDTMainWizardPage) page;
			configName = cdtPage.getProjectName();
		}
		
		fConfig = ServiceModelManager.getInstance().newServiceConfiguration(configName);
		
		fModelWidget.setServiceConfiguration(fConfig);
		
		Control control = fModelWidget.getParent().getShell(); //get the shell or doesn't display help correctly
		PlatformUI.getWorkbench().getHelpSystem().setHelp(control,RDTHelpContextIds.SERVICE_MODEL_WIZARD);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return fModelWidget;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		if (fDescription == null)
			fDescription = Messages.getString("ServiceModelWizardPage_description"); //$NON-NLS-1$
		return fDescription;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		if(fImage == null && fImageDescriptor != null)
			fImage = fImageDescriptor.createImage();
		
		if (fImage == null && wizard != null) {
			fImage = wizard.getDefaultPageImage();
		}
		
		return fImage;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		if (fTitle == null)
			fTitle = Messages.getString("ServiceModelWizardPage_0"); //$NON-NLS-1$
		return fTitle;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		fDescription = description;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		fImageDescriptor = image;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		fTitle = title;

	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if(visible) {
			fbVisited = true;
		}
		fCanvas.setVisible(visible);
	}


}
