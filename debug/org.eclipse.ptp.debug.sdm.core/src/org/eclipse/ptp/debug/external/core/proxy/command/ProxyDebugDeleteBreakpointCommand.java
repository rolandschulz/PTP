package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugDeleteBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDeleteBreakpointCommand(IProxyClient client, BitList procs, int bpid) {
		super(client, procs);
		addArgument(bpid);
	}

	public int getCommandID() {
		return DELETEBREAKPOINT;
	}
}
