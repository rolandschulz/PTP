/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * JAXB implementation. Provides the controller tab.
 * 
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return JAXBResourceManager.class;
	}

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		if (!(rm instanceof IJAXBResourceManager)) {
			throw CoreExceptionUtils.newException(Messages.JAXBRMLaunchConfigurationFactory_wrongRMType + rm, null);
		}
		try {
			return new JAXBControllerLaunchConfigurationTab((IJAXBResourceManager) rm, dialog);
		} catch (Throwable t) {
			throw CoreExceptionUtils.newException(Messages.JAXBRMLaunchConfigurationFactory_doCreateError + rm, t);
		}
	}
}
