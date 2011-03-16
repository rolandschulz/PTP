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
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerConfiguration;
import org.eclipse.ptp.rm.jaxb.core.data.Control;
import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.ResourceManagerData;
import org.eclipse.ptp.rm.jaxb.core.rm.JAXBResourceManager;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * @author arossi
 * 
 */
public class JAXBRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory implements IJAXBUINonNLSConstants {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #getResourceManagerClass()
	 */
	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return JAXBResourceManager.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #doCreate(org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		if (!(rm instanceof IJAXBResourceManager)) {
			throw CoreExceptionUtils.newException(Messages.JAXBRMLaunchConfigurationFactory_doCreateError + rm, null);
		}
		return new JAXBRMLaunchConfigurationDynamicTab(((IJAXBResourceManager) rm).getControl(), dialog);
	}

	static LaunchTab getLaunchTab(IJAXBResourceManagerConfiguration config) {
		ResourceManagerData data = config.resourceManagerData();
		if (data != null) {
			Control control = data.getControlData();
			if (control != null) {
				return control.getLaunchTab();
			}
		}
		return null;
	}

	static boolean hasScript(IJAXBResourceManagerConfiguration config) {
		ResourceManagerData data = config.resourceManagerData();
		if (data != null) {
			Control control = data.getControlData();
			if (control != null) {
				return control.getScript() != null;
			}
		}
		return false;
	}
}
