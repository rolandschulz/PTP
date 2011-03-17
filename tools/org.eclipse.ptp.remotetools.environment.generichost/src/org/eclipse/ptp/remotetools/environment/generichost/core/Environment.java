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
package org.eclipse.ptp.remotetools.environment.generichost.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.extension.ITargetTypeExtension;
import org.eclipse.ptp.remotetools.environment.generichost.ui.ConfigurationPage;
import org.eclipse.ptp.remotetools.environment.wizard.AbstractEnvironmentDialogPage;
import org.eclipse.ptp.remotetools.utils.verification.ControlAttributes;

/**
 * Factory for the environment.
 * 
 * @author Daniel Felix Ferber
 * @since 1.4
 */
public class Environment implements ITargetTypeExtension {
	public Environment() {
		super();
	}

	public ITargetControl createControl(ITargetConfig config) throws CoreException {
		return new TargetControl(config, new AuthInfo(config));
	}

	public ITargetConfig createConfig(ControlAttributes attributes) throws CoreException {
		ConfigFactory factory = new ConfigFactory(attributes);
		return factory.createTargetConfig();
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
