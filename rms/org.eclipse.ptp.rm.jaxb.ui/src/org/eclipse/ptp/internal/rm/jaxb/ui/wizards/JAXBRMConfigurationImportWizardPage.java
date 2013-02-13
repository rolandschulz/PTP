/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.ui.wizards;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.internal.rm.jaxb.core.JAXBExtensionUtils;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * Loads all extensions to the JAXB Resource Manager extension point and displays names for user to select from.
 * 
 * @author arossi
 * 
 */
public class JAXBRMConfigurationImportWizardPage extends WizardPage implements SelectionListener {
	private Combo configurations;

	private String[] items;
	private URL[] urls;
	private int index;

	/**
	 * @param pageName
	 */
	public JAXBRMConfigurationImportWizardPage(String pageName) {
		super(pageName);
		setDescription(Messages.ConfigurationImportWizardPageDescription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets .Composite)
	 */
	public void createControl(Composite parent) {
		GridData gridData = WidgetBuilderUtils.createGridData(GridData.GRAB_HORIZONTAL | GridData.FILL_HORIZONTAL, 3);
		GridLayout layout = WidgetBuilderUtils.createGridLayout(3, false);
		Group group = WidgetBuilderUtils.createGroup(parent, SWT.NONE, layout, gridData);
		new Label(group, SWT.NONE).setLayoutData(gridData);
		WidgetBuilderUtils.createLabel(group, Messages.ConfigurationImportWizardPageLabel, SWT.LEFT, 1).setToolTipText(
				Messages.ConfigurationImportWizardPageTooltip);
		gridData = WidgetBuilderUtils.createGridDataFillH(2);
		configurations = WidgetBuilderUtils.createCombo(group, SWT.BORDER | SWT.READ_ONLY, gridData, items,
				JAXBUIConstants.ZEROSTR, null, null, this);
		setControl(group);
	}

	/**
	 * @return location of the selected configuration.
	 */
	public URL getSelectedConfiguration() {
		if (index < 0 || index >= urls.length) {
			return null;
		}
		return urls[index];
	}

	/**
	 * @return name of the selection
	 */
	public String getSelectedName() {
		if (index < 0 || index >= items.length) {
			return null;
		}
		return items[index];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.wizard.WizardPage#isPageComplete()
	 */
	@Override
	public boolean isPageComplete() {
		return index != 0;
	}

	/**
	 * Populate the combo box and URL data with the available extensions.
	 */
	public void loadConfigurations() {
		List<String> names = new ArrayList<String>();
		names.add(JAXBUIConstants.ZEROSTR);
		List<URL> locations = new ArrayList<URL>();
		locations.add(null);
		Map<String, URL> configs = JAXBExtensionUtils.getPluginConfiguations();
		if (JAXBExtensionUtils.getInvalid() != null) {
			MessageDialog.openError(Display.getCurrent().getActiveShell(), Messages.InvalidConfiguration_title,
					Messages.InvalidConfiguration + JAXBExtensionUtils.getInvalid());
		}
		for (Map.Entry<String, URL> e : configs.entrySet()) {
			names.add(e.getKey());
			locations.add(e.getValue());
		}
		items = names.toArray(new String[0]);
		urls = locations.toArray(new URL[0]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetDefaultSelected(org.eclipse .swt.events.SelectionEvent)
	 */
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.swt.events.SelectionListener#widgetSelected(org.eclipse.swt .events.SelectionEvent)
	 */
	public void widgetSelected(SelectionEvent e) {
		index = configurations.getSelectionIndex();
		setPageComplete(isPageComplete());
	}
}
