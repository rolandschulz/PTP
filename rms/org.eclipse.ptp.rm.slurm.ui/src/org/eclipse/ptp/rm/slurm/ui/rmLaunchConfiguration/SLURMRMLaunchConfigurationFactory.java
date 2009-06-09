/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.slurm.core.rmsystem.SLURMResourceManager;

public class SLURMRMLaunchConfigurationFactory extends
		AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm) throws CoreException {
		return new SLURMRMLaunchConfigurationDynamicTab(rm);
	}

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return SLURMResourceManager.class;
	}

}
