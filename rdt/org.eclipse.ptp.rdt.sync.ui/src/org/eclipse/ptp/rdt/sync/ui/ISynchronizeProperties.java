/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.swt.widgets.Composite;

/**
 * Implemented by clients wishing to extend the synchronize property page.
 * 
 */
public interface ISynchronizeProperties extends ISynchronizePropertiesDescriptor {
	/**
	 * Called when a new configuration is added to the preference page.
	 * 
	 * @param config
	 *            new configuration
	 */
	public void addConfiguration(SyncConfig config);

	/**
	 * Create wizard pages that will be displayed when the add button is pressed.
	 * 
	 * @param project
	 *            project who's properties are being displayed
	 */
	public WizardPage[] createAddWizardPages(IProject project);

	/**
	 * Create a composite that will be displayed in the properties user defined configuration area when a sync configuration is
	 * selected.
	 * 
	 * @param parent
	 *            parent composite that contains the configuration area
	 * @param config
	 *            project properties being displayed
	 * @param context
	 *            runnable context (can be null)
	 */
	public void createPropertiesConfigurationArea(Composite parent, SyncConfig config);

	/**
	 * Called to dispose of the composite when a sync config selection is changed.
	 */
	public void disposePropertiesConfigurationArea();

	/**
	 * Called when the OK or Apply buttons on the preference page are selected. Apply any changes to the project properties.
	 */
	public void performApply();

	/**
	 * Called when the Cancel button on the preference page is selected. Cancel any changes to the project properties.
	 */
	public void performCancel();

	/**
	 * Called when the Defaults button on the preference page is selected. Revert any changes to the project properties to default
	 * values.
	 */
	public void performDefaults();
}
