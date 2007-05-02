package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSetThreadSelectCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetThreadSelectCommand(IProxyClient client, BitList procs, int thread) {
		super(client, procs);
		addArgument(thread);
	}

	public int getCommandID() {
		return DEBUG_CMD_SETTHREADSELECT;
	}
}
