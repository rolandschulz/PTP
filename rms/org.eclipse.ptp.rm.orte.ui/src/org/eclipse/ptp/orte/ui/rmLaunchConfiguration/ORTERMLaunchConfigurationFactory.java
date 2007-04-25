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
 *******************************************************************************/
package org.eclipse.ptp.orte.ui.rmLaunchConfiguration;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class ORTERMLaunchConfigurationFactory extends
		AbstractRMLaunchConfigurationFactory {

	@Override
	protected IRMLaunchConfigurationDynamicTab doCreate(IResourceManager rm) throws CoreException {
		return new ORTERMLaunchConfigurationDynamicTab(rm);
	}

	@Override
	public Class<? extends IResourceManager> getResourceManagerClass() {
		return ORTEResourceManager.class;
	}

}
