/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.pbs.core.rtsystem;

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
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;

public class PBSProxyCommandFactory implements IProxyRuntimeCommandFactory {
	public final static String PBSD_PATH_ATTR = "pbsdPath"; //$NON-NLS-1$
	public final static String PBSD_ARGS_ATTR = "pbsdArgs"; //$NON-NLS-1$
	
	private IProxyRuntimeCommandFactory factory;
	private IPBSResourceManagerConfiguration config;
	
	public PBSProxyCommandFactory(IPBSResourceManagerConfiguration config) {
		this.factory = new ProxyRuntimeCommandFactory();
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeInitCommand(int)
	 */
	public IProxyRuntimeInitCommand newProxyRuntimeInitCommand(int baseId) {
		IProxyRuntimeInitCommand cmd = factory.newProxyRuntimeInitCommand(baseId);
		if (!config.getUseDefaults()) {
			if (!config.getPBSdPath().equals("")) { //$NON-NLS-1$
				cmd.addArgument(PBSD_PATH_ATTR + "=" + config.getPBSdPath()); //$NON-NLS-1$
			}
			if (!config.getPBSdArgs().equals("")) { //$NON-NLS-1$
				for (String arg : config.getPBSdArgs().split(" ")) { //$NON-NLS-1$
					cmd.addArgument(PBSD_ARGS_ATTR + "=" + arg); //$NON-NLS-1$
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
