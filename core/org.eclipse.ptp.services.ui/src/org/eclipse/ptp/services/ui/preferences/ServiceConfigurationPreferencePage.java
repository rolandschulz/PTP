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
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ColumnLayoutData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableLayout;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.dialogs.ServiceProviderConfigurationDialog;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
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
 * @since 1.0
 * 
 */
public class ServiceConfigurationPreferencePage extends PreferencePage
		implements IWorkbenchPreferencePage {

	/**
	 * Handle widget selection events for this page
	 * 
	 * @author dave
	 * 
	 */
	private class EventHandler extends SelectionAdapter {

		/* (non-Javadoc)
		 * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
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
			} else if (source == importButton) {
				importServiceConfiguration();
			} else if (source == exportButton) {
				exportServiceConfiguration();
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
	private class ProjectComparator implements Comparator<IProject> {

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
		 */
		public int compare(IProject p1, IProject p2) {
			return p1.getName().compareTo(p2.getName());
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
		public int compare(IServiceConfiguration s1, IServiceConfiguration s2) {
			return s1.getName().compareTo(s2.getName());
		}
	}

	private class ServiceConfigurationContentProvider implements IStructuredContentProvider {

		public Object[] getElements(Object inputElement) {
			// TODO Auto-generated method stub
			Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getConfigurations();
			configs.addAll(addedServiceConfigurations);
			configs.removeAll(deletedServiceConfigurations);
			IServiceConfiguration[] current = configs.toArray(new IServiceConfiguration[0]);
			Arrays.sort(current, serviceConfigurationComparator);
			return current;
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
			// TODO Auto-generated method stub
		}
		
	}
	
	private class ServiceConfigurationLabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			IServiceConfiguration config = (IServiceConfiguration)element;
			if (columnIndex == 0) {
				return config.getName();
			}
			if (columnIndex == 1) {
				IProject[] projects = ServiceModelManager.getInstance()
						.getProjectsForConfiguration(config).toArray(new IProject[0]);
				Arrays.sort(projects, projectComparator);
				String projectNames = null;
				for (IProject project : projects) {
					if (projectNames != null) {
						projectNames += ", " + project.getName(); //$NON-NLS-1$
					} else {
						projectNames = project.getName();
					}
				}
				return projectNames;
			}
			return null;
		}

		public void addListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}

		public void dispose() {
			// TODO Auto-generated method stub
		}

		public boolean isLabelProperty(Object element, String property) {
			return false;
		}

		public void removeListener(ILabelProviderListener listener) {
			// TODO Auto-generated method stub
		}
		
	}
	
	private final String[] fTableColumnHeaders= {
	        Messages.ServiceConfigurationPreferencePage_7, Messages.ServiceConfigurationPreferencePage_8
	};
	
	private final ColumnLayoutData[] fTableColumnLayouts= {
	        new ColumnWeightData(40),
	        new ColumnWeightData(60)
	};  

	private Button addButton;
	private Button editButton;
	private Button removeButton;
	private Button importButton;
	private Button exportButton;
	private TableItem selectedTableItem;
	private Table serviceConfigurationTable;
	private TableViewer serviceConfigurationViewer;
	
	private Set<IServiceConfiguration> deletedServiceConfigurations = new HashSet<IServiceConfiguration>();
	private Set<IServiceConfiguration> addedServiceConfigurations = new HashSet<IServiceConfiguration>();
	private EventHandler eventHandler;
	private ProjectComparator projectComparator = new ProjectComparator();
	private IServiceConfiguration selectedConfiguration;
	private ServiceConfigurationComparator serviceConfigurationComparator = new ServiceConfigurationComparator();

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

	public void init(IWorkbench workbench) {
	}

	/**
	 * Delete service configurations when Ok button is pressed
	 * 
	 * @return Status from superclass indicating if Ok processing is to continue
	 */
	public boolean performOk() {
		updateServiceConfigurations();
		return super.performOk();
	}

	/**
	 * Add a service configuration to the set of service configurations
	 */
	private void addServiceConfiguration() {
		// Create a new service configuration then invoke the service
		// configuration wizard using
		// this service configuration. If the user presses ok, then add a new
		// service configuration to
		// the list.
		IServiceConfiguration config = ServiceModelManager.getInstance().newServiceConfiguration(
				Messages.ServiceConfigurationPreferencePage_6);
		ServiceProviderConfigurationDialog dialog = new ServiceProviderConfigurationDialog(getShell(), config);
		if (dialog.open() == Dialog.OK) {
			addedServiceConfigurations.add(config);
			serviceConfigurationViewer.refresh();
		}
	}

	/**
	 * Create the widgets for this page
	 * 
	 * @param parent
	 *            The parent widget for the client area
	 * @return
	 */
	private Control createWidgets(Composite parent) {
		eventHandler = new EventHandler();

		Composite preferencePane = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginBottom = 0;
		layout.marginLeft = 0;
		layout.marginRight = 0;
		layout.marginTop = 0;
		preferencePane.setLayout(layout);

		serviceConfigurationTable = new Table(preferencePane, SWT.MULTI | SWT.FULL_SELECTION | SWT.BORDER);
		GridData data= new GridData(GridData.FILL_BOTH);
		data.widthHint = 425;
		data.heightHint = serviceConfigurationTable.getItemHeight();
		data.horizontalSpan = 1;
		serviceConfigurationTable.setLayoutData(data);
		serviceConfigurationTable.setFont(parent.getFont());
		serviceConfigurationTable.addSelectionListener(eventHandler);
 
		TableLayout tableLayout = new TableLayout();
		serviceConfigurationTable.setLayout(tableLayout);
		serviceConfigurationTable.setHeaderVisible(true);
		serviceConfigurationTable.setLinesVisible(true);

		for (int i = 0; i < fTableColumnHeaders.length; i++) {
		    tableLayout.addColumnData(fTableColumnLayouts[i]);
		    TableColumn column = new TableColumn(serviceConfigurationTable, SWT.NONE, i);
		    column.setResizable(fTableColumnLayouts[i].resizable);
		    column.setText(fTableColumnHeaders[i]);
		}
		serviceConfigurationViewer = new TableViewer(serviceConfigurationTable);
		serviceConfigurationViewer.setContentProvider(new ServiceConfigurationContentProvider());
		serviceConfigurationViewer.setLabelProvider(new ServiceConfigurationLabelProvider());
		serviceConfigurationViewer.setInput(ServiceModelManager.getInstance());

		Composite buttonPane = new Composite(preferencePane, SWT.NONE);
		layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		buttonPane.setLayout(layout);
		buttonPane.setLayoutData(new GridData(GridData.FILL_VERTICAL));
		buttonPane.setFont(preferencePane.getFont());

		addButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(addButton);
		addButton.setText(Messages.ServiceConfigurationPreferencePage_3);
		addButton.addSelectionListener(eventHandler);
		editButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(editButton);
		editButton.setText(Messages.ServiceConfigurationPreferencePage_4);
		editButton.addSelectionListener(eventHandler);
		editButton.setEnabled(false);
		removeButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(removeButton);
		removeButton.setText(Messages.ServiceConfigurationPreferencePage_5);
		removeButton.addSelectionListener(eventHandler);
		removeButton.setEnabled(false);
		importButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(importButton);
		importButton.setText(Messages.ServiceConfigurationPreferencePage_9);
		importButton.addSelectionListener(eventHandler);
		importButton.setEnabled(false);
		exportButton = new Button(buttonPane, SWT.PUSH);
		setButtonLayoutData(exportButton);
		exportButton.setText(Messages.ServiceConfigurationPreferencePage_10);
		exportButton.addSelectionListener(eventHandler);
		exportButton.setEnabled(false);

		return preferencePane;
	}

	/**
	 * Edit an existing service configuration
	 */
	private void editServiceConfiguration() {
		if (selectedConfiguration != null) {
			ServiceProviderConfigurationDialog dialog = new ServiceProviderConfigurationDialog(getShell(), selectedConfiguration);
			if (dialog.open() == Dialog.OK) {
				serviceConfigurationViewer.refresh();
			}
		}
	}

	/**
	 * Remove the selected service configuration from the set of service
	 * configurations
	 */
	private void removeServiceConfiguration() {
		deletedServiceConfigurations.add(selectedConfiguration);
		serviceConfigurationViewer.refresh();
	}

	/**
	 * Import service configurations
	 */
	private void importServiceConfiguration() {
	}
	
	/**
	 * Export service configurations
	 */
	private void exportServiceConfiguration() {
	}
	
	/**
	 * Record the selected service configuration and enable the edit and remove
	 * service configuration buttons.
	 */
	private void setSelectedConfig() {
		TableItem[] selection = serviceConfigurationTable.getSelection();
		boolean enabled = selection.length > 0;
		if (enabled) {
			selectedTableItem = selection[0];
			selectedConfiguration = (IServiceConfiguration) selectedTableItem
					.getData();
		}
		editButton.setEnabled(enabled);
		removeButton.setEnabled(enabled);
	}

	/**
	 * Add/remove selected service configurations from the set of service
	 * configurations known to the service model manager
	 */
	private void updateServiceConfigurations() {
		addedServiceConfigurations.removeAll(deletedServiceConfigurations);
		for (IServiceConfiguration config : addedServiceConfigurations) {
			ServiceModelManager.getInstance().addConfiguration(config);
		}
		for (IServiceConfiguration config : deletedServiceConfigurations) {
			if (ServiceModelManager.getInstance().getConfiguration(config.getId()) != null) {
				ServiceModelManager.getInstance().remove(config);
			}
		}
		addedServiceConfigurations.clear();
		deletedServiceConfigurations.clear();
	}

	/**
	 * Create the contents for this page
	 * 
	 * @param parent
	 *            - The parent widget for the client area
	 */
	@Override
	protected Control createContents(Composite parent) {
		return createWidgets(parent);
	}

	/**
	 * Delete service configurations when Apply button is pressed
	 */
	protected void performApply() {
		updateServiceConfigurations();
		super.performApply();
	}

}
