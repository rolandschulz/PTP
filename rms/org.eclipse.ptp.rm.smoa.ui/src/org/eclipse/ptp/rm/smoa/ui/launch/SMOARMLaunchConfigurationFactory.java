/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.smoa.core.rmsystem.SMOAResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * Part of an extension of PTP plug-in. Shows where to take launch tab from.
 */
public class SMOARMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		return new SMOARMLaunchConfigurationDynamicTab(rm, dialog);
	}

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return SMOAResourceManager.class;
	}
}
