/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.services.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.dialogs.ServiceConfigurationSelectionDialog;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.ptp.services.ui.widgets.ServiceProviderConfigurationWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * This class implements a project properties page which allows the user to
 * associate service configurations with the project
 * 
 * @author dave
 * 
 */
public class ServiceConfigurationPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {
	
	private class ServiceScrolledComposite extends SharedScrolledComposite {

		public ServiceScrolledComposite(Composite parent, int style) {
			super(parent, style);
		}
		
	}
	
	/**
	 * Class to handle widget selection events
	 * 
	 * @author dave
	 * 
	 */
	private class EventHandler implements SelectionListener {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org
		 * .eclipse.swt.events.SelectionEvent)
		 */
		public void widgetDefaultSelected(SelectionEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse
		 * .swt.events.SelectionEvent)
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source;

			source = e.getSource();
			if (source == serviceConfigurationList) {
				showSelectedConfiguration();
			} else if (source == addButton) {
				addServiceConfiguration();
			} else if (source == removeButton) {
				removeServiceConfiguration();
			} else if (source == serviceModelWidget) {
				serviceModelPane.reflow(true);
			}
		}
	}

	/**
	 * Comparator class used to sort service configurations in ascending order
	 * by name
	 * 
	 * @author dave
	 * 
	 */
	private class ServiceConfigurationComparator implements Comparator<IServiceConfiguration> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(IServiceConfiguration o1, IServiceConfiguration o2) {
			return o1.getName().compareTo(o2.getName());
		}
	}

	private static int BUTTON_WIDTH = 85;

	private Button addButton;
	
	private IServiceConfiguration currentConfig;
	private Vector<IServiceConfiguration> deletedServiceConfigurations;
	private Vector<IServiceConfiguration> addedServiceConfigurations;
	private final EventHandler eventHandler = new EventHandler();
	private Composite propertiesPane;
	private Button removeButton;
	private ServiceConfigurationComparator serviceConfigurationComparator;
	private Table serviceConfigurationList;
	private ServiceScrolledComposite serviceModelPane;
	private ServiceProviderConfigurationWidget serviceModelWidget;

	/**
	 * Create the service configuration properties page
	 */
	public ServiceConfigurationPropertyPage() {
		super();
		serviceConfigurationComparator = new ServiceConfigurationComparator();
	}

	/**
	 * Delete service configurations when Ok or Apply button is pressed
	 * 
	 * @return Status from superclass indicating if Ok processing is to continue
	 */
	public boolean performOk() {
		deleteServiceConfigurations();
		addServiceConfigurations();
		serviceModelWidget.applyChangesToConfiguration();
		try {
			ServiceModelManager.getInstance().saveModelConfiguration();
		} catch (IOException e) {
			ServicesUIPlugin.getDefault().log(e);
		}
		return super.performOk();
	}

	/**
	 * Add selected service configurations to the set of service
	 * configurations known to the service model manager
	 */
	private void addServiceConfigurations() {
		if (addedServiceConfigurations != null)
			for (IServiceConfiguration configuration : addedServiceConfigurations)
				ServiceModelManager.getInstance().addConfiguration(
						getProject(), configuration);
	}

	/**
	 * Add a new service configuration to the list of service configurations
	 * used by this project
	 */
	private void addServiceConfiguration() {
		ServiceConfigurationSelectionDialog dialog;
		int status;
		Set<IServiceConfiguration> configs;

		// Display a dialog containing a list of available service
		// configurations
		try {
			configs = ServiceModelManager.getInstance().getConfigurations(
					getProject());
		} catch (ProjectNotConfiguredException e) {
			configs = new HashSet<IServiceConfiguration>();
		}
		dialog = new ServiceConfigurationSelectionDialog(getShell(), configs);
		status = dialog.open();
		if (status == Window.OK) {
			IServiceConfiguration config;
			TableItem item;

			config = dialog.getSelectedConfiguration();
			if (config != null) {
				item = new TableItem(serviceConfigurationList, 0);
				item.setData(config);
				item.setText(config.getName());
				// ServiceModelManager.getInstance().addConfiguration(
				// getProject(), config);
				if (addedServiceConfigurations == null)
					addedServiceConfigurations = new Vector<IServiceConfiguration>();
				addedServiceConfigurations.add(config);
			}
		}
	}

	/**
	 * Remove selected service configurations from the set of service
	 * configurations known to the service model manager
	 */
	private void deleteServiceConfigurations() {
		if (deletedServiceConfigurations != null) {
			for (IServiceConfiguration config : deletedServiceConfigurations) {
				ServiceModelManager.getInstance().removeConfiguration(
						getProject(), config);
			}
			deletedServiceConfigurations.clear();
		}
	}

	/**
	 * Get the project Object
	 * 
	 * @return The project
	 */
	private IProject getProject() {
		Object element = getElement();
		IProject project = null;
		if (element instanceof IProject) {
			project = (IProject) element;
		} else if (element instanceof IAdaptable) {
			project = (IProject) ((IAdaptable) element)
					.getAdapter(IProject.class);
		}
		return project;

	}

	/**
	 * Get the list of service configuration used by this project, sort by name
	 * and then populate the service configuration list.
	 */
	private void getProjectConfigurations() {
		IServiceConfiguration serviceConfigurations[];

		try {
			serviceConfigurations = ServiceModelManager.getInstance()
					.getConfigurations(getProject()).toArray(new IServiceConfiguration[0]);
			Arrays.sort(serviceConfigurations, serviceConfigurationComparator);
			for (Object config : serviceConfigurations) {
				TableItem item;

				item = new TableItem(serviceConfigurationList, 0);
				item.setData(config);
				item.setText(0, ((IServiceConfiguration) config).getName());
			}
		} catch (ProjectNotConfiguredException e) {
		}
	}

	/**
	 * Remove the selected service configuration from the project
	 */
	private void removeServiceConfiguration() {
		TableItem selection[];
		IServiceConfiguration selectedConfig;

		selection = serviceConfigurationList.getSelection();
		if (selection.length > 0) {
			selectedConfig = (IServiceConfiguration) selection[0].getData();
			if (deletedServiceConfigurations == null) {
				deletedServiceConfigurations = new Vector<IServiceConfiguration>();
			}
			// Selected service model is added to vector to be deleted during Ok
			// or Apply button processing
			deletedServiceConfigurations.add(selectedConfig);
			serviceConfigurationList.remove(serviceConfigurationList
					.getSelectionIndex());
			serviceModelWidget.setServiceConfiguration(null);
		}
	}

	/**
	 * Show the details for the selected service configuration
	 */
	private void showSelectedConfiguration() {
		TableItem selection[];
		IServiceConfiguration selectedConfig;

		selection = serviceConfigurationList.getSelection();
		if (selection.length > 0) {
			selectedConfig = (IServiceConfiguration) selection[0].getData();
			if (selectedConfig != currentConfig) {
				currentConfig = selectedConfig;
				
				Set<String> natures = Collections.emptySet();
				IProject project = (IProject) getElement().getAdapter(IProject.class);
				if(project != null) {
					try {
						natures = new HashSet<String>(Arrays.asList(project.getDescription().getNatureIds()));
					} catch (CoreException e) {
						ServicesUIPlugin.getDefault().log(e);
					}
				}
				serviceModelWidget.setServiceConfiguration(selectedConfig, natures);
				serviceModelWidget.setEnabled(true);
				serviceModelPane.reflow(true);
			}
		} else {
			currentConfig = null;
			serviceModelWidget.setServiceConfiguration(null);
			serviceModelWidget.setEnabled(false);
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		// Create the properties pane, which will contain the service
		// configuration list
		// and the current service configuration
		propertiesPane = new Composite(parent, SWT.NONE);
		GridLayout propertiesPaneLayout = new GridLayout(1, true);
		propertiesPaneLayout.marginHeight = 0;
		propertiesPaneLayout.marginWidth = 0;
		propertiesPane.setLayout(propertiesPaneLayout);

		// Create the pane for the service configuration list and then create
		// the service configuration list
		Composite serviceConfigurationPane = new Composite(propertiesPane, SWT.NONE);
		GridLayout serviceConfigLayout = new GridLayout(2, false);
		serviceConfigurationPane.setLayout(serviceConfigLayout);
		serviceConfigurationPane.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		Label label = new Label(serviceConfigurationPane, SWT.NONE);
		label.setText(Messages.ServiceConfigurationPropertyPage_2);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		data.horizontalSpan = 2;
		label.setLayoutData(data);

		serviceConfigurationList = new Table(serviceConfigurationPane, SWT.SINGLE|SWT.BORDER);
		serviceConfigurationList.setLayoutData(new GridData(GridData.FILL_BOTH));
		serviceConfigurationList.setLinesVisible(false);
		serviceConfigurationList.addSelectionListener(eventHandler);

		// Create the pane and buttons to add and remove service configurations
		Composite buttonPane = new Composite(serviceConfigurationPane, SWT.NONE);
		buttonPane.setLayout(new GridLayout(1, true));
		addButton = new Button(buttonPane, SWT.PUSH);
		addButton.setText(Messages.ServiceConfigurationPropertyPage_0);
		addButton.addSelectionListener(eventHandler);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		data.widthHint = BUTTON_WIDTH;
		addButton.setLayoutData(data);
		removeButton = new Button(buttonPane, SWT.PUSH);
		removeButton.setText(Messages.ServiceConfigurationPropertyPage_1);
		removeButton.addSelectionListener(eventHandler);
		data = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
		data.widthHint = BUTTON_WIDTH;
		removeButton.setLayoutData(data);

		// Create the pane which will contain the current service model
		serviceModelPane = new ServiceScrolledComposite(propertiesPane, SWT.V_SCROLL|SWT.H_SCROLL);
		serviceModelPane.setExpandVertical(true);
		serviceModelPane.setExpandHorizontal(true);
		serviceModelPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite serviceComp = new Composite(serviceModelPane, SWT.NONE);
		serviceComp.setLayout(new GridLayout(1, true));
		
		serviceModelWidget = new ServiceProviderConfigurationWidget(serviceComp, SWT.NONE);
		serviceModelWidget.addSelectionListener(eventHandler);
		serviceModelWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		serviceModelPane.setContent(serviceComp);
		serviceModelPane.setMinSize(serviceComp.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		
		// Fill in the list of service configurations currently used by this
		// project
		getProjectConfigurations();
		return propertiesPane;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// clean up pending deletes
		if (deletedServiceConfigurations != null)
			deletedServiceConfigurations.removeAllElements();
		// remove any pending additions
		if (addedServiceConfigurations != null)
			addedServiceConfigurations.removeAllElements();
		currentConfig = null;
		serviceConfigurationList.removeAll(); // cleanup table
		getProjectConfigurations(); // reload table
		serviceModelWidget.setServiceConfiguration(null);
		super.performDefaults();
		setErrorMessage(null);
	}

}
