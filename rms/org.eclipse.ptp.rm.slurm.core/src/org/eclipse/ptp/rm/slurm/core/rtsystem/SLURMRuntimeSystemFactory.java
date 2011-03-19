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
package org.eclipse.ptp.rm.slurm.core.rtsystem;

import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMNodeAttributes;
import org.eclipse.ptp.rm.slurm.core.rmsystem.ISLURMResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;
import org.eclipse.ptp.rtsystem.IRuntimeSystemFactory;

public class SLURMRuntimeSystemFactory implements IRuntimeSystemFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeSystemFactory#create(org.eclipse.ptp
	 * .rmsystem.IResourceManager)
	 */
	public IRuntimeSystem create(IResourceManager rm) {
		IRuntimeSystem slurmRMS;
		ISLURMResourceManagerConfiguration config = (ISLURMResourceManagerConfiguration) rm.getConfiguration();
		IPResourceManager prm = (IPResourceManager) rm.getAdapter(IPResourceManager.class);
		int baseId = 0;
		try {
			baseId = Integer.parseInt(prm.getID());
		} catch (NumberFormatException e) {
			// Ignore
		}
		SLURMProxyRuntimeClient runtimeProxy = new SLURMProxyRuntimeClient(config, baseId);
		slurmRMS = new SLURMRuntimeSystem(runtimeProxy);
		AttributeDefinitionManager attrDefMgr = slurmRMS.getAttributeDefinitionManager();
		attrDefMgr.setAttributeDefinitions(SLURMJobAttributes.getDefaultAttributeDefinitions());
		attrDefMgr.setAttributeDefinitions(SLURMNodeAttributes.getDefaultAttributeDefinitions());
		return slurmRMS;
	}
}
