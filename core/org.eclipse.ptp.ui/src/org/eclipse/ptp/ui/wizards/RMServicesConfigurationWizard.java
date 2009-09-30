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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ITreeSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.ptp.services.ui.widgets.ServiceConfigurationSelectionWidget;
import org.eclipse.ptp.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Text;

public class RMServicesConfigurationWizard extends Wizard implements IRMConfigurationWizard {

	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	
	public class NameAndDescPage extends WizardPage {

		private Text descText;
		private Text nameText;

		public NameAndDescPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.RMServicesConfigurationWizard_2);
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			createNameDescControl(composite);

			nameText.setEnabled(true);
			descText.setEnabled(true);

			setControl(composite);
		}

		private void createNameDescControl(Composite container) {

			Composite nameGroup = new Composite(container, SWT.NONE);
			GridLayout layout = new GridLayout();
			layout.numColumns = 2;
			nameGroup.setLayout(layout);
			nameGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

			final Button useDefaultsButton = new Button(nameGroup,
					SWT.TOGGLE | SWT.CHECK);
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
						fRMConfiguration.setDefaultNameAndDesc();
						setNameAndDescription(fRMConfiguration);
						setPageComplete(true);
						setErrorMessage(null);
					}
					fNameAndDescPage.setEnabled(true);
					getContainer().updateButtons();
				}
			});
			
			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(Messages.RMServicesConfigurationWizard_4);
			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setText(""); //$NON-NLS-1$
			nameText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					fRMConfiguration.setName(nameText.getText());
					if (nameText.getText().equals(EMPTY_STRING)) {
						setPageComplete(false);
						setErrorMessage(Messages.RMServicesConfigurationWizard_5);
					}
				}});

			Label descLabel = new Label(nameGroup, SWT.NONE);
			descLabel.setText(Messages.RMServicesConfigurationWizard_6);
			descText = new Text(nameGroup, SWT.BORDER);
			descText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			descText.setText(""); //$NON-NLS-1$
			descText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					fRMConfiguration.setDescription(descText.getText());
				}});
			
			setPageComplete(true);
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
			 * Locate all service providers that implement IResourceManagerConfiguration
			 */
			IService launchService = ServiceModelManager.getInstance().getService(IServiceConstants.LAUNCH_SERVICE);
			ArrayList<IServiceProviderDescriptor> providerDescs = new ArrayList<IServiceProviderDescriptor>();
			for (IServiceProviderDescriptor desc : launchService.getProvidersByPriority()) {
				IServiceProvider provider = fModelManager.getServiceProvider(desc);
				if (provider != null && provider instanceof IResourceManagerConfiguration) {
					providerDescs.add(desc);
				}
			}
			
			fProviders = providerDescs.toArray(new IServiceProviderDescriptor[0]);
			String[] providerNames = new String[fProviders.length];
			for (int i = 0; i < fProviders.length; ++i) {
				providerNames[i] = fProviders[i].getName();
			}

			Label label = new Label(container, SWT.NONE);
			label.setText(Messages.RMServicesConfigurationWizard_8);
			fServiceProviderList = new List(container, SWT.SINGLE | SWT.BORDER
					| SWT.V_SCROLL);
			fServiceProviderList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			fServiceProviderList.setItems(providerNames);
			fServiceProviderList.setEnabled(fProviders.length > 0);
			fServiceProviderList.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// no-op
				}

				public void widgetSelected(SelectionEvent e) {
					handleProviderSelection();
				}
			});
		}

		private void handleProviderSelection() {
			serviceProviderSelected(fProviders[fServiceProviderList.getSelectionIndex()]);
			setPageComplete(true);
		}
	}

	private class SelectServiceConfigurationPage extends WizardPage {

		private ServiceConfigurationSelectionWidget serviceConfigWidget;

		public SelectServiceConfigurationPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(Messages.RMServicesConfigurationWizard_0);
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
	        serviceConfigWidget = new ServiceConfigurationSelectionWidget(composite, SWT.NONE);
	        GridData data = new GridData(GridData.FILL_BOTH);
	        data.heightHint = 200;
	        serviceConfigWidget.setLayoutData(data);
	        serviceConfigWidget.addSelectionChangedListener(new ISelectionChangedListener() {
				public void selectionChanged(SelectionChangedEvent event) {
					handleSelectionChanged(event);
				}
	        });

			setControl(composite);
			setPageComplete(false);
		}
		
		public void handleSelectionChanged(SelectionChangedEvent event) {
			IServiceConfiguration config = null;
			ISelection selection = event.getSelection();
			if (!selection.isEmpty() && selection instanceof ITreeSelection) {
				ITreeSelection treeSelection = (ITreeSelection)selection;
				TreePath path = treeSelection.getPaths()[0];
				config = (IServiceConfiguration)path.getFirstSegment();
			}
			
			serviceConfigurationSelected(config);
			setPageComplete(config != null);
		}
	}
	
	private final ArrayList<IWizardPage> fWizardPages = new ArrayList<IWizardPage>();
	private final SelectServiceProviderPage fSelectServiceProviderPage;
	private final SelectServiceConfigurationPage fSelectServiceConfigurationPage;
	private final NameAndDescPage fNameAndDescPage;
	private final IService fLaunchService;
	private final IServiceModelManager fModelManager = ServiceModelManager.getInstance();

	private Map<String, IWizardPage[]> fCachedPages = new HashMap<String, IWizardPage[]>();
	private boolean fUseDefaultNameAndDesc;
	private IResourceManagerControl fResourceManager = null;
	private IServiceConfiguration fServiceConfiguration = null; 
	private IServiceProvider fServiceProvider = null;
	private IResourceManagerConfiguration fRMConfiguration = null;

	/*
	 * Constructor used when creating a new resource manager.
	 */
	public RMServicesConfigurationWizard() {
		setForcePreviousAndNextButtons(true);
		fSelectServiceConfigurationPage = new SelectServiceConfigurationPage(
				Messages.RMServicesConfigurationWizard_1);
		fSelectServiceProviderPage = new SelectServiceProviderPage(
				Messages.RMServicesConfigurationWizard_9);
		fNameAndDescPage = new NameAndDescPage(
				Messages.RMServicesConfigurationWizard_10);
		fUseDefaultNameAndDesc = true;
		fLaunchService = fModelManager.getService(IServiceConstants.LAUNCH_SERVICE);
		setWizardPages(null);
	}

	/*
	 * Constructor used when editing configuration of an existing resource manager.
	 */
	public RMServicesConfigurationWizard(IResourceManagerControl resourceManager) {
		this();
		fRMConfiguration = (IResourceManagerConfiguration)resourceManager.getConfiguration().clone();
		fServiceProvider = (IServiceProvider)fRMConfiguration;
		fUseDefaultNameAndDesc = false;
		fResourceManager = resourceManager;
		for (IServiceConfiguration config : fModelManager.getConfigurations()) {
			IServiceProvider provider = config.getServiceProvider(fLaunchService);
			if (provider.getId().equals(fServiceProvider.getId())) {
				fServiceConfiguration = config;
				break;
			}
		}
		setWizardPages(fServiceProvider.getDescriptor());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#addPages()
	 */
	public void addPages() {
		if (fResourceManager == null) {
			addPage(fSelectServiceConfigurationPage);
			addPage(fSelectServiceProviderPage);
		}
		addPage(fNameAndDescPage);
		super.addPages();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#canFinish()
	 */
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

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.ui.wizards.IRMConfigurationWizard#getConfiguration()
	 */
	public IResourceManagerConfiguration getConfiguration() {
		return fRMConfiguration;
	}

	public String getDescription() {
		return fNameAndDescPage.descText.getText();
	}

	public String getName() {
		return fNameAndDescPage.nameText.getText();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getNextPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getNextPage(IWizardPage page) {
		int numPages = getNumPages();

		int index = fWizardPages.indexOf(page);
		if (index == numPages - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		if (index == numPages - 2) {
			// initialize last page
			if (fUseDefaultNameAndDesc) {
				fRMConfiguration.setDefaultNameAndDesc();
			}
			fNameAndDescPage.setNameAndDescription(fRMConfiguration);
			fNameAndDescPage.setEnabled(!fUseDefaultNameAndDesc);
		}
		return (IWizardPage) fWizardPages.get(index + 1);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#getPreviousPage(org.eclipse.jface.wizard.IWizardPage)
	 */
	public IWizardPage getPreviousPage(IWizardPage page) {
        int index = fWizardPages.indexOf(page);
        if (index == 0 || index == -1) {
			// first page or page not found
            return null;
		} else {
			return (IWizardPage) fWizardPages.get(index - 1);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.wizard.Wizard#performFinish()
	 */
	public boolean performFinish() {
		if (fUseDefaultNameAndDesc) {
			fRMConfiguration.setDefaultNameAndDesc();
		}
		fServiceConfiguration.setServiceProvider(fLaunchService, fServiceProvider);
		return true;
	}

	private void serviceProviderSelected(IServiceProviderDescriptor desc) {
		fServiceProvider = fModelManager.getServiceProvider(desc);
		/*
		 * We know the following cast is safe, because only service providers that
		 * implement IResourceManagerConfiguration can be selected
		 */
		fRMConfiguration = (IResourceManagerConfiguration)fServiceProvider;
		setWizardPages(desc);
	}
	
	private void serviceConfigurationSelected(IServiceConfiguration config) {
		fServiceConfiguration = config;
	}

	private int getNumPages() {
		return fWizardPages.size();
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
		if (fResourceManager == null) {
			fWizardPages.add(fSelectServiceConfigurationPage);
			fWizardPages.add(fSelectServiceProviderPage);
		}
		if (pages != null) {
			fWizardPages.addAll(Arrays.asList(pages));
		}
		fWizardPages.add(fNameAndDescPage);
	}

}
