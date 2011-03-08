/*******************************************************************************
 * Copyright (c) 2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 		Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 * 		Jie Jiang, 	National University of Defense Technology
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.rm.slurm.core.SLURMCorePlugin;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMNodeAttributes;
import org.eclipse.ptp.rm.slurm.core.rtsystem.SLURMProxyRuntimeClient;
import org.eclipse.ptp.rm.slurm.core.rtsystem.SLURMRuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

public class SLURMResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 5.0
	 */
	public SLURMResourceManager(IResourceManagerConfiguration config, AbstractRuntimeResourceManagerControl control,
			AbstractRuntimeResourceManagerMonitor monitor) {
		super(config, control, monitor);
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
		IRuntimeSystem slurmRMS;
		ISLURMResourceManagerConfiguration config = (ISLURMResourceManagerConfiguration) getConfiguration();
		IPResourceManager rm = (IPResourceManager) getAdapter(IPResourceManager.class);
		int baseId;
		try {
			baseId = Integer.parseInt(rm.getID());
		} catch (NumberFormatException e) {
			throw new CoreException(new Status(IStatus.ERROR, SLURMCorePlugin.getUniqueIdentifier(), e.getLocalizedMessage()));
		}
		SLURMProxyRuntimeClient runtimeProxy = new SLURMProxyRuntimeClient(config, baseId);
		slurmRMS = new SLURMRuntimeSystem(runtimeProxy);
		AttributeDefinitionManager attrDefMgr = slurmRMS.getAttributeDefinitionManager();
		attrDefMgr.setAttributeDefinitions(SLURMJobAttributes.getDefaultAttributeDefinitions());
		attrDefMgr.setAttributeDefinitions(SLURMNodeAttributes.getDefaultAttributeDefinitions());
		return slurmRMS;
	}

}