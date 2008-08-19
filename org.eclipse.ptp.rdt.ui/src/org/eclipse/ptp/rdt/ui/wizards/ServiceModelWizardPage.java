/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * @author crecoskie
 *
 */
public class ServiceModelWizardPage extends MBSCustomPage {

	public static final String SERVICE_MODEL_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.ui.serviceModelWizardPage"; //$NON-NLS-1$
	public static final String SELECTED_PROVIDERS_MAP_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.selectedProviders"; //$NON-NLS-1$
	public static final String ID_TO_PROVIDERS_MAP_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.providersMap"; //$NON-NLS-1$

	boolean fbVisited;
	
	private String fTitle;
	
	private String fDescription;
	
	private ImageDescriptor fImageDescriptor;
	
	private Image fImage;
	
	private Control fCanvas;
	
	ServiceModelWidget fModelWidget;
	
	/**
	 * @param pageID
	 */
	public ServiceModelWizardPage(String pageID) {
		super(pageID);
		fModelWidget = new ServiceModelWidget();
		MBSCustomPageManager.addPageProperty(pageID, SELECTED_PROVIDERS_MAP_PROPERTY, fModelWidget.getServiceIDToSelectedProviderID());
		MBSCustomPageManager.addPageProperty(pageID, ID_TO_PROVIDERS_MAP_PROPERTY, fModelWidget.getProviderIDToProviderMap());
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
		return fbVisited;
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
		fCanvas = fModelWidget.createContents(parent);
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
		return fCanvas;
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
