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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.window.Window;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.dialogs.ServiceConfigurationSelectionDialog;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.ptp.services.ui.wizards.ServiceModelWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * This class implements a project properties page which allows the user to
 * associate service configurations with the project
 * 
 * @author dave
 * 
 */
public class ServiceConfigurationPropertyPage extends PropertyPage implements
		IWorkbenchPropertyPage {
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
	private class ServiceConfigurationComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			return ((IServiceConfiguration) o1).getName().compareTo(
					((IServiceConfiguration) o2).getName());
		}
	}
	private Composite widgetPane;
	private Composite serviceModelPane;
	private Composite propertiesPane;
	private Table serviceConfigurationList;
	private Button addButton;
	private Button removeButton;
	private EventHandler eventHandler;
	private IServiceConfiguration currentConfig;

	private ServiceConfigurationComparator serviceConfigurationComparator;

	private ServiceModelWidget serviceModelWidget;

	/**
	 * Create the service configuration properties page
	 */
	public ServiceConfigurationPropertyPage() {
		super();
		serviceConfigurationComparator = new ServiceConfigurationComparator();
	}

	/**
	 * Add a new service configuration to the list of service configurations
	 * used by this project
	 */
	private void addServiceConfiguration() {
		ServiceConfigurationSelectionDialog dialog;
		int status;

		// Display a dialog containing a list of available service
		// configurations
		dialog = new ServiceConfigurationSelectionDialog(getShell(),
				ServiceModelManager.getInstance().getConfigurations(
						getProject()));
		status = dialog.open();
		if (status == Window.OK) {
			IServiceConfiguration config;
			TableItem item;

			config = dialog.getSelectedConfiguration();
			if (config != null) {
				item = new TableItem(serviceConfigurationList, 0);
				item.setData(config);
				item.setText(config.getName());
				ServiceModelManager.getInstance().addConfiguration(
						getProject(), config);
			}
		}
	}

	@Override
	protected Control createContents(Composite parent) {
		GridLayout parentPaneLayout;
		GridLayout propertiesPaneLayout;
		GridLayout serviceConfigLayout;
		FillLayout fillLayout;
		RowLayout buttonLayout;
		GridData layoutData;
		Composite serviceConfigurationPane;
		Composite buttonPane;
		Control serviceModelControl;

		eventHandler = new EventHandler();

		fillLayout = new FillLayout();
		layoutData = new GridData(SWT.LEFT, SWT.FILL, false, true);

		// Create a top level composite for the widgets in this panel
		parentPaneLayout = new GridLayout(1, true);
		parent.setLayout(parentPaneLayout);

		// Create the properties pane, which will contain the service
		// configuration list
		// and the current service configuration
		propertiesPane = new Composite(parent, SWT.NONE);
		propertiesPaneLayout = new GridLayout(2, true);
		propertiesPaneLayout.makeColumnsEqualWidth = false;
		propertiesPane.setLayout(propertiesPaneLayout);

		// Create the pane for the service configuration list and then create
		// the service configuration list
		serviceConfigurationPane = new Composite(propertiesPane, SWT.NONE);
		serviceConfigLayout = new GridLayout(1, true);
		serviceConfigurationPane.setLayout(serviceConfigLayout);
		serviceConfigurationPane.setLayoutData(layoutData);

		serviceConfigurationList = new Table(serviceConfigurationPane,
				SWT.SINGLE);
		serviceConfigurationList.setLayoutData(layoutData);
		serviceConfigurationList.setLinesVisible(true);
		serviceConfigurationList.addSelectionListener(eventHandler);

		// Create the pane which will contain the current service model
		serviceModelPane = new Composite(propertiesPane, SWT.NONE);
		;
		serviceModelPane.setLayout(fillLayout);
		serviceModelWidget = new ServiceModelWidget();
		serviceModelControl = serviceModelWidget
				.createContents(serviceModelPane);

		// Create the pane and buttons to add and remove service configurations
		buttonPane = new Composite(serviceConfigurationPane, SWT.NONE);
		buttonLayout = new RowLayout();
		buttonLayout.fill = true;
		buttonLayout.center = false;
		buttonLayout.justify = true;
		buttonPane.setLayout(buttonLayout);
		addButton = new Button(buttonPane, SWT.PUSH);
		addButton.setText(Messages.ServiceConfigurationPropertyPage_0);
		addButton.addSelectionListener(eventHandler);
		removeButton = new Button(buttonPane, SWT.PUSH);
		removeButton.setText(Messages.ServiceConfigurationPropertyPage_1);
		removeButton.addSelectionListener(eventHandler);

		widgetPane = propertiesPane;
		// Fill in the list of service configurations currently used by this
		// project
		getProjectConfigurations();
		return propertiesPane;
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
		Object serviceConfigurations[];

		try {
			serviceConfigurations = ServiceModelManager.getInstance()
					.getConfigurations(getProject()).toArray();
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
			ServiceModelManager.getInstance().removeConfiguration(getProject(),
					selectedConfig);
			serviceConfigurationList.remove(serviceConfigurationList
					.getSelectionIndex());
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
				serviceModelWidget.setServiceConfiguration(selectedConfig);
			}
		}
	}
}
