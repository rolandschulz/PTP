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
package org.eclipse.ptp.cell.environment.cellsimulator.core.remote;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.cell.environment.cellsimulator.conf.RemoteDefaultValues;
import org.eclipse.ptp.cell.environment.cellsimulator.ui.ConfigurationPage;
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
public class RemoteLaunchEnvironment implements ITargetTypeExtension {
	
	public RemoteLaunchEnvironment() {
		super();
	}

	public ITargetControl controlFactory(ITargetElement element) throws CoreException {
		return new RemoteTargetControl(element);
	}

	public String[] getControlAttributeNames() {
		return RemoteConfigurationBean.KEY_ARRAY;
	}

	public AbstractEnvironmentDialogPage dialogPageFactory(ITargetElement element) {
		ConfigurationPage page = new ConfigurationPage(element.getName(), 
				new RemoteConfigurationBean(element.getAttributes()));
		page.setAvailableAutomaticNetwork(false);
		page.setAvailableAutomaticPort(false);
		page.setAvailableRemoteConnection(true);
		page.setAvailableAutomaticWorkDirectory(false);
		return page;
	}
	
	public AbstractEnvironmentDialogPage dialogPageFactory() {
		ConfigurationPage page = new ConfigurationPage(RemoteDefaultValues.DefaultTargetName,
				new RemoteConfigurationBean());
		page.setAvailableAutomaticNetwork(false);
		page.setAvailableAutomaticPort(false);
		page.setAvailableRemoteConnection(true);
		page.setAvailableAutomaticWorkDirectory(false);
		return page;
	}

	public String[] getControlAttributeNamesForCipheredKeys() {
		return RemoteConfigurationBean.KEY_CIPHERED_ARRAY;
	}
}
