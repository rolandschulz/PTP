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


import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class NewMPICH2RMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	public NewMPICH2RMLaunchConfigurationDynamicTab(IResourceManager rm) {
		super();
		addDynamicTab(new BasicMPICH2RMLaunchConfigurationDynamicTab());
		addDynamicTab(new AdvancedMPICH2RMLaunchConfigurationDynamicTab());
	}
}
