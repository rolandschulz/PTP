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

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeModelDefCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStopEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.runtime.command.ProxyRuntimeCommandFactory;
import org.eclipse.ptp.rm.slurm.core.rmsystem.SLURMResourceManagerConfiguration;

public class SLURMProxyCommandFactory implements IProxyRuntimeCommandFactory {
	public final static String SLURMD_PATH_ATTR = "slurmdPath";
	public final static String SLURMD_ARGS_ATTR = "slurmdArgs";
	
	private IProxyRuntimeCommandFactory factory;
	private SLURMResourceManagerConfiguration config;
	
	public SLURMProxyCommandFactory(SLURMResourceManagerConfiguration config) {
		this.factory = new ProxyRuntimeCommandFactory();
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeInitCommand(int)
	 */
	public IProxyRuntimeInitCommand newProxyRuntimeInitCommand(int baseId) {
		IProxyRuntimeInitCommand cmd = factory.newProxyRuntimeInitCommand(baseId);
		if (!config.useDefaults()) {
			if (!config.getSlurmdPath().equals("")) {
				cmd.addArgument(SLURMD_PATH_ATTR + "=" + config.getSlurmdPath());
			}
			if (!config.getSlurmdArgs().equals("")) {
				for (String arg : config.getSlurmdArgs().split(" ")) {
					cmd.addArgument(SLURMD_ARGS_ATTR + "=" + arg);
				}
			}
		}
		return cmd;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeModelDefCommand()
	 */
	public IProxyRuntimeModelDefCommand newProxyRuntimeModelDefCommand() {
		return factory.newProxyRuntimeModelDefCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeStartEventsCommand()
	 */
	public IProxyRuntimeStartEventsCommand newProxyRuntimeStartEventsCommand() {
		return factory.newProxyRuntimeStartEventsCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeStopEventsCommand()
	 */
	public IProxyRuntimeStopEventsCommand newProxyRuntimeStopEventsCommand() {
		return factory.newProxyRuntimeStopEventsCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeSubmitJobCommand(java.lang.String[])
	 */
	public IProxyRuntimeSubmitJobCommand newProxyRuntimeSubmitJobCommand(
			String[] args) {
		return factory.newProxyRuntimeSubmitJobCommand(args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeTerminateJobCommand(java.lang.String)
	 */
	public IProxyRuntimeTerminateJobCommand newProxyRuntimeTerminateJobCommand(
			String jobId) {
		return factory.newProxyRuntimeTerminateJobCommand(jobId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.command.ProxyCommandFactory#toCommand(org.eclipse.ptp.proxy.packet.ProxyPacket)
	 */
	public IProxyCommand toCommand(ProxyPacket packet) {
		return factory.toCommand(packet);
	}
}
