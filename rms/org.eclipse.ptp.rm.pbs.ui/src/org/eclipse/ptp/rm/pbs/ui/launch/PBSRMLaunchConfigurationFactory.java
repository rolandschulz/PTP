/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;

public class PBSRMLaunchConfigurationFactory extends
		AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm) throws CoreException {
		return new PBSRMLaunchConfigurationDynamicTab(rm);
	}

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return PBSResourceManager.class;
	}

}
