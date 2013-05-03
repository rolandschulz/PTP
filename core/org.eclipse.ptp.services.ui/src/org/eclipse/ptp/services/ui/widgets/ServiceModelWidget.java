/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.services.ui.widgets;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.ptp.services.ui.ServiceModelUIManager;
import org.eclipse.ptp.services.ui.dialogs.ServiceSelectionDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

/**
 * Old style configuration widget. Displays a table with three columns, service,
 * provider, and configured status. Uses add/remove/configure buttons. New services
 * are added using the {@link #ServiceSelectionDialog}. Services are configured using
 * wizard pages supplied by the providerContributor extension point.
 * 
 * NOT CURRENTLY USED AND MAY BE DEPRECATED
 * 
 */
public class ServiceModelWidget {
	
	public class AddListener implements Listener {
		public void handleEvent(Event event) {
			Set<IService> displaySet = new HashSet<IService>();
			Set<IService> configServices = getServiceConfiguration().getServices();
			/*
			 * Calculate the set of services to display. Only include services that 
			 * are not in the configuration and that have one or more providers.
			 */
			for (IService service : ServiceModelManager.getInstance().getServices()) {
				if (!configServices.contains(service) && service.getProviders().size() > 0) {
					displaySet.add(service);
				}
			}
			
			ServiceSelectionDialog dialog = new ServiceSelectionDialog(getShell(), displaySet.toArray(new IService[0]));
			if (dialog.open() == Dialog.OK) {
				IService[] selectedServices = dialog.getSelectedServices();
				for (IService service : selectedServices) {
					SortedSet<IServiceProviderDescriptor> providers = service.getProvidersByPriority();
					IServiceProvider provider = ServiceModelManager.getInstance().getServiceProvider(providers.iterator().next());
					addTableRow(service, provider);
					getServiceConfiguration().setServiceProvider(service, provider);
				}
				updateAddRemoveButtons();
			}
		}
	}
	
	public class ConfigureListener implements Listener {
		public void handleEvent(Event event) {
			// launch the configuration UI for the service provider
			TableItem[] selection = fTable.getSelection();
			if (selection.length == 0) {
				return;
			}
			TableItem item = selection[0];
			IServiceProvider provider = (IServiceProvider) item.getData(PROVIDER_KEY);

			IServiceProviderContributor config = ServiceModelUIManager.getInstance().getServiceProviderContributor(provider);
			IWizard wizard = config.getWizard(provider, null);
			WizardDialog dialog = new WizardDialog(getShell(), wizard);
			dialog.open();
			
			String configString = provider.getConfigurationString();
			// column 2 holds the configuration string of the provider's current configuration 
			if (configString == null) {
				configString = Messages.ServiceModelWidget_4;
			}
			item.setText(2, configString);
			
			// allow container page to check if configurations are set
			if (fConfigChangeListener != null) {
				fConfigChangeListener.handleEvent(null);
			}
		}
	}
	
	public class RemoveListener implements Listener {
		public void handleEvent(Event event) {
			TableItem[] items = fTable.getSelection();
			if (items.length == 0) {
				return;
			}
			for (TableItem item : items) {
				IService service = (IService) item.getData(SERVICE_KEY);
				if (service != null) {
					getServiceConfiguration().disable(service);
				}
				fTable.remove(fTable.indexOf(item));
			}
			updateAddRemoveButtons();
		}
	}
	
	protected static final String PROVIDER_KEY = "provider-id"; //$NON-NLS-1$
	protected static final String SERVICE_KEY = "service-id"; //$NON-NLS-1$

	protected IServiceConfiguration fServiceConfiguration;
	protected Map<String, String> fServiceIDToSelectedProviderID;
	protected Map<String, IServiceProvider> fProviderIDToProviderMap;
	protected Table fTable;
	protected Button fConfigureButton;
	protected Button fAddButton;
	protected Button fRemoveButton;
	
	protected Listener fConfigChangeListener = null;
	
	private Shell fShell;
	
	public ServiceModelWidget() {
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
	}
	
	public ServiceModelWidget(IServiceConfiguration serviceConfiguration) {
		fServiceConfiguration = serviceConfiguration;
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
	}
	
	private void addTableRow(IService service, IServiceProvider provider) {
		TableItem item = new TableItem (fTable, SWT.NONE);

		// column 0 lists the name of the service
		item.setText (0, service.getName());
		item.setData(SERVICE_KEY, service);
		
		// column 1 holds a dropdown with a list of providers
		// default entry is the first provider if there is one		
		item.setText(1, provider.getName());
		item.setData(PROVIDER_KEY, provider);
		
		// column 2 holds the status string
		item.setText(2, provider.getConfigurationString());
		
		fServiceIDToSelectedProviderID.put(service.getId(), provider.getId());

		// allow container page to check if configurations are set
		if (fConfigChangeListener != null) {
			fConfigChangeListener.handleEvent(null);
		}
	}
	
	public Control createContents(Composite parent) {
		fShell = parent.getShell();
		
		Composite canvas = new Composite(parent, SWT.NONE);
		GridLayout canvasLayout = new GridLayout(2, false);
		canvas.setLayout(canvasLayout);
		
		Composite tableParent = new Composite(canvas, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fTable = new Table(tableParent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL);
		fTable.setLinesVisible (true);
		fTable.setHeaderVisible (true);
		
		TableColumnLayout layout = new TableColumnLayout();
		// create the columns and headers... note fourth column holds "Configure..." buttons and hence has no title.
		String[] titles = {Messages.ServiceModelWidget_0, Messages.ServiceModelWidget_1, Messages.ServiceModelWidget_3};
		for (int i=0; i<titles.length; i++) {
			TableColumn column = new TableColumn (fTable, SWT.NONE);
			column.setText (titles [i]);
			int width = ColumnWeightData.MINIMUM_WIDTH;
			
			// set the column widths
			switch (i) {
			case 0: // Service name... usually short
				width = 100;
				break;

			case 1: // provider name... typically long
				width = 250;
				break;
				
			case 2: // configuration status... typically short
				width = 100;
				break;

			}
			
			layout.setColumnData(column, new ColumnWeightData(1, width, true));
			

		}
		tableParent.setLayout(layout);
		fTable.setLayout(new FillLayout());
		
		createTableContent();
		
		fTable.setVisible(true);
		
		final TableEditor editor = new TableEditor(fTable);
		editor.horizontalAlignment = SWT.BEGINNING;
		editor.grabHorizontal = true;
		fTable.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event event) {
				int selectionIndex = fTable.getSelectionIndex();
				if (selectionIndex == -1) {
					fConfigureButton.setEnabled(false);
					return;
				}
				final TableItem item = fTable.getItem(selectionIndex);
				IService service = (IService) item.getData(SERVICE_KEY);
				IServiceProvider provider = (IServiceProvider) item.getData(PROVIDER_KEY);
				
				updateConfigureButton(provider);
				
				final CCombo combo = new CCombo(fTable, SWT.READ_ONLY);
				
				// populate with list of providers
				int index = 0;
				final List<IServiceProviderDescriptor> providerIds = new LinkedList<IServiceProviderDescriptor>();
				SortedSet<IServiceProviderDescriptor> providers = service.getProvidersByPriority();
				for (IServiceProviderDescriptor descriptor : providers) {
					combo.add(descriptor.getName(), index);
					providerIds.add(descriptor);
					if (descriptor.getId().equals(provider.getId())) {
						combo.select(index);
					}
					++index;
				}
				
				combo.setFocus();
				Listener listener = new Listener() {
					public void handleEvent(Event event) {
						switch (event.type) {
						case SWT.FocusOut:
							combo.dispose();
							break;
						case SWT.Selection:
							int selection = combo.getSelectionIndex();
							if (selection == -1) {
								return;
							}
							IServiceProviderDescriptor descriptor = providerIds.get(selection);
							IServiceProvider provider = getServiceProvider(descriptor);
							
							/*
							 * Set the provider name in the second field of the row
							 */
							item.setText(1, provider.getName());
							item.setData(PROVIDER_KEY, provider);

							updateConfigureButton(descriptor);							
							
							/*
							 * Set the configured status in the third field
							 */
							item.setText(2, provider.getConfigurationString());

							/*
							 * Update the configuration
							 */
							IService service = (IService) item.getData(SERVICE_KEY);
							fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
							getServiceConfiguration().setServiceProvider(service, provider);
							
							// allow container page to check if configurations are set
							if (fConfigChangeListener != null) {
								fConfigChangeListener.handleEvent(null);
							}
							
							combo.dispose();
							break;
						}
					}
				};
				combo.addListener(SWT.FocusOut, listener);
				combo.addListener(SWT.Selection, listener);

				editor.setEditor(combo, item, 1);
			}
		});
		
		Composite buttonParent = new Composite(canvas, SWT.NONE);
		GridLayout buttonLayout = new GridLayout(1, true);
		buttonParent.setLayout(buttonLayout);
		buttonParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fConfigureButton = new Button(buttonParent, SWT.PUSH);
		fConfigureButton.setEnabled(false);
		fConfigureButton.setText(Messages.ServiceModelWidget_2);
		fConfigureButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Listener configureListener = getConfigureListener();
		fConfigureButton.addListener(SWT.Selection, configureListener);
		
		fAddButton = new Button(buttonParent, SWT.PUSH);
		fAddButton.setEnabled(true);
		fAddButton.setText(Messages.ServiceModelWidget_6);
		fAddButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Listener addListener = getAddListener();
		fAddButton.addListener(SWT.Selection, addListener);
		
		fRemoveButton = new Button(buttonParent, SWT.PUSH);
		fRemoveButton.setEnabled(false);
		fRemoveButton.setText(Messages.ServiceModelWidget_7);
		fRemoveButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		Listener removeListener = getRemoveListener();
		fRemoveButton.addListener(SWT.Selection, removeListener);

		updateAddRemoveButtons();
		
		return canvas;
	}
	
	/**
	 * Generate the services, providers and provider configuration available for
	 * the given configuration in the table
	 * 
	 * Sub-classes may override its behaviour
	 * @param project
	 */
	protected void createTableContent() {
		fTable.removeAll();
		
		if (getServiceConfiguration() != null) {
			for (IService service : getServiceConfiguration().getServices()) {
				addTableRow(service, getServiceConfiguration().getServiceProvider(service));
			}
		}
	}
	
	//sub class may override to change behaviour
	protected Listener getAddListener() {
		return new AddListener();		
	}
	
	/**
	 * @return the configuration change listener
	 */
	public Listener getConfigChangeListener() {
		return fConfigChangeListener;
	}
	
	//sub class may override to change behaviour
	protected Listener getConfigureListener() {
		return new ConfigureListener();		
	}
	
	/**
	 * Find available remote services and service providers for a given project
	 */
	protected Set<IService> getContributedServices(IProject project) {		
		IServiceModelManager modelManager = ServiceModelManager.getInstance();
		Set<IService> allApplicableServices = new LinkedHashSet<IService>();
		
		if (project != null) {
		
			String[] natureIds = new String[] {};			
			try {
				//get the project natures of the project
				natureIds = project.getDescription().getNatureIds();			
			} catch (CoreException e) {
				e.printStackTrace();
			}		
	
			for (int i = 0; i < natureIds.length; i++) {
				String natureId = natureIds[i];
				Set<IService> services = modelManager.getServices(natureId);
				if (services != null)
					allApplicableServices.addAll(services);
			}
		}
		return allApplicableServices;
	}
	
	public Map<String, IServiceProvider> getProviderIDToProviderMap() {
		return fProviderIDToProviderMap;
	}
	
	//sub class may override to change behaviour
	protected Listener getRemoveListener() {
		return new RemoveListener();		
	}

	/**
	 * Get the service configuration for this widget
	 * 
	 * @return service configuration
	 */
	public IServiceConfiguration getServiceConfiguration() {
		return fServiceConfiguration;
	}

	public Map<String, String> getServiceIDToSelectedProviderID() {
		return fServiceIDToSelectedProviderID;
	}

	/**
	 * Get a the service provider for the descriptor. Keeps a cache of service providers.
	 * 
	 * @param descriptor descriptor for the service provider
	 * @return service provider
	 */
	private IServiceProvider getServiceProvider(IServiceProviderDescriptor descriptor) {
		IServiceProvider serviceProvider = getProviderIDToProviderMap().get(descriptor.getId());
		
		if (serviceProvider == null) {
			serviceProvider = ServiceModelManager.getInstance().getServiceProvider(descriptor);
			getProviderIDToProviderMap().put(descriptor.getId(), serviceProvider);
		}
		
		return serviceProvider;
	}

	private Shell getShell() {
		return fShell;
	}

	public Table getTable() {
		return fTable;
	}
	
	/**
	 * Sub-class may override behaviour
	 * @return true if all available services have been configured
	 */
	public boolean isConfigured() {
		return isConfigured(null, fServiceIDToSelectedProviderID, getProviderIDToProviderMap());
	}

	/**
	 * Determine if all service providers have been configured
	 * @param project
	 * @param serviceIDToSelectedProviderID
	 * @param providerIDToProviderMap
	 * @return true if all service providers have been configured
	 */
	protected boolean isConfigured(IProject project, Map<String, String> serviceIDToSelectedProviderID, Map<String, IServiceProvider> providerIDToProviderMap) {
		Set<IService> allApplicableServices = getContributedServices(project);
		Iterator<IService> iterator = allApplicableServices.iterator();
		boolean configured = true;
		while (iterator.hasNext()) {
			String providerID = serviceIDToSelectedProviderID.get(iterator.next().getId());
			if (providerID == null)
				return false;
			else {
				IServiceProvider provider = providerIDToProviderMap.get(providerID);
				if (provider == null)
					return false;
				else
					configured = configured && provider.isConfigured();
			}
		}
		return configured;
	}
	
	/**
	 * Listens for changes in service provider configuration
	 * @param configChangeListener the configuration change listener to set
	 */
	public void setConfigChangeListener(Listener configChangeListener) {
		fConfigChangeListener = configChangeListener;
	}
	
	public void setProviderIDToProviderMap(
			Map<String, IServiceProvider> providerIDToProviderMap) {
		fProviderIDToProviderMap = providerIDToProviderMap;
	}
	
	/**
	 * Set the service configuration for this widget
	 * 
	 * @param service configuration
	 */
	public void setServiceConfiguration(IServiceConfiguration config) {
		fServiceConfiguration = config;
		if (config != null) {
			createTableContent();
		}
	}
	
	public void setServiceIDToSelectedProviderID(
			Map<String, String> serviceIDToSelectedProviderID) {
		fServiceIDToSelectedProviderID = serviceIDToSelectedProviderID;
	}

	public void setTable(Table table) {
		fTable = table;
	}
		
	protected void updateAddRemoveButtons() {
		Set<IService> services = ServiceModelManager.getInstance().getServices();
		fAddButton.setEnabled(services.size() > fTable.getItemCount());
		fRemoveButton.setEnabled(fTable.getItemCount() > 0);
	}
	
	/**
	 * Enable/disable the configure button in this widget based on the service provider descriptor selected
	 * @param enabled
	 */
	protected void updateConfigureButton(IServiceProviderDescriptor descriptor) {
		IServiceProviderContributor config = ServiceModelUIManager.getInstance().getServiceProviderContributor(descriptor);
		fConfigureButton.setEnabled(config != null);
	}
}
