/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *  
 * Copyright (c) 2006, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.ibm.ll.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.ibm.ll.core.rmsystem.IBMLLResourceManager;

public class IBMLLRMLaunchConfigurationFactory extends AbstractRMLaunchConfigurationFactory {

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
		return new IBMLLRMLaunchConfigurationDynamicTab(rm, dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory
	 * #getResourceManagerClass()
	 */
	@Override
	public Class<? extends IResourceManagerControl> getResourceManagerClass() {
		return IBMLLResourceManager.class;
	}

}
