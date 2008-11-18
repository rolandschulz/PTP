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
package org.eclipse.ptp.rm.mpi.mpich2.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManager;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2RMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm) throws CoreException {
		return new NewMPICH2RMLaunchConfigurationDynamicTab(rm);
	}

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return MPICH2ResourceManager.class;
	}

}
