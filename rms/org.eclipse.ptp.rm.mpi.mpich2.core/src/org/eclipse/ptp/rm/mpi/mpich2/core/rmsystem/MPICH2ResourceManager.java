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
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rm.core.AbstractToolsAttributes;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManager;
import org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem.MPICH2RuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class MPICH2ResourceManager extends AbstractToolResourceManager {

	private MPICH2RuntimeSystem rts = null;

	/**
	 * @since 2.0
	 */
	public MPICH2ResourceManager(IPUniverse universe, IResourceManagerConfiguration config) {
		super(universe, config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem
	 * ()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() throws CoreException {
		IMPICH2ResourceManagerConfiguration config = (IMPICH2ResourceManagerConfiguration) getConfiguration();
		AttributeDefinitionManager attrDefMgr = getAttributeDefinitionManager();
		attrDefMgr.setAttributeDefinitions(AbstractToolsAttributes.getDefaultAttributeDefinitions());
		rts = new MPICH2RuntimeSystem(this, config, attrDefMgr);
		return rts;
	}
}
