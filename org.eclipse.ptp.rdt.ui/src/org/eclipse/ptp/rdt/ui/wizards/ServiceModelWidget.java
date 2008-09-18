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

package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.services.ui.IServiceProviderConfiguration;
import org.eclipse.ptp.rdt.ui.messages.Messages;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

public class ServiceModelWidget{
	
	protected static final String PROVIDER_KEY = "provider-id"; //$NON-NLS-1$
	protected static final String SERVICE_KEY = "service-id"; //$NON-NLS-1$
	
	protected Map<String, String> fServiceIDToSelectedProviderID;
	protected Map<String, IServiceProvider> fProviderIDToProviderMap;

	protected Table fTable;
	protected Button fConfigureButton;
	
	public ServiceModelWidget() {
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
	}
	
	public Control createContents(Composite parent) {
		Composite canvas = new Composite(parent, SWT.NONE);
		GridLayout canvasLayout = new GridLayout(2, false);
		canvas.setLayout(canvasLayout);
		
		Composite tableParent = new Composite(canvas, SWT.NONE);
		tableParent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		
		fTable = new Table (tableParent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION | SWT.H_SCROLL);
		fTable.setLinesVisible (true);
		fTable.setHeaderVisible (true);
		
		TableColumnLayout layout = new TableColumnLayout();
		// create the columns and headers... note fourth column holds "Configure..." buttons and hence has no title.
		String[] titles = {Messages.getString("ServiceModelWidget.0"), Messages.getString("ServiceModelWidget.1"), Messages.getString("ServiceModelWidget.3")}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
			case 2: // configuration string... typically long
				width = 250;
				break;

			}
			
			layout.setColumnData(column, new ColumnWeightData(1, width, true));
			

		}
		tableParent.setLayout(layout);
		fTable.setLayout(new FillLayout());
		
		createTableContent(null);
		
		fTable.setVisible(true);
		
		final TableEditor editor = new TableEditor(fTable);
		editor.horizontalAlignment = SWT.BEGINNING;
		editor.grabHorizontal = true;
		fTable.addListener(SWT.MouseDown, new Listener() {
			public void handleEvent(Event event) {
				int selectionIndex = fTable.getSelectionIndex();
				if (selectionIndex == -1) {
					fConfigureButton.setEnabled(false);
					return;
				}
				fConfigureButton.setEnabled(true);
				final TableItem item = fTable.getItem(selectionIndex);
				IService service = (IService) item.getData(SERVICE_KEY);
				IServiceProviderDescriptor provider = (IServiceProviderDescriptor) item.getData(PROVIDER_KEY);
				final CCombo combo = new CCombo(fTable, SWT.READ_ONLY);
				
				// populate with list of providers
				Set<IServiceProviderDescriptor> providers = service.getProviders();
				Iterator<IServiceProviderDescriptor> providerIterator = providers.iterator();
				
				int index = 0;
				final List<IServiceProviderDescriptor> providerIds = new LinkedList<IServiceProviderDescriptor>();
				while(providerIterator.hasNext()) {
					IServiceProviderDescriptor descriptor = providerIterator.next();
					combo.add(descriptor.getName(), index);
					providerIds.add(descriptor);
					if (descriptor.equals(provider)) {
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
							item.setText(1, descriptor.getName());
							IService service = (IService) item.getData(SERVICE_KEY);
							item.setData(PROVIDER_KEY, descriptor);
							
							IServiceProvider serviceProvider = fProviderIDToProviderMap.get(descriptor.getId());
							
							if (serviceProvider == null) {
								ServiceModelManager manager = ServiceModelManager.getInstance();
								serviceProvider = manager.getServiceProvider(descriptor);
							}
							
							// column 2 holds the configuration string of the provider's current configuration 
							String configString = serviceProvider.getConfigurationString();
							if (configString == null) {
								configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
							}
							item.setText(2, configString);
							
							fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
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
		
		fConfigureButton = new Button(canvas, SWT.PUSH);
		fConfigureButton.setEnabled(false);
		fConfigureButton.setText(Messages.getString("ServiceModelWidget.2")); //$NON-NLS-1$
		fConfigureButton.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
		Listener configureListener = getConfigureListener();
		fConfigureButton.addListener(SWT.Selection, configureListener);
		return canvas;
	}
	
	public class ConfigureListener implements Listener {
		public void handleEvent(Event event) {
			// launch the configuration UI for the service provider
			TableItem[] selection = fTable.getSelection();
			if (selection.length == 0) {
				return;
			}
			TableItem item = selection[0];
			ServiceModelManager manager = ServiceModelManager.getInstance();
			IServiceProviderDescriptor descriptor = (IServiceProviderDescriptor) item.getData(PROVIDER_KEY);
			IServiceProvider serviceProvider = manager.getServiceProvider(descriptor);
			fProviderIDToProviderMap.put(descriptor.getId(), serviceProvider);

			IServiceProviderConfiguration configUI = manager.getServiceProviderConfigurationUI(serviceProvider);
			configUI.configureServiceProvider(serviceProvider, fConfigureButton.getShell());
			
			String configString = serviceProvider.getConfigurationString();
			// column 2 holds the configuration string of the provider's current configuration 
			if (configString == null) {
				configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
			}
			item.setText(2, configString);
		}
	}
	
	//sub class may override to change behaviour
	protected Listener getConfigureListener() {
		return new ConfigureListener();		
	}
	
	/**
	 * Generate the services, providers and provider configuration available for
	 * the given project in the table
	 * 
	 * Sub-classes may override its behaviour
	 * @param project
	 */
	protected void createTableContent(IProject project) {
		Set<IService> allApplicableServices = getContributedServices(project);
		
		Iterator<IService> iterator = allApplicableServices.iterator();

		// get the contributed services... we need one row for each
		while(iterator.hasNext()) {
			final IService service = iterator.next();			
			
			TableItem item = new TableItem (fTable, SWT.NONE);

			// column 0 lists the name of the service
			item.setText (0, service.getName());
			item.setData(SERVICE_KEY, service);
			
			// column 1 holds a dropdown with a list of providers
			IServiceProviderDescriptor descriptor = service.getProviders().iterator().next();
			item.setText(1, descriptor.getName());
			item.setData(PROVIDER_KEY, descriptor);
			
			ServiceModelManager manager = ServiceModelManager.getInstance();
			IServiceProvider serviceProvider = manager.getServiceProvider(descriptor);
			
			String configString = serviceProvider.getConfigurationString();
			// column 2 holds the configuration string of the provider's current configuration 
			if (configString == null) {
				configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
			}
			item.setText(2, configString);
			
			fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
		}
	}
	
	/**
	 * Find available remote services and service providers for a given project
	 * 
	 * If project is null, the C and C++ natures are used to determine which services
	 * are available
	 */
	protected Set<IService> getContributedServices(IProject project) {		
		ServiceModelManager modelManager = ServiceModelManager.getInstance();
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
		else {		
			Set<IService> cppServices = modelManager.getServices(CCProjectNature.CC_NATURE_ID);
			Set<IService> cServices = modelManager.getServices(CProjectNature.C_NATURE_ID);
			
			allApplicableServices.addAll(cppServices);
			allApplicableServices.addAll(cServices);
		}
		return allApplicableServices;
	}

	public Map<String, String> getServiceIDToSelectedProviderID() {
		return fServiceIDToSelectedProviderID;
	}

	public Table getTable() {
		return fTable;
	}

	public void setTable(Table table) {
		fTable = table;
	}

	public void setServiceIDToSelectedProviderID(
			Map<String, String> serviceIDToSelectedProviderID) {
		fServiceIDToSelectedProviderID = serviceIDToSelectedProviderID;
	}

	public Map<String, IServiceProvider> getProviderIDToProviderMap() {
		return fProviderIDToProviderMap;
	}

	public void setProviderIDToProviderMap(
			Map<String, IServiceProvider> providerIDToProviderMap) {
		fProviderIDToProviderMap = providerIDToProviderMap;
	}
}
