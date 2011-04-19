/**
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.ui.wizards;

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * Abstract class that is the extension point for contributing additional RM
 * configurations for selection in the RM configuration wizard.
 * 
 * @since 5.0
 */
public abstract class RMConfigurationSelectionFactory {

	private String fId;

	/**
	 * Retrieves the names of addition resource managers to display in the
	 * wizard.
	 * 
	 * @return array of provider types
	 * @since 5.0
	 */
	public abstract String[] getConfigurationNames();

	/**
	 * Get the ID of the resource manager that this extension is for.
	 * 
	 * @return ID of the resource manager
	 */
	public String getId() {
		return fId;
	}

	/**
	 * Set the resource manager name that has been selected in the wizard.
	 * 
	 * @since 5.0
	 */
	public abstract void setConfigurationName(String name, IResourceManagerConfiguration configuration);

	/**
	 * /** Set the ID of the resource manager that this extension is for.
	 * 
	 * @param id
	 *            resource manager ID
	 */
	public void setId(String id) {
		fId = id;
	}
}
