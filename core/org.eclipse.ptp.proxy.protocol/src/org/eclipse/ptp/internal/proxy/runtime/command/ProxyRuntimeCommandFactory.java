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

package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.ProxyCommandFactory;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeCommandFactory extends ProxyCommandFactory {
	public IProxyCommand toCommand(ProxyPacket packet) {
		IProxyRuntimeCommand	cmd = null;

		IProxyCommand c = super.toCommand(packet);
		if (c != null) {
			return c;
		}
		
		switch (packet.getID()) {
		case IProxyRuntimeCommand.INIT:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.MODEL_DEF:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.START_EVENTS:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.STOP_EVENTS:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.SUBMIT_JOB:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeCommand.TERMINATE_JOB:
			cmd = new ProxyRuntimeModelDefCommand(packet.getTransID(), packet.getArgs());
			break;
		}

		return cmd;
	}
}
