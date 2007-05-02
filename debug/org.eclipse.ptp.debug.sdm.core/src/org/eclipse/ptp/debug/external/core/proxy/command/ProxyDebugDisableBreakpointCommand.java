package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugDisableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDisableBreakpointCommand(IProxyClient client, BitList procs, int bpid) {
		super(client, procs);
		addArgument(bpid);
	}

	public int getCommandID() {
		return DEBUG_CMD_DISABLEBREAKPOINT;
	}
}
