/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/

package org.eclipse.ptp.rm.slurm.core.rtsystem;

import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteProxyRuntimeSystem;

public class SLURMRuntimeSystem extends AbstractRemoteProxyRuntimeSystem {
	public SLURMRuntimeSystem(SLURMProxyRuntimeClient proxy, AttributeDefinitionManager manager) {
		super(proxy, manager);
	}
}
