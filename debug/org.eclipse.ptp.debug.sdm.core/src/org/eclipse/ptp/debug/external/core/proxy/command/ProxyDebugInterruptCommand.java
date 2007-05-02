package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugInterruptCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugInterruptCommand(IProxyClient client, BitList procs) {
		super(client, procs);
	}

	public int getCommandID() {
		return DEBUG_CMD_INTERRUPT;
	}
}
