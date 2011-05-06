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
package org.eclipse.ptp.ui.wizards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

/**
 * @since 5.0
 */
public class RMConfigurationWizard extends Wizard implements IRMConfigurationWizard {

	/**
	 * Wizard page for common resource manager configuration. This is the final
	 * page that the wizard will display.
	 */
	private class ResourceManagerPage extends WizardPage {

		private Text descText;
		private Text nameText;
		private Button autoStartButton;

		public ResourceManagerPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.RMServicesConfigurationWizard_2);
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setFont(parent.getFont());
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			createRMControl(composite);

			setControl(composite);
		}

		public String getRMDesc() {
			return descText.getText();
		}

		public String getRMName() {
			return nameText.getText();
		}

		private void createRMControl(Composite container) {
			Group nameGroup = new Group(container, SWT.SHADOW_ETCHED_IN);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			nameGroup.setLayout(layout);
			nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameGroup.setText(Messages.RMServicesConfigurationWizard_11);

			final Button useDefaultsButton = new Button(nameGroup, SWT.TOGGLE | SWT.CHECK);
			useDefaultsButton.setText(Messages.RMServicesConfigurationWizard_3);
			useDefaultsButton.setSelection(fUseDefaultNameAndDesc);
			GridData buttonData = new GridData();
			buttonData.horizontalSpan = 2;
			useDefaultsButton.setLayoutData(buttonData);
			useDefaultsButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					fUseDefaultNameAndDesc = useDefaultsButton.getSelection();
					if (fUseDefaultNameAndDesc) {
						getBaseConfiguration().setDefaultNameAndDesc();
						setNameAndDescription(getBaseConfiguration());
						setPageComplete(true);
						setErrorMessage(null);
					}
					fResourceManagerPage.setEnabled(!fUseDefaultNameAndDesc);
					getContainer().updateButtons();
				}
			});

			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(Messages.RMServicesConfigurationWizard_4);
			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setText(""); //$NON-NLS-1$
			nameText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					if (nameText.getText().equals(EMPTY_STRING)) {
						setPageComplete(false);
						setErrorMessage(Messages.RMServicesConfigurationWizard_5);
					} else {
						getBaseConfiguration().setName(nameText.getText());
						setPageComplete(true);
						setErrorMessage(null);
					}
				}
			});

			Label descLabel = new Label(nameGroup, SWT.NONE);
			descLabel.setText(Messages.RMServicesConfigurationWizard_6);
			descText = new Text(nameGroup, SWT.BORDER);
			descText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			descText.setText(""); //$NON-NLS-1$
			descText.addModifyListener(new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					getBaseConfiguration().setDescription(descText.getText());
				}
			});

			Group startGroup = new Group(container, SWT.SHADOW_ETCHED_IN);
			startGroup.setText(Messages.RMServicesConfigurationWizard_12);
			startGroup.setLayout(new GridLayout(1, true));
			startGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			autoStartButton = new Button(startGroup, SWT.CHECK);
			autoStartButton.setText(Messages.RMServicesConfigurationWizard_13);
			autoStartButton.addSelectionListener(new SelectionAdapter() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see
				 * org.eclipse.swt.events.SelectionAdapter#widgetSelected(org
				 * .eclipse.swt.events.SelectionEvent)
				 */
				@Override
				public void widgetSelected(SelectionEvent e) {
					getBaseConfiguration().setAutoStart(autoStartButton.getSelection());
				}

			});

			setPageComplete(true);
		}

		private void setAutoStart(boolean flag) {
			autoStartButton.setSelection(flag);
		}

		private void setEnabled(boolean enabled) {
			nameText.setEnabled(enabled);
			descText.setEnabled(enabled);
		}

		private void setNameAndDescription(IResourceManagerConfiguration config) {
			nameText.setText(config.getName());
			descText.setText(config.getDescription());
		}
	}

	/**
	 * Wizard page for selecting the resource manager type.
	 * 
	 * Resource manager types correspond to service providers for the Launch
	 * service that implement IResourceManagerConfiguration.
	 * 
	 */
	private class SelectServiceProviderPage extends WizardPage {

		private class ProviderInfo implements Comparable<ProviderInfo> {
			public final String name;
			public final IServiceProviderDescriptor descriptor;
			public final RMConfigurationSelectionFactory factory;

			public ProviderInfo(String name, IServiceProviderDescriptor descriptor, RMConfigurationSelectionFactory factory) {
				this.name = name;
				this.descriptor = descriptor;
				this.factory = factory;
			}

			public int compareTo(ProviderInfo o) {
				return name.compareTo(o.name);
			}
		}

		private List fServiceProviderList;

		private final ArrayList<ProviderInfo> fProviders = new ArrayList<ProviderInfo>();

		public SelectServiceProviderPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.RMServicesConfigurationWizard_7);
		}

		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setFont(parent.getFont());
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			createServiceProviderChoiceControl(composite);

			parent.getDisplay().asyncExec(new Runnable() {

				public void run() {
					fServiceProviderList.select(0);
					handleProviderSelection();
				}
			});

			setControl(composite);
		}

		private void createServiceProviderChoiceControl(Composite container) {
			/*
			 * Locate all service providers. The initial list of providers comes
			 * from the launch service. Providers can also register an extension
			 * factory to allow for more complex behaviors.
			 * 
			 * TODO: This assumes all LAUNCH_SERVICE providers implement
			 * IResourceManagerConfiguration Probably better to create an
			 * RM_LAUNCH_SERVICE instead.
			 */
			Set<IServiceProviderDescriptor> providers = fLaunchService.getProvidersByPriority();
			fProviders.clear();
			for (IServiceProviderDescriptor desc : providers) {
				/*
				 * Check if this provider has an extension
				 */
				RMConfigurationSelectionFactory factory = RMProviderContributor.getRMConfigurationSelectionFactory(desc.getId());
				if (factory != null) {
					for (String name : factory.getConfigurationNames()) {
						fProviders.add(new ProviderInfo(name, desc, factory));
					}
				} else {
					fProviders.add(new ProviderInfo(desc.getName(), desc, null));
				}
			}
			Collections.sort(fProviders);
			String[] providerNames = new String[fProviders.size()];
			for (int i = 0; i < fProviders.size(); i++) {
				providerNames[i] = fProviders.get(i).name;
			}

			Label label = new Label(container, SWT.NONE);
			label.setText(Messages.RMServicesConfigurationWizard_8);
			fServiceProviderList = new List(container, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			fServiceProviderList.setLayoutData(new GridData(GridData.FILL_BOTH));
			fServiceProviderList.setItems(providerNames);
			fServiceProviderList.setEnabled(fProviders.size() > 0);
			fServiceProviderList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleProviderSelection();
				}
			});
		}

		private void handleProviderSelection() {
			ProviderInfo providerInfo = fProviders.get(fServiceProviderList.getSelectionIndex());
			IServiceProvider provider = fServiceModelManager.getServiceProvider(providerInfo.descriptor);
			if (provider != null) {
				fBaseConfiguration = ModelManager.getInstance().createBaseConfiguration(provider);
				if (providerInfo.factory != null) {
					providerInfo.factory.setConfigurationName(providerInfo.name, getBaseConfiguration());
				}
				setWizardPages(provider);
			}
			setPageComplete(true);
		}
	}

	public final static String EMPTY_STRING = ""; //$NON-NLS-1$

	private final ArrayList<IWizardPage> fWizardPages = new ArrayList<IWizardPage>();
	private final SelectServiceProviderPage fSelectServiceProviderPage = new SelectServiceProviderPage(
			Messages.RMServicesConfigurationWizard_9);
	private final ResourceManagerPage fResourceManagerPage = new ResourceManagerPage(Messages.RMServicesConfigurationWizard_10);
	private final IServiceModelManager fServiceModelManager = ServiceModelManager.getInstance();
	private final IService fLaunchService = ServiceModelManager.getInstance().getService(IServiceConstants.LAUNCH_SERVICE);

	private final Map<String, IWizardPage[]> fCachedPages = new HashMap<String, IWizardPage[]>();
	private boolean fUseDefaultNameAndDesc = true;
	private boolean fEditMode = false;
	private IServiceProviderWorkingCopy fServiceProvider = null;
	private IResourceManagerConfiguration fBaseConfiguration = null;

	/*
	 * Constructor used when creating a new resource manager.
	 */
	/**
	 * @since 5.0
	 */
	public RMConfigurationWizard() {
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWizardPages(null);
	}

	/*
	 * Constructor used when editing configuration of an existing resource
	 * manager.
	 */
	/**
	 * @since 5.0
	 */
	public RMConfigurationWizard(IResourceManager rm) {
		this();
		fUseDefaultNameAndDesc = false;
		fEditMode = true;
		IServiceProvider provider = (IServiceProvider) rm.getConfiguration().getAdapter(IServiceProvider.class);
		fServiceProvider = provider.copy();
		fBaseConfiguration = ModelManager.getInstance().createBaseConfiguration(fServiceProvider);
		setWizardPages(fServiceProvider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (!fEditMode) {
			addPage(fSelectServiceProviderPage);
		}
		addPage(fResourceManagerPage);
		super.addPages();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
	@Override
	public boolean canFinish() {
		int numPages = getNumPages();
		for (int i = 0; i < numPages; ++i) {
			WizardPage page = (WizardPage) fWizardPages.get(i);
			if (!page.isPageComplete()) {
				return false;
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getBaseConfiguration()
	 */
	/**
	 * @since 5.0
	 */
	public IResourceManagerConfiguration getBaseConfiguration() {
		return fBaseConfiguration;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return fResourceManagerPage.getRMDesc();
	}

	/**
	 * @return
	 */
	public String getName() {
		return fResourceManagerPage.getRMName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.
	 * IWizardPage)
	 */
	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		int numPages = getNumPages();

		int index = fWizardPages.indexOf(page);
		if (index == numPages - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		IWizardPage nextPage = fWizardPages.get(index + 1);
		if (nextPage instanceof ResourceManagerPage && getBaseConfiguration() != null) {
			// initialize last page
			if (fUseDefaultNameAndDesc) {
				getBaseConfiguration().setDefaultNameAndDesc();
			}
			fResourceManagerPage.setNameAndDescription(getBaseConfiguration());
			fResourceManagerPage.setEnabled(!fUseDefaultNameAndDesc);
			fResourceManagerPage.setAutoStart(getBaseConfiguration().getAutoStart());
		}
		return nextPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard
	 * .IWizardPage)
	 */
	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		int index = fWizardPages.indexOf(page);
		if (index == 0 || index == -1) {
			// first page or page not found
			return null;
		} else {
			return fWizardPages.get(index - 1);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	@Override
	public boolean performFinish() {
		if (fUseDefaultNameAndDesc) {
			getBaseConfiguration().setDefaultNameAndDesc();
		}
		if (!fEditMode) {
			IServiceConfiguration config = fServiceModelManager.newServiceConfiguration(fResourceManagerPage.getRMName());
			config.setServiceProvider(fLaunchService, (IServiceProvider) getBaseConfiguration().getAdapter(IServiceProvider.class));
			fServiceModelManager.addConfiguration(config);
		} else {
			fServiceProvider.save();
		}
		return true;
	}

	/**
	 * @return
	 */
	private int getNumPages() {
		return fWizardPages.size();
	}

	private void setWizardPages(IServiceProvider provider) {
		// If this factory has not been selected before
		// then we must get the additional wizard pages for
		// the factory and cache them.
		IWizardPage[] pages = null;
		if (provider != null) {
			pages = fCachedPages.get(provider.getDescriptor().getId());
			if (pages == null) {
				IServiceProviderContributor contributor = ServiceModelUIManager.getInstance().getServiceProviderContributor(
						provider.getDescriptor());
				if (contributor != null) {
					pages = contributor.getWizardPages(this, provider);
					for (IWizardPage page : pages) {
						addPage(page);
					}
					fCachedPages.put(provider.getDescriptor().getId(), pages);
				}
			}
		}

		fWizardPages.clear();
		if (!fEditMode) {
			fWizardPages.add(fSelectServiceProviderPage);
		}
		if (pages != null) {
			fWizardPages.addAll(Arrays.asList(pages));
		}
		fWizardPages.add(fResourceManagerPage);
	}

}
