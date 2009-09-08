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

package org.eclipse.ptp.proxy.runtime.command;

import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeFilterEventsCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeInitCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeModelDefCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeStopEventsCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.ProxyCommandFactory;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public class ProxyRuntimeCommandFactory extends ProxyCommandFactory implements IProxyRuntimeCommandFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeFilterEventsCommand(java.lang.String[])
	 */
	public IProxyRuntimeFilterEventsCommand newProxyRuntimeFilterEventsCommand(String[] attrs) {
		return new ProxyRuntimeFilterEventsCommand(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeInitCommand(int)
	 */
	public IProxyRuntimeInitCommand newProxyRuntimeInitCommand(int baseId) {
		return new ProxyRuntimeInitCommand(baseId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeModelDefCommand()
	 */
	public IProxyRuntimeModelDefCommand newProxyRuntimeModelDefCommand() {
		return new ProxyRuntimeModelDefCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeStartEventsCommand()
	 */
	public IProxyRuntimeStartEventsCommand newProxyRuntimeStartEventsCommand() {
		return new ProxyRuntimeStartEventsCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeStopEventsCommand()
	 */
	public IProxyRuntimeStopEventsCommand newProxyRuntimeStopEventsCommand() {
		return new ProxyRuntimeStopEventsCommand();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeSubmitJobCommand(java.lang.String[])
	 */
	public IProxyRuntimeSubmitJobCommand newProxyRuntimeSubmitJobCommand(
			String[] attrs) {
		return new ProxyRuntimeSubmitJobCommand(attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommandFactory#newProxyRuntimeTerminateJobCommand(java.lang.String)
	 */
	public IProxyRuntimeTerminateJobCommand newProxyRuntimeTerminateJobCommand(
			String jobId) {
		return new ProxyRuntimeTerminateJobCommand(jobId);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.command.ProxyCommandFactory#toCommand(org.eclipse.ptp.proxy.packet.ProxyPacket)
	 */
	public IProxyCommand toCommand(ProxyPacket packet) {
		IProxyRuntimeCommand	cmd = null;

		IProxyCommand c = super.toCommand(packet);
		if (c != null) {
			return c;
		}
		
		switch (packet.getID()) {
		case IProxyRuntimeCommand.INIT:
			cmd = new ProxyRuntimeInitCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.MODEL_DEF:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.START_EVENTS:
			cmd = new ProxyRuntimeStartEventsCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.STOP_EVENTS:
			cmd = new ProxyRuntimeStopEventsCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.SUBMIT_JOB:
			cmd = new ProxyRuntimeSubmitJobCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.TERMINATE_JOB:
			cmd = new ProxyRuntimeTerminateJobCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.FILTER_EVENTS:
			cmd = new ProxyRuntimeFilterEventsCommand(packet.getTransID(), packet.getArgs());
			break;
		}

		return cmd;
	}
}
