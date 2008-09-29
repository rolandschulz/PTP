/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.environment.remotesimulator.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.cell.environment.remotesimulator.core.ConfigFactory;
import org.eclipse.ptp.cell.environment.remotesimulator.ui.ConfigurationPage;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;


/**
 * Factory for the Environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2.0
 */
public class Environment implements ITargetTypeExtension {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.management.ICellTargetEnvironment#controlFactory(java.util.Map, java.lang.String)
	 */
	public ITargetControl controlFactory(ITargetElement element)
			throws CoreException {
		return new TargetControl(element);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.management.ICellTargetEnvironment#dialogPageFactory(java.util.Map, java.lang.String)
	 */
	public AbstractEnvironmentDialogPage dialogPageFactory(ITargetElement element) {
		return new ConfigurationPage(element.getName(), element.getAttributes());
	}
	
	public AbstractEnvironmentDialogPage dialogPageFactory() {
		return new ConfigurationPage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.environment.management.ICellTargetEnvironment#getControlAttributeNames()
	 */
	public String[] getControlAttributeNames() {
		return ConfigFactory.KEY_ARRAY;
	}

	public String[] getControlAttributeNamesForCipheredKeys() {
		return  ConfigFactory.KEY_CIPHERED_ARRAY;
	}

}
