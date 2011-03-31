/**
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.rm.jaxb.ui.wizards;

import org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory;

public class JAXBRMConfigurationSelectionFactory extends RMConfigurationSelectionFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory#getProviderNames
	 * ()
	 */
	@Override
	public String[] getProviderNames() {
		return new String[] { "JAXB" }; //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.ui.wizards.RMConfigurationSelectionFactory#setProvider
	 * (java.lang.String)
	 */
	@Override
	public void setProvider(String name) {

	}
}
