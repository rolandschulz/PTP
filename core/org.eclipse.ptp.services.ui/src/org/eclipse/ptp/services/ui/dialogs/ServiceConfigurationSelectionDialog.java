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
/**
 * 
 */
package org.eclipse.ptp.services.ui.dialogs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.ui.messages.Messages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

/**
 * Display a dialog prompting the user to select a service configuration to add
 * to the project. Only service configurations not currently used by the project
 * are diplayed.
 * 
 * @author dave
 * 
 */
public class ServiceConfigurationSelectionDialog extends TitleAreaDialog {
	/**
	 * Class to handle widget selection events for this dialog.
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
			TableItem selection[];

			selection = serviceConfigurationList.getSelection();
			if (selection.length > 0) {
				selectedConfig = (IServiceConfiguration) selection[0].getData();
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
	private Table serviceConfigurationList;
	private IServiceConfiguration selectedConfig;
	private EventHandler eventHandler;

	private ServiceConfigurationComparator serviceConfigurationComparator;

	private Set<IServiceConfiguration> currentServiceConfigurations;

	/**
	 * Create a dialog listing the service configurations which can be selected
	 * for the project
	 * 
	 * @param parentShell
	 *            Shell to use when displaying the dialog
	 * @param currentConfigs
	 *            Set of service configurations currently used by the project
	 */
	public ServiceConfigurationSelectionDialog(Shell parentShell,
			Set<IServiceConfiguration> currentConfigs) {
		super(parentShell);
		serviceConfigurationComparator = new ServiceConfigurationComparator();
		currentServiceConfigurations = currentConfigs;
	}

	/**
	 * Create the widgets used to display the list of available service
	 * configurations
	 * 
	 * @param parent
	 *            - The composite widget that is parent to the client area
	 * @return Top level control for client area
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite serviceConfigurationPane;
		GridLayout layout;
		GridData layoutData;

		eventHandler = new EventHandler();
		setTitle(Messages.ServiceConfigurationSelectionDialog_0);
		setMessage(Messages.ServiceConfigurationSelectionDialog_1, SWT.NONE);
		serviceConfigurationPane = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		serviceConfigurationPane.setLayout(layout);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		serviceConfigurationList = new Table(serviceConfigurationPane,
				SWT.SINGLE);
		serviceConfigurationList.setLinesVisible(true);
		serviceConfigurationList.addSelectionListener(eventHandler);
		serviceConfigurationList.setLayoutData(layoutData);
		populateList();
		return serviceConfigurationPane;
	}

	/**
	 * Return the service configuration selected by the user
	 * 
	 * @return Selected service configuration
	 */
	public IServiceConfiguration getSelectedConfiguration() {
		return selectedConfig;
	}

	/**
	 * Fill in the list of available service configurations. Check if each
	 * service is already used by the project. If not, then add it to the list
	 * of available configurations.
	 */
	private void populateList() {
		Object serviceConfigurations[];

		serviceConfigurations = ServiceModelManager.getInstance()
				.getConfigurations().toArray();
		Arrays.sort(serviceConfigurations, serviceConfigurationComparator);
		for (Object config : serviceConfigurations) {
			TableItem item;

			if (!currentServiceConfigurations.contains(config)) {
				item = new TableItem(serviceConfigurationList, 0);
				item.setData(config);
				item.setText(0, ((IServiceConfiguration) config).getName());
			}
		}
	}
}
