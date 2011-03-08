/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.ui.launch;

import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class GenericRMLaunchConfigurationDynamicTab extends ExtendableRMLaunchConfigurationDynamicTab {

	public GenericRMLaunchConfigurationDynamicTab(IResourceManager rm, ILaunchConfigurationDialog dialog) {
		super(dialog);
	}
}
