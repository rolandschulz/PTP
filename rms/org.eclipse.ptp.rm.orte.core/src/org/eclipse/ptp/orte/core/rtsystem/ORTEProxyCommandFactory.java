/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.orte.core.rtsystem;

import org.eclipse.ptp.orte.core.rmsystem.ORTEResourceManagerConfiguration;
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

public class ORTEProxyCommandFactory implements IProxyRuntimeCommandFactory {
	public final static String ORTED_PATH_ATTR = "ortedPath";
	public final static String ORTED_ARGS_ATTR = "ortedArgs";
	
	private IProxyRuntimeCommandFactory factory;
	private ORTEResourceManagerConfiguration config;
	
	public ORTEProxyCommandFactory(ORTEResourceManagerConfiguration config) {
		this.factory = new ProxyRuntimeCommandFactory();
		this.config = config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeInitCommand(int)
	 */
	public IProxyRuntimeInitCommand newProxyRuntimeInitCommand(int baseId) {
		IProxyRuntimeInitCommand cmd = factory.newProxyRuntimeInitCommand(baseId);
		if (!config.useDefaults()) {
			if (!config.getOrtedPath().equals("")) {
				cmd.addArgument(ORTED_PATH_ATTR + "=" + config.getOrtedPath());
			}
			if (!config.getOrtedArgs().equals("")) {
				for (String arg : config.getOrtedArgs().split(" ")) {
					cmd.addArgument(ORTED_ARGS_ATTR + "=" + arg);
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
