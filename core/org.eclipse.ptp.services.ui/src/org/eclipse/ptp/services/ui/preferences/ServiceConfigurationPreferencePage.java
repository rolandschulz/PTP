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
package org.eclipse.ptp.services.ui.preferences;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Vector;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.ptp.services.ui.wizards.ServiceConfigurationWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

/**
 * This class implements a preference page which can be used to view a list of
 * service configurations, to create new service configurations or to delete
 * existing service configurations. This page also displays a list of projects
 * using a service configuration.
 * 
 * @author dave
 * 
 */
public class ServiceConfigurationPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {
	/**
	 * This class implements the ICellModifier required in order to make the
	 * service configuration table editable.
	 * 
	 * @author dave
	 * 
	 */
	private class ConfigCellModifier implements ICellModifier {

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICellModifier#canModify(java.lang.Object,
		 * java.lang.String)
		 */
		public boolean canModify(Object element, String property) {
			// The table is a single column table which is always editable.
			return true;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.jface.viewers.ICellModifier#getValue(java.lang.Object,
		 * java.lang.String)
		 */
		public Object getValue(Object element, String property) {
			// Return the existing name of the service configuration. This way,
			// If the user is just clicking on service configuration names, then
			// we don't clobber existing names.
			return ((IServiceConfiguration) element).getName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.jface.viewers.ICellModifier#modify(java.lang.Object,
		 * java.lang.String, java.lang.Object)
		 */
		public void modify(Object element, String property, Object value) {
			IServiceConfiguration config;
			TableItem item;
			// Update the service configuration name with the selected value
			item = (TableItem) element;
			config = (IServiceConfiguration) item.getData();
			config.setName((String) value);
			item.setText((String) value);
		}
	}

	/**
	 * Handle widget selection events for this page
	 * 
	 * @author dave
	 * 
	 */
	private class EventHandler implements SelectionListener {

		public void widgetDefaultSelected(SelectionEvent e) {
		}

		/**
		 * Handle selection events for widgets in this page
		 * 
		 * @param e
		 *            The selection event to be handled
		 */
		public void widgetSelected(SelectionEvent e) {
			Object source;

			source = e.getSource();
			if (source == addButton) {
				addServiceConfiguration();
			} else if (source == editButton) {
				editServiceConfiguration();
			} else if (source == removeButton) {
				removeServiceConfiguration();
			} else if (source == serviceConfigurationTable) {
				setSelectedConfig();
			}
		}

	}

	/**
	 * Comparator class used to sort projects in ascending order by name
	 * 
	 * @author dave
	 * 
	 */
	private class ProjectComparator implements Comparator {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(Object o1, Object o2) {
			return ((IProject) o1).getName().compareTo(
					((IProject) o2).getName());
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

	private Button addButton;
	private ConfigCellModifier configCellModifier;
	private List<IServiceConfiguration> deletedServiceConfigurations;
	private Button editButton;
	private EventHandler eventHandler;
	private TableColumn projectColumn;
	private ProjectComparator projectComparator;
	private Table projectTable;
	private Button removeButton;
	private IServiceConfiguration selectedConfiguration;
	private TableItem selectedTableItem;
	private TableColumn serviceConfigurationColumn;
	private ServiceConfigurationComparator serviceConfigurationComparator;
	private Table serviceConfigurationTable;
	private TableViewer serviceConfigurationViewer;

	public ServiceConfigurationPreferencePage() {
		super();
	}

	public ServiceConfigurationPreferencePage(String title) {
		super(title);
	}

	public ServiceConfigurationPreferencePage(String title,
			ImageDescriptor image) {
		super(title, image);
	}

	/**
	 * Add a service configuration to the set of service configurations
	 */
	private void addServiceConfiguration() {
		IServiceConfiguration config;
		int status;

		// Create a new service configuration then invoke the service
		// configuration wizard using
		// this service configuration. If the user presses ok, then add a new
		// service configuration to
		// the list.
		config = ServiceModelManager.getInstance().newServiceConfiguration(
				Messages.ServiceConfigurationPreferencePage_6);
		ServiceConfigurationWizard wizard = new ServiceConfigurationWizard(
				config);
		WizardDialog dialog = new WizardDialog(getShell(), wizard);
		status = dialog.open();
		if (status == Window.OK) {
			TableItem item;

			item = new TableItem(serviceConfigurationTable, 0);
			item.setData(config);
			item.setText(0, config.getName());
		}
	}

	/**
	 * Create the contents for this page
	 * 
	 * @param parent
	 *            - The parent widget for the client area
	 */
	@Override
	protected Control createContents(Composite parent) {
		Control mainPane;

		mainPane = createWidgets(parent);
		populateServiceConfigurationList();
		return mainPane;
	}

	/**
	 * Create the widgets for this page
	 * 
	 * @param parent
	 *            The parent widget for the client area
	 * @return
	 */
	private Control createWidgets(Composite parent) {
		GridLayout layout;
		Composite preferencePane;
		Composite buttonPane;
		TextCellEditor configEditor[];
		String configProperties[];
		GridData layoutData;
		RowLayout buttonLayout;

		eventHandler = new EventHandler();

		preferencePane = new Composite(parent, SWT.NONE);
		layout = new GridLayout(3, false);
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		preferencePane.setLayout(layout);

		serviceConfigurationTable = new Table(preferencePane, SWT.SINGLE);
		serviceConfigurationTable.addSelectionListener(eventHandler);
		serviceConfigurationTable.setHeaderVisible(true);
		serviceConfigurationTable.setLinesVisible(true);
		serviceConfigurationTable.setLayoutData(layoutData);

		serviceConfigurationColumn = new TableColumn(serviceConfigurationTable,
				SWT.NONE, 0);
		serviceConfigurationColumn
				.setText(Messages.ServiceConfigurationPreferencePage_0);
		serviceConfigurationColumn.pack();
		serviceConfigurationViewer = new TableViewer(serviceConfigurationTable);

		configEditor = new TextCellEditor[1];
		configEditor[0] = new TextCellEditor(serviceConfigurationTable);
		configProperties = new String[1];
		configProperties[0] = Messages.ServiceConfigurationPreferencePage_1;

		configCellModifier = new ConfigCellModifier();
		serviceConfigurationViewer.setCellModifier(configCellModifier);
		serviceConfigurationViewer.setCellEditors(configEditor);
		serviceConfigurationViewer.setColumnProperties(configProperties);
		serviceConfigurationTable.addSelectionListener(eventHandler);

		projectTable = new Table(preferencePane, SWT.SINGLE);
		projectTable.setHeaderVisible(true);
		projectTable.setLinesVisible(true);
		projectTable.setLayoutData(layoutData);

		projectColumn = new TableColumn(projectTable, SWT.NONE, 0);
		projectColumn.setText(Messages.ServiceConfigurationPreferencePage_2);
		projectColumn.pack();

		buttonPane = new Composite(preferencePane, SWT.NONE);
		buttonLayout = new RowLayout(SWT.VERTICAL);
		buttonLayout.fill = true;
		buttonLayout.center = false;
		buttonPane.setLayout(buttonLayout);

		addButton = new Button(buttonPane, SWT.PUSH);
		addButton.setText(Messages.ServiceConfigurationPreferencePage_3);
		addButton.addSelectionListener(eventHandler);
		editButton = new Button(buttonPane, SWT.PUSH);
		editButton.setText(Messages.ServiceConfigurationPreferencePage_4);
		editButton.addSelectionListener(eventHandler);
		editButton.setEnabled(false);
		removeButton = new Button(buttonPane, SWT.PUSH);
		removeButton.setText(Messages.ServiceConfigurationPreferencePage_5);
		removeButton.addSelectionListener(eventHandler);
		removeButton.setEnabled(false);

		return preferencePane;
	}

	/**
	 * Remove selected service configurations from the set of service
	 * configurations known to the service model manager
	 */
	private void deleteServiceConfigurations() {
		if (deletedServiceConfigurations != null) {
			for (IServiceConfiguration config : deletedServiceConfigurations) {
				ServiceModelManager.getInstance().remove(config);
			}
			deletedServiceConfigurations.clear();
		}
	}

	/**
	 * Edit an existing service configuration
	 */
	private void editServiceConfiguration() {
		ServiceConfigurationWizard wizard;
		WizardDialog dialog;
		int status;

		if (selectedConfiguration != null) {
			wizard = new ServiceConfigurationWizard(selectedConfiguration);
			dialog = new WizardDialog(getShell(), wizard);
			status = dialog.open();
		}
	}

	public void init(IWorkbench workbench) {
	}

	/**
	 * Delete service configurations when Apply button is pressed
	 */
	protected void performApply() {
		deleteServiceConfigurations();
		super.performApply();
	}

	/**
	 * Delete service configurations when Ok button is pressed
	 * 
	 * @return Status from superclass indicating if Ok processing is to continue
	 */
	public boolean performOk() {
		deleteServiceConfigurations();
		return super.performOk();
	}

	/**
	 * Fill in the list of service configurations
	 */
	private void populateServiceConfigurationList() {
		Object serviceConfigurations[];

		// Get the service configurations set, sort by name and update the table
		// with the list
		serviceConfigurationComparator = new ServiceConfigurationComparator();
		serviceConfigurations = ServiceModelManager.getInstance()
				.getConfigurations().toArray();
		Arrays.sort(serviceConfigurations, serviceConfigurationComparator);
		for (Object config : serviceConfigurations) {
			TableItem item;

			item = new TableItem(serviceConfigurationTable, 0);
			item.setData(config);
			item.setText(0, ((IServiceConfiguration) config).getName());
		}
	}

	/**
	 * Remove the selected service configuration from the set of service
	 * configurations
	 */
	private void removeServiceConfiguration() {
		int idx;

		idx = serviceConfigurationTable.indexOf(selectedTableItem);
		if (idx != -1) {
			int itemCount;

			serviceConfigurationTable.remove(idx);
			if (deletedServiceConfigurations == null) {
				deletedServiceConfigurations = new Vector<IServiceConfiguration>();
			}
			// Add deleted service configuration to vector to be processed
			// when Apply or Ok is pressed.
			deletedServiceConfigurations.add(selectedConfiguration);
			itemCount = projectTable.getItemCount();
			if (itemCount > 0) {
				projectTable.remove(0, itemCount - 1);
			}
		}
	}

	/**
	 * Record the selected service configuration and enable the edit and remove
	 * service configuration buttons.
	 */
	private void setSelectedConfig() {
		TableItem selection[];

		selection = serviceConfigurationTable.getSelection();
		if (selection.length > 0) {
			selectedTableItem = selection[0];
			selectedConfiguration = (IServiceConfiguration) selectedTableItem
					.getData();
			showProjectsForConfiguration(selectedConfiguration);
			editButton.setEnabled(true);
			removeButton.setEnabled(true);
		}
	}

	/**
	 * Build a list of projects using the selected service configuration, sort
	 * by project name then update the project table with the list of projects
	 * 
	 * @param config
	 */
	private void showProjectsForConfiguration(IServiceConfiguration config) {
		Object projects[];

		projectComparator = new ProjectComparator();
		projects = ServiceModelManager.getInstance()
				.getProjectsForConfiguration(config).toArray();
		Arrays.sort(projects, projectComparator);
		projectTable.removeAll();
		for (Object project : projects) {
			TableItem item;

			item = new TableItem(projectTable, 0);
			item.setText(((IProject) project).getName());
		}
	}

}
