/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.ibm.pe.core.rtsystem;

import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rm.ibm.pe.core.rmsystem.PEResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.IRuntimeSystemFactory;

public class PERuntimeSystemFactory implements IRuntimeSystemFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rtsystem.IRuntimeSystemFactory#create(org.eclipse.ptp .rmsystem.IResourceManager)
	 */
	public IRuntimeSystem create(IResourceManager rm) {
		IPResourceManager prm = ModelManager.getInstance().getUniverse().getResourceManager(rm.getUniqueName());
		int baseId = 0;
		try {
			baseId = Integer.parseInt(prm.getID());
		} catch (NumberFormatException e) {
			// Ignore
		}
		PEProxyRuntimeClient runtimeProxy = new PEProxyRuntimeClient((PEResourceManagerConfiguration) rm.getConfiguration(), baseId);
		return new PERuntimeSystem(runtimeProxy);
	}
}
