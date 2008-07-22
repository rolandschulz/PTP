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
package org.eclipse.ptp.remote.remotetools.ui.environment;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remote.remotetools.core.environment.ConfigFactory;
import org.eclipse.ptp.remote.remotetools.core.environment.PTPTargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;


/**
 * Factory for the environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.2
 */
public class Environment implements ITargetTypeExtension {
	
	public Environment() {
		super();
	}

	public ITargetControl controlFactory(ITargetElement element) throws CoreException {
		return new PTPTargetControl(element);
	}

	public String[] getControlAttributeNames() {
		return ConfigFactory.KEY_ARRAY;
	}

	public AbstractEnvironmentDialogPage dialogPageFactory(ITargetElement targetElement) {
		return new ConfigurationPage(targetElement.getName(), targetElement.getAttributes());
	}
	
	public AbstractEnvironmentDialogPage dialogPageFactory() {
		return new ConfigurationPage();
	}
	
	public String[] getControlAttributeNamesForCipheredKeys() {
		return ConfigFactory.KEY_CIPHERED_ARRAY;
	}
}
