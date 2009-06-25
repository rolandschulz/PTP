/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.ui.wizards;

import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.ui.wizards.ServiceConfigurationWizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Allows the user to select a resource manager.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 *
 */
public class LaunchServiceWizardPage extends ServiceConfigurationWizardPage {
	public LaunchServiceWizardPage(IService service, String pageName) {
		super(service, pageName);
		setTitle(pageName);
		setDescription("This page allows you to set the default launcher for your project."); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.ui.wizards.ServiceConfigurationWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
        Composite container = new Composite(parent, SWT.NONE);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);

        Label label = new Label(container, SWT.LEFT);
        label.setText("Choose a launch provider for your project."); //$NON-NLS-1$
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        label.setLayoutData(data);
        
		super.createControl(container);
		
		setControl(container);
	}
	
}
