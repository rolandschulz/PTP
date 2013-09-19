/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.IRemoteToolsIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.serviceproviders.RSECIndexServiceProvider;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.widgets.AddServiceConfigurationWidget;
import org.eclipse.rse.connectorservice.dstore.DStoreConnectorService;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

/**
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class ServiceModelWizardPage extends MBSCustomPage {
	public static final String SERVICE_MODEL_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.ui.serviceModelWizardPage"; //$NON-NLS-1$
	public static final String DEFAULT_CONFIG = Messages.getString("ConfigureRemoteServices.0"); //$NON-NLS-1$
	/**
	 * @since 2.0
	 */
	public static final String CONFIG_PROPERTY = "org.eclipse.ptp.rdt.ui.ServiceModelWizardPage.serviceConfig"; //$NON-NLS-1$

	boolean fbVisited;
	private String fTitle;
	private String fDescription;
	private ImageDescriptor fImageDescriptor;
	private Image fImage;
	private IServiceConfiguration fNewConfig;
	private Control pageControl;

	private AddServiceConfigurationWidget serviceConfigWidget;

	public ServiceModelWizardPage(String pageID) {
		super(pageID);
	}

	/**
	 * Find available remote services and service providers for a given project
	 * 
	 * If project is null, the C and C++ natures are used to determine which
	 * services are available
	 */
	protected Set<IService> getContributedServices() {
		ServiceModelManager smm = ServiceModelManager.getInstance();
		Set<IService> cppServices = smm.getServices(CCProjectNature.CC_NATURE_ID);
		Set<IService> cServices = smm.getServices(CProjectNature.C_NATURE_ID);

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPage#isCustomPageComplete
	 * ()
	 */
	@Override
	protected boolean isCustomPageComplete() {
		return fbVisited;// && fModelWidget.isConfigured();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.IWizardPage#getName()
	 */
	public String getName() {
		return Messages.getString("ServiceModelWizardPage_0"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets
	 * .Composite)
	 */
	public void createControl(final Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(1, false));
		comp.setLayoutData(new GridData(GridData.FILL_BOTH));
		pageControl = comp;

		serviceConfigWidget = new AddServiceConfigurationWidget(comp, SWT.NONE);
		GridData data = new GridData(GridData.FILL_BOTH);
		serviceConfigWidget.setLayoutData(data);
		serviceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
			public void selectionChanged(SelectionChangedEvent event) {
				updateConfigPageProperty();
			}
		});
		serviceConfigWidget.setDefaultConfiguration(getNewConfiguration());
		serviceConfigWidget.setSelection(true);

		updateConfigPageProperty();
	}

	private void updateConfigPageProperty() {
		IServiceConfiguration config = serviceConfigWidget.getServiceConfiguration();
		MBSCustomPageManager.addPageProperty(SERVICE_MODEL_WIZARD_PAGE_ID, CONFIG_PROPERTY, config);

	}

	private String getDefaultConfigName() {
		String candidateName = DEFAULT_CONFIG;
		IWizardPage page = getWizard().getStartingPage();
		if (page instanceof NewRemoteProjectCreationPage) {
			NewRemoteProjectCreationPage cdtPage = (NewRemoteProjectCreationPage) page;
			candidateName = cdtPage.getRemoteConnection().getName();
		}

		Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getConfigurations();
		Set<String> existingNames = new HashSet<String>();
		for (IServiceConfiguration config : configs) {
			existingNames.add(config.getName());
		}

		int i = 2;
		String newConfigName = candidateName;
		while (existingNames.contains(newConfigName)) {
			newConfigName = candidateName + " (" + (i++) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return newConfigName;
	}

	/**
	 * Creates a new configuration with the RDT defaults.
	 */
	private IServiceConfiguration getNewConfiguration() {
		if (fNewConfig == null) {
			IServiceModelManager smm = ServiceModelManager.getInstance();
			fNewConfig = smm.newServiceConfiguration(getDefaultConfigName());

			IWizardPage page = getWizard().getStartingPage();
			if (page instanceof NewRemoteProjectCreationPage) {
				NewRemoteProjectCreationPage cdtPage = (NewRemoteProjectCreationPage) page;
				IRemoteServices remoteServices = cdtPage.getRemoteServices();
				IRemoteConnection remoteConnection = cdtPage.getRemoteConnection();

				IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
				IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
				RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
				if (rbsp != null) {
					rbsp.setRemoteToolsConnection(remoteConnection);
					fNewConfig.setServiceProvider(buildService, rbsp);
				}

				IService indexingService = smm.getService(IRDTServiceConstants.SERVICE_C_INDEX);
				if (remoteServices.getId().equals("org.eclipse.ptp.remote.RSERemoteServices")) { //$NON-NLS-1$
					descriptor = indexingService.getProviderDescriptor(RSECIndexServiceProvider.ID);
					RSECIndexServiceProvider provider = (RSECIndexServiceProvider) smm.getServiceProvider(descriptor);
					if (provider != null) {
							String hostName = remoteConnection.getAddress();
							IHost host = RSEUtils.getConnection(hostName);
							String configPath = RSEUtils.getDefaultConfigDirectory(host);
		
							provider.setConnection(host, getDStoreConnectorService(host));
							provider.setIndexLocation(configPath);
							provider.setConfigured(true);
							fNewConfig.setServiceProvider(indexingService, provider);
					}
				} else if (remoteServices.getId().equals("org.eclipse.ptp.remote.RemoteTools")) { //$NON-NLS-1$
					descriptor = indexingService
							.getProviderDescriptor("org.eclipse.ptp.rdt.server.dstore.RemoteToolsCIndexServiceProvider"); //$NON-NLS-1$
					IRemoteToolsIndexServiceProvider provider = (IRemoteToolsIndexServiceProvider) smm
							.getServiceProvider(descriptor);
					if (provider != null) {
						provider.setConnection(remoteConnection);
						fNewConfig.setServiceProvider(indexingService, provider);
					}
				}
			}
		}

		return fNewConfig;
	}

	private IConnectorService getDStoreConnectorService(IHost host) {
		for (IConnectorService cs : host.getConnectorServices()) {
			if (cs instanceof DStoreConnectorService)
				return cs;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#dispose()
	 */
	public void dispose() {
		// TODO Auto-generated method stub
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getControl()
	 */
	public Control getControl() {
		return pageControl;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getDescription()
	 */
	public String getDescription() {
		if (fDescription == null)
			fDescription = Messages.getString("ServiceModelWizardPage_description"); //$NON-NLS-1$
		return fDescription;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	public String getErrorMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getImage()
	 */
	public Image getImage() {
		if (fImage == null && fImageDescriptor != null)
			fImage = fImageDescriptor.createImage();

		if (fImage == null && wizard != null) {
			fImage = wizard.getDefaultPageImage();
		}

		return fImage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	public String getMessage() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getTitle()
	 */
	public String getTitle() {
		if (fTitle == null)
			fTitle = Messages.getString("ServiceModelWizardPage_0"); //$NON-NLS-1$
		return fTitle;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#performHelp()
	 */
	public void performHelp() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		fDescription = description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.dialogs.IDialogPage#setImageDescriptor(org.eclipse.
	 * jface.resource.ImageDescriptor)
	 */
	public void setImageDescriptor(ImageDescriptor image) {
		fImageDescriptor = image;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setTitle(java.lang.String)
	 */
	public void setTitle(String title) {
		fTitle = title;

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#setVisible(boolean)
	 */
	public void setVisible(boolean visible) {
		if (visible) {
			fbVisited = true;
		}
	}

}
