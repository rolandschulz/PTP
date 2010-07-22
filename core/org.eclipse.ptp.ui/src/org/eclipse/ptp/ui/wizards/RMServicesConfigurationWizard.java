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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
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
import org.eclipse.ptp.services.ui.widgets.AddServiceConfigurationWidget;
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

public class RMServicesConfigurationWizard extends Wizard implements IRMConfigurationWizard {

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
						getConfiguration().setDefaultNameAndDesc();
						setNameAndDescription(getConfiguration());
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
						getConfiguration().setName(nameText.getText());
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
					getConfiguration().setDescription(descText.getText());
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
					getConfiguration().setAutoStart(autoStartButton.getSelection());
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
	 * Wizard page for selecting a service configuration from a list of service
	 * configurations.
	 */
	private class SelectServiceConfigurationPage extends WizardPage {
		private AddServiceConfigurationWidget serviceConfigWidget;

		public SelectServiceConfigurationPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.RMServicesConfigurationWizard_0);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt
		 * .widgets.Composite)
		 */
		public void createControl(Composite parent) {
			Composite composite = new Composite(parent, SWT.NULL);
			composite.setFont(parent.getFont());
			composite.setLayout(new GridLayout());
			composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			serviceConfigWidget = new AddServiceConfigurationWidget(composite, SWT.NONE, null,
					Collections.singleton(fLaunchService), false);
			GridData data = new GridData(GridData.FILL_BOTH);
			serviceConfigWidget.setLayoutData(data);
			serviceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					setPageComplete(getServiceConfiguration() != null);
				}
			});

			setControl(composite);
			setPageComplete(false);
		}

		/**
		 * Get the service configuration selected in the widget.
		 * 
		 * @return service configuration
		 */
		public IServiceConfiguration getServiceConfiguration() {
			return serviceConfigWidget.getServiceConfiguration();
		}

		/**
		 * Set the name that will be displayed in the create configuration text
		 * box.
		 * 
		 * @param name
		 */
		public void setDefaultConfiguration(IServiceConfiguration config) {
			serviceConfigWidget.setDefaultConfiguration(config);
			serviceConfigWidget.setSelection(false);
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

		private List fServiceProviderList;
		private IServiceProviderDescriptor[] fProviders;

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
			 * Locate all service providers
			 * 
			 * TODO: This assumes all LAUNCH_SERVICE providers implement
			 * IResourceManagerConfiguration Probably better to create an
			 * RM_LAUNCH_SERVICE instead.
			 */
			fProviders = fLaunchService.getProvidersByPriority().toArray(new IServiceProviderDescriptor[0]);
			String[] providerNames = new String[fProviders.length];
			for (int i = 0; i < fProviders.length; ++i) {
				providerNames[i] = fProviders[i].getName();
			}

			Label label = new Label(container, SWT.NONE);
			label.setText(Messages.RMServicesConfigurationWizard_8);
			fServiceProviderList = new List(container, SWT.SINGLE | SWT.BORDER | SWT.V_SCROLL);
			fServiceProviderList.setLayoutData(new GridData(GridData.FILL_BOTH));
			fServiceProviderList.setItems(providerNames);
			fServiceProviderList.setEnabled(fProviders.length > 0);
			fServiceProviderList.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					handleProviderSelection();
				}
			});
		}

		private void handleProviderSelection() {
			setServiceProvider(fModelManager.getServiceProvider(fProviders[fServiceProviderList.getSelectionIndex()]));
			setPageComplete(true);
		}
	}

	public final static String EMPTY_STRING = ""; //$NON-NLS-1$

	private final ArrayList<IWizardPage> fWizardPages = new ArrayList<IWizardPage>();
	private final SelectServiceProviderPage fSelectServiceProviderPage = new SelectServiceProviderPage(
			Messages.RMServicesConfigurationWizard_9);
	private final SelectServiceConfigurationPage fSelectServiceConfigurationPage = new SelectServiceConfigurationPage(
			Messages.RMServicesConfigurationWizard_1);
	private final ResourceManagerPage fResourceManagerPage = new ResourceManagerPage(Messages.RMServicesConfigurationWizard_10);
	private final IServiceModelManager fModelManager = ServiceModelManager.getInstance();
	private final IService fLaunchService = ServiceModelManager.getInstance().getService(IServiceConstants.LAUNCH_SERVICE);

	private final Map<String, IWizardPage[]> fCachedPages = new HashMap<String, IWizardPage[]>();
	private boolean fUseDefaultNameAndDesc = true;
	private boolean fUsingWorkingCopy = false;
	private IServiceProvider fServiceProvider = null;

	/*
	 * Constructor used when creating a new resource manager.
	 */
	public RMServicesConfigurationWizard() {
		setForcePreviousAndNextButtons(true);
		setNeedsProgressMonitor(true);
		setWizardPages(null);
	}

	/*
	 * Constructor used when editing configuration of an existing resource
	 * manager.
	 */
	public RMServicesConfigurationWizard(IResourceManagerControl resourceManager) {
		this();
		fUseDefaultNameAndDesc = false;
		fUsingWorkingCopy = true;
		setServiceProvider(((IServiceProvider) resourceManager.getConfiguration()).copy());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	@Override
	public void addPages() {
		if (!fUsingWorkingCopy) {
			addPage(fSelectServiceProviderPage);
		}
		addPage(fResourceManagerPage);
		if (!fUsingWorkingCopy) {
			addPage(fSelectServiceConfigurationPage);
		}
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
	 * @see org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getConfiguration()
	 */
	public IResourceManagerConfiguration getConfiguration() {
		/*
		 * We know the following cast is safe, because only service providers
		 * that implement IResourceManagerConfiguration can be selected
		 */
		return (IResourceManagerConfiguration) getServiceProvider();
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
		if (nextPage instanceof ResourceManagerPage && getConfiguration() != null) {
			// initialize last page
			if (fUseDefaultNameAndDesc) {
				getConfiguration().setDefaultNameAndDesc();
			}
			fResourceManagerPage.setNameAndDescription(getConfiguration());
			fResourceManagerPage.setEnabled(!fUseDefaultNameAndDesc);
			fResourceManagerPage.setAutoStart(getConfiguration().getAutoStart());
		} else if (nextPage instanceof SelectServiceConfigurationPage) {
			IServiceConfiguration config = fModelManager.newServiceConfiguration(fResourceManagerPage.getRMName());
			fSelectServiceConfigurationPage.setDefaultConfiguration(config);
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
			getConfiguration().setDefaultNameAndDesc();
		}
		if (fUsingWorkingCopy) {
			((IServiceProviderWorkingCopy) getServiceProvider()).save();
		} else {
			IServiceConfiguration config = fSelectServiceConfigurationPage.getServiceConfiguration();
			IServiceProvider provider = config.getServiceProvider(fLaunchService);
			if (provider != null) {
				boolean replace = MessageDialog.openQuestion(getShell(), Messages.RMServicesConfigurationWizard_15,
						NLS.bind(Messages.RMServicesConfigurationWizard_16, provider.getName()));
				if (!replace) {
					return false;
				}
			}
			config.setServiceProvider(fLaunchService, getServiceProvider());
			fModelManager.addConfiguration(config);
		}
		return true;
	}

	/**
	 * @return
	 */
	private int getNumPages() {
		return fWizardPages.size();
	}

	/**
	 * @return
	 */
	private IServiceProvider getServiceProvider() {
		return fServiceProvider;
	}

	/**
	 * @param provider
	 */
	private void setServiceProvider(IServiceProvider provider) {
		fServiceProvider = provider;
		setWizardPages(provider.getDescriptor());
	}

	private void setWizardPages(IServiceProviderDescriptor desc) {
		// If this factory has not been selected before
		// then we must get the additional wizard pages for
		// the factory and cache them.
		IWizardPage[] pages = null;
		if (desc != null) {
			pages = fCachedPages.get(desc.getId());
			if (pages == null) {
				IServiceProviderContributor contributor = ServiceModelUIManager.getInstance().getServiceProviderContributor(desc);
				if (contributor != null) {
					pages = contributor.getWizardPages(this, null);
					for (IWizardPage page : pages) {
						addPage(page);
					}
					fCachedPages.put(desc.getId(), pages);
				}
			}
		}

		fWizardPages.clear();
		if (!fUsingWorkingCopy) {
			fWizardPages.add(fSelectServiceProviderPage);
		}
		if (pages != null) {
			fWizardPages.addAll(Arrays.asList(pages));
		}
		fWizardPages.add(fResourceManagerPage);
		if (!fUsingWorkingCopy) {
			fWizardPages.add(fSelectServiceConfigurationPage);
		}
	}

}
