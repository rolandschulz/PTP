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

/**
 * Abstract class that is the extension point for contributing additional RM
 * configuration types for selection in the RM configuration wizard.
 * 
 * @since 5.0
 */
public abstract class RMConfigurationSelectionFactory {

	/**
	 * Retrieves the names of addition providers to display in the wizard.
	 * 
	 * @return array of providers
	 * @since 5.0
	 */
	public abstract String[] getProviderNames();

	/**
	 * Set the provider that has been selected in the wizard.
	 * 
	 * @since 5.0
	 */
	public abstract void setProvider(String name);
}
