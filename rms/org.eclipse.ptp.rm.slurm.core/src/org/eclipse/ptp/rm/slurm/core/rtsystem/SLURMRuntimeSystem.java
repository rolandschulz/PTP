/*******************************************************************************
 * Copyright (c) 2008,2009 School of Computer Science, 
 * National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/

package org.eclipse.ptp.rm.slurm.core.rtsystem;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.rm.core.proxy.AbstractRemoteProxyRuntimeSystem;
import org.eclipse.ptp.rm.slurm.core.SLURMJobAttributes;
import org.eclipse.ptp.rm.slurm.core.SLURMLaunchConfiguration;

public class SLURMRuntimeSystem extends AbstractRemoteProxyRuntimeSystem {
	public SLURMRuntimeSystem(SLURMProxyRuntimeClient proxy) {
		super(proxy);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.AbstractProxyRuntimeSystem#getAttributes(org
	 * .eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public List<IAttribute<?, ?, ?>> getAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {

		List<IAttribute<?, ?, ?>> attrs = super.getAttributes(configuration, mode);

		int jobnumProcs = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_NUMPROCS, -1);
		try {
			attrs.add(SLURMJobAttributes.getJobNumberOfProcsAttributeDefinition().create(jobnumProcs));
		} catch (IllegalValueException e) {
		}

		int jobNumNodes = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_NUMNODES, -1);
		try {
			attrs.add(SLURMJobAttributes.getJobNumberOfNodesAttributeDefinition().create(jobNumNodes));
		} catch (IllegalValueException e) {
		}

		int jobTimeLimit = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_TIMELIMIT, -1);
		try {
			attrs.add(SLURMJobAttributes.getJobTimelimitAttributeDefinition().create(jobTimeLimit));
		} catch (IllegalValueException e) {
		}

		String jobPartition = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBPARTITION, "");//$NON-NLS-1$
		if (jobPartition.length() > 0) {
			attrs.add(SLURMJobAttributes.getJobPartitionAttributeDefinition().create(jobPartition));
		}

		String jobReqNodeList = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBREQNODELIST, "");//$NON-NLS-1$
		if (jobReqNodeList.length() > 0) {
			attrs.add(SLURMJobAttributes.getJobReqNodeListAttributeDefinition().create(jobReqNodeList));
		}

		String jobExcNodeList = configuration.getAttribute(SLURMLaunchConfiguration.ATTR_JOBEXCNODELIST, "");//$NON-NLS-1$
		if (jobExcNodeList.length() > 0) {
			attrs.add(SLURMJobAttributes.getJobExcNodeListAttributeDefinition().create(jobExcNodeList));
		}

		return attrs;
	}
}
