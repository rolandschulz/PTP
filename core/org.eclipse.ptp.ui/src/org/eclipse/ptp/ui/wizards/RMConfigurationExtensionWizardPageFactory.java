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
 * Abstract class that is the extension point for contributing
 * additional pages to RM configuration wizards.
 */
public abstract class RMConfigurationExtensionWizardPageFactory {

	/**
	 * Retrieves the tool specific configuration pages.
	 * 
	 * @param wizard 
	 * @return configuration pages
	 */
	public abstract RMConfigurationWizardPage[] getPages(IRMConfigurationWizard wizard);

}
