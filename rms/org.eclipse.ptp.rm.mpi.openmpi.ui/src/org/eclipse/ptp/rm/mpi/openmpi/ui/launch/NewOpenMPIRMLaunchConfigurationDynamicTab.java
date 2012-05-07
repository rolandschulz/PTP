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

import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class NewOpenMPIRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	/**
	 * @since 2.0
	 */
	public NewOpenMPIRMLaunchConfigurationDynamicTab(IResourceManager rm) {
		addDynamicTab(new BasicOpenMpiRMLaunchConfigurationDynamicTab());
		addDynamicTab(new AdvancedOpenMpiRMLaunchConfigurationDynamicTab(rm));
	}
}
