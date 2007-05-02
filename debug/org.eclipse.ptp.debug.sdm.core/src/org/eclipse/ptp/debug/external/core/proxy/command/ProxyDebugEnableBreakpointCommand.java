package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugEnableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugEnableBreakpointCommand(IProxyClient client, BitList procs, int bpid) {
		super(client, procs);
		addArgument(bpid);
	}

	public int getCommandID() {
		return DEBUG_CMD_ENABLEBREAKPOINT;
	}
}
