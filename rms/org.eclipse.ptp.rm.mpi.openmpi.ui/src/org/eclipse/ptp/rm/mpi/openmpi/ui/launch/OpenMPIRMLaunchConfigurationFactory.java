/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem.OpenMPIResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManagerControl rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		return new NewOpenMPIRMLaunchConfigurationDynamicTab(rm, dialog);
	}

	@Override
	public Class<? extends IResourceManagerControl> getResourceManagerClass() {
		return OpenMPIResourceManager.class;
	}

}
