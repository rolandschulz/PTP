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

import org.eclipse.ptp.proxy.runtime.event.ProxyRuntimeEventFactory;
import org.eclipse.ptp.rm.core.proxy.AbstractRemoteProxyRuntimeClient;
import org.eclipse.ptp.rm.slurm.core.rmsystem.ISLURMResourceManagerConfiguration;

public class SLURMProxyRuntimeClient extends AbstractRemoteProxyRuntimeClient {
	public SLURMProxyRuntimeClient(ISLURMResourceManagerConfiguration config, 
			int baseModelId) {
		super(config, baseModelId, new SLURMProxyCommandFactory(config), new ProxyRuntimeEventFactory());
	}
}
