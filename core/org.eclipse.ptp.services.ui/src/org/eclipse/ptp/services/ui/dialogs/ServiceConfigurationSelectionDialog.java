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

import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.ptp.internal.services.ui.messages.Messages;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.ui.widgets.ServiceConfigurationSelectionWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Display a dialog prompting the user to select a service configuration to add
 * to the project. Only service configurations not currently used by the project
 * are displayed.
 * 
 * @author dave
 * 
 */
public class ServiceConfigurationSelectionDialog extends TitleAreaDialog {
	private ServiceConfigurationSelectionWidget fServiceWidget;
	private Set<IServiceConfiguration> fCurrentServiceConfigurations;

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
		fCurrentServiceConfigurations = currentConfigs;
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

		setTitle(Messages.ServiceConfigurationSelectionDialog_0);
		setMessage(Messages.ServiceConfigurationSelectionDialog_1, SWT.NONE);
		
		serviceConfigurationPane = new Composite(parent, SWT.NONE);
		layout = new GridLayout(1, true);
		serviceConfigurationPane.setLayout(layout);
		serviceConfigurationPane.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		fServiceWidget = new ServiceConfigurationSelectionWidget(serviceConfigurationPane, SWT.NONE, 
				fCurrentServiceConfigurations, null, false);
		fServiceWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		return fServiceWidget;
	}

	/**
	 * Return the service configuration selected by the user
	 * 
	 * @return Selected service configuration
	 */
	public IServiceConfiguration getSelectedConfiguration() {
		return fServiceWidget.getSelectedConfiguration();
	}
}
