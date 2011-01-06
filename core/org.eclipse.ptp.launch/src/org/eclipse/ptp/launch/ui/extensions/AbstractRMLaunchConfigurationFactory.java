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
package org.eclipse.ptp.launch.ui.extensions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.messages.Messages;

/**
 * Abstract class that is the extension point for contributing Dynamic
 * configuration tabs to this plug-in.
 * 
 * @author rsqrd
 * 
 */
public abstract class AbstractRMLaunchConfigurationFactory {

	protected static CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), Status.ERROR, string, null);
		return new CoreException(status);
	}

	/**
	 * Create a new launch configuration dynamic tab.
	 * 
	 * @param rm
	 *            resource manager this tab is for
	 * @param dialog
	 *            dialog that is creating the tab
	 * @return new dynamic tab
	 * @throws CoreException
	 * @since 5.0
	 */
	public IRMLaunchConfigurationDynamicTab create(IPResourceManager rm, ILaunchConfigurationDialog dialog) throws CoreException {
		if (!getResourceManagerClass().isInstance(rm)) {
			throw makeCoreException(NLS.bind(Messages.AbstractRMLaunchConfigurationFactory_0, rm.getName()));
		}
		return doCreate(rm, dialog);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getResourceManagerClass()
	 */
	public abstract Class<? extends IPResourceManager> getResourceManagerClass();

	/**
	 * Method to actually create the tab.
	 * 
	 * @param rm
	 *            resource manager this tab is for
	 * @param dialog
	 *            dialog that is creating the tab
	 * @return new dynamic tab
	 * @throws CoreException
	 * @since 5.0
	 */
	protected abstract IRMLaunchConfigurationDynamicTab doCreate(IPResourceManager rm, ILaunchConfigurationDialog dialog)
			throws CoreException;
}
