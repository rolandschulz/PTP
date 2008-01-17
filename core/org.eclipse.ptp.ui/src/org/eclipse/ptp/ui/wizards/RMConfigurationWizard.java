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

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.ui.PTPUIPlugin;
import org.eclipse.ptp.ui.UIMessage;
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

public class RMConfigurationWizard extends Wizard {

	public final static String EMPTY_STRING = ""; //$NON-NLS-1$
	
	public class NameAndDescPage extends WizardPage {

		private Text descText;
		private Text nameText;

		public NameAndDescPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(UIMessage.getResourceString("ConfigurationWizard.NameDesc")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

			createNameDescControl(composite);

			final boolean textAndDescriptionEnabled = hasFactories;
			nameText.setEnabled(textAndDescriptionEnabled);
			descText.setEnabled(textAndDescriptionEnabled);

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
			useDefaultsButton.setText(UIMessage.getResourceString("ConfigurationWizard.UseDefaultButtonLabel")); //$NON-NLS-1$
			useDefaultsButton.setSelection(useDefaultNameAndDesc);
			GridData buttonData = new GridData();
			buttonData.horizontalSpan = 2;
			useDefaultsButton.setLayoutData(buttonData);
			useDefaultsButton.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
				}

				public void widgetSelected(SelectionEvent e) {
					useDefaultNameAndDesc = useDefaultsButton.getSelection();
					final boolean enabled = hasFactories
							&& !useDefaultNameAndDesc;
					if (useDefaultNameAndDesc) {
						configs[selectedFactory].setDefaultNameAndDesc();
						setNameAndDescription(configs[selectedFactory]);
						setPageComplete(true);
						setErrorMessage(null);
					}
					nameAndDescPage.setEnabled(enabled);
					getContainer().updateButtons();
				}
			});
			
			Label nameLabel = new Label(nameGroup, SWT.NONE);
			nameLabel.setText(UIMessage.getResourceString("ConfigurationWizard.NameLabel")); //$NON-NLS-1$
			nameText = new Text(nameGroup, SWT.BORDER);
			nameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			nameText.setText(""); //$NON-NLS-1$
			nameText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					configs[selectedFactory].setName(nameText.getText());
					if (nameText.getText().equals(EMPTY_STRING)) {
						setPageComplete(false);
						setErrorMessage("Please specify a resource manager name");
					}
				}});

			Label descLabel = new Label(nameGroup, SWT.NONE);
			descLabel.setText(UIMessage.getResourceString("ConfigurationWizard.DescriptionLabel")); //$NON-NLS-1$
			descText = new Text(nameGroup, SWT.BORDER);
			descText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			descText.setText(""); //$NON-NLS-1$
			descText.addModifyListener(new ModifyListener(){
				public void modifyText(ModifyEvent e) {
					configs[selectedFactory].setDescription(descText.getText());
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

	private class SelectFactoryPage extends WizardPage {

		private List factoryList;

		public SelectFactoryPage(String pageName) {
			super(pageName);
			setTitle(pageName);
			setDescription(UIMessage.getResourceString("ConfigurationWizard.SelectFactoryDesc")); //$NON-NLS-1$
		}

		public void createControl(Composite parent) {
	        Composite composite = new Composite(parent, SWT.NULL);
	        composite.setFont(parent.getFont());
	        composite.setLayout(new GridLayout());
	        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
	        
			createRMFactoryChoiceControl(composite);

			if (hasFactories) {
				parent.getDisplay().asyncExec(new Runnable() {

					public void run() {
						factoryList.select(0);
						handleFactorySeclection();
					}
				});
			}

			setControl(composite);
		}

		private void createRMFactoryChoiceControl(Composite container) {
			String[] factoryNames = new String[factories.length];
			for (int i = 0; i < factories.length; ++i) {
				factoryNames[i] = factories[i].getName();
			}

			Label factoryLabel = new Label(container, SWT.NONE);
			factoryLabel.setText(UIMessage.getResourceString("ConfigurationWizard.ResourceManagerTypesLabel")); //$NON-NLS-1$
			factoryList = new List(container, SWT.SINGLE | SWT.BORDER
					| SWT.V_SCROLL);
			factoryList.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			factoryList.setItems(factoryNames);
			factoryList.setEnabled(hasFactories);
			factoryList.addSelectionListener(new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {
					// no-op
				}

				public void widgetSelected(SelectionEvent e) {
					handleFactorySeclection();
				}
			});
		}

		private void handleFactorySeclection() {
			int index = factoryList.getSelectionIndex();
			factorySelected(index);
			setPageComplete(true);
		}
	}

	private RMConfigurationWizardPage[][] cachedPages;

	private final ArrayList<WizardPage> wizardPages = new ArrayList<WizardPage>();

	private IResourceManagerConfiguration[] configs = null;

	private final SelectFactoryPage selectFactoryPage;

	private final IResourceManagerFactory[] factories;

	private final boolean hasFactories;

	private IResourceManagerFactory resourceManagerFactory;

	private boolean useDefaultNameAndDesc;
	
	private final NameAndDescPage nameAndDescPage;

	private int selectedFactory = -1;
	
	private IResourceManagerControl resourceManager;

	/*
	 * Constructor used when creating a new resource manager.
	 */
	public RMConfigurationWizard(
			IResourceManagerFactory[] resourceManagerFactories) {
		setForcePreviousAndNextButtons(true);
		this.factories = resourceManagerFactories;
		this.hasFactories = factories != null && factories.length > 0;
		this.cachedPages = new RMConfigurationWizardPage[factories.length][];
		this.configs = new IResourceManagerConfiguration[factories.length];
		this.selectFactoryPage = new SelectFactoryPage(
				UIMessage.getResourceString("ConfigurationWizard.FirstWizardPageName")); //$NON-NLS-1$
		this.nameAndDescPage = new NameAndDescPage(
				UIMessage.getResourceString("ConfigurationWizard.SecondWizardPageName")); //$NON-NLS-1$
		this.useDefaultNameAndDesc = true;
		this.resourceManager = null;
	}

	/*
	 * Constructor used when editing configuration of an existing resource manager.
	 */
	public RMConfigurationWizard(IResourceManagerFactory resourceManagerFactory,
			IResourceManagerControl resourceManager) {
		this(new IResourceManagerFactory[] { resourceManagerFactory });
		this.configs[0] = resourceManagerFactory.copyConfiguration(resourceManager.getConfiguration());
		this.useDefaultNameAndDesc = false;
		this.resourceManager = resourceManager;
		factorySelected(0);
	}

	public void addPages() {
		if (resourceManager == null) {
			addPage(selectFactoryPage);
		}
		addPage(nameAndDescPage);
		super.addPages();
	}

	public boolean canFinish() {
		if (!hasFactories)
			return false;

		int numPages = getNumPages();
		for (int i = 0; i < numPages; ++i) {
			WizardPage page = (WizardPage) wizardPages.get(i);
			if (!page.isPageComplete())
				return false;
		}

		return true;
	}

	public IResourceManagerConfiguration getConfiguration() {
		return configs[selectedFactory];
	}

	public String getDescription() {
		return nameAndDescPage.descText.getText();
	}

	public String getName() {
		return nameAndDescPage.nameText.getText();
	}

	public IWizardPage getNextPage(IWizardPage page) {
		int numPages = getNumPages();

		int index = wizardPages.indexOf(page);
		if (index == numPages - 1 || index == -1) {
			// last page or page not found
			return null;
		}
		if (index == numPages - 2) {
			// initialize last page
			if (useDefaultNameAndDesc) {
				configs[selectedFactory].setDefaultNameAndDesc();
			}
			nameAndDescPage.setNameAndDescription(configs[selectedFactory]);
			nameAndDescPage.setEnabled(!useDefaultNameAndDesc);
		}
		return (IWizardPage) wizardPages.get(index + 1);
	}

	public IWizardPage getPreviousPage(IWizardPage page) {
        int index = wizardPages.indexOf(page);
        if (index == 0 || index == -1) {
			// first page or page not found
            return null;
		} else {
			return (IWizardPage) wizardPages.get(index - 1);
		}
	}

	public boolean performFinish() {
		final IResourceManagerConfiguration config = configs[selectedFactory];
		if (useDefaultNameAndDesc) {
			config.setDefaultNameAndDesc();
		}
		if (resourceManager != null) {
			resourceManager.setConfiguration(config);
			PTPCorePlugin.getDefault().getModelManager().saveResourceManagers();
		} else {
			IResourceManagerControl rm = resourceManagerFactory.create(config);
			PTPCorePlugin.getDefault().getModelManager().addResourceManager(rm);
		}
		Arrays.fill(configs, null);
		return true;
	}

	private void factorySelected(int selectedFactory) {
		this.selectedFactory = selectedFactory;
		resourceManagerFactory = factories[selectedFactory];
		if (configs[selectedFactory] == null) {
			configs[selectedFactory] = resourceManagerFactory.createConfiguration();
		}

		setWizardPages(selectedFactory);
	}

	private int getNumPages() {
		return wizardPages.size();
	}

	private void setWizardPages(int index) {
		// If this factory has not been selected before
		// then we must get the additional wizard pages for
		// the factory and cache them.
		
		if (cachedPages[index] == null) {
			final PTPUIPlugin uiPlugin = PTPUIPlugin.getDefault();
			final RMConfigurationWizardPageFactory factory = uiPlugin.getRMConfigurationWizardPageFactory(resourceManagerFactory);
			if (factory != null) {
				cachedPages[index] = factory.getPages(this);
			}

			if (cachedPages[index] == null) {
				cachedPages[index] = new RMConfigurationWizardPage[0];
			}
			
			// All wizard pages must be registered with
			// a call to addPage(...)
			for (int i = 0; i < cachedPages[index].length; ++i) {
				addPage(cachedPages[index][i]);
			}
		}

		// add the first and last pages to the selected factory's pages
		
		wizardPages.clear();
		if (resourceManager == null) {
			wizardPages.add(selectFactoryPage);
		}
		wizardPages.addAll(Arrays.asList(cachedPages[index]));
		wizardPages.add(nameAndDescPage);
	}

}
