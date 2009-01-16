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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;


import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rm.core.AbstractToolsAttributes;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.OmpiInfo;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIRuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class OpenMPIResourceManager extends AbstractToolResourceManager {

	private Integer OPENMPI_RMID;
	private OpenMPIRuntimeSystem rts = null;

	public OpenMPIResourceManager(Integer id, IPUniverseControl universe, IResourceManagerConfiguration config) {
		super(id.toString(), universe, config);
		OPENMPI_RMID = id;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() {
		OpenMPIResourceManagerConfiguration config = (OpenMPIResourceManagerConfiguration) getConfiguration();
		AttributeDefinitionManager attrDefMgr = getAttributeDefinitionManager();
		attrDefMgr.setAttributeDefinitions(AbstractToolsAttributes.getDefaultAttributeDefinitions());
		rts = new OpenMPIRuntimeSystem(OPENMPI_RMID, config, attrDefMgr);
		return rts;
	}

	/**
	 * Get OpenMPI info
	 * 
	 * @return OmpiInfo
	 */
	public OmpiInfo getOmpiInfo() {
		if (rts == null)
			return null;
		try {
			return rts.getOmpiInfo().clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
