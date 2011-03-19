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

import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManager;

public class SLURMResourceManager extends AbstractRuntimeResourceManager {

	/**
	 * @since 5.0
	 */
	public SLURMResourceManager(SLURMResourceManagerConfiguration config, SLURMResourceManagerControl control,
			SLURMResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}
}