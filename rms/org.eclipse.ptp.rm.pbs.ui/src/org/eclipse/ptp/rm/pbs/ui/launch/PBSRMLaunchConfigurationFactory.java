/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Albert L. Rossi - modification of doCreate construction
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;

public class PBSRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #getResourceManagerClass()
	 */
	@Override
	public Class<? extends IResourceManagerControl> getResourceManagerClass() {
		return PBSResourceManager.class;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #doCreate(org.eclipse.ptp.core.elements.IPResourceManager,
	 * org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManagerControl rm, ILaunchConfigurationDialog dialog)
			throws CoreException {
		return new PBSRMLaunchConfigurationDynamicTab(rm, dialog);
	}
}
