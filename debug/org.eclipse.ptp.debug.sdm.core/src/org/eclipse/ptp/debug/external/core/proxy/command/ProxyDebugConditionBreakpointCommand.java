package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugConditionBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugConditionBreakpointCommand(IProxyClient client, BitList procs, 
			int bpid, String expr) {
		super(client, procs);
		addArgument(bpid);
		addArgument(expr);
	}

	public int getCommandID() {
		return CONDITIONBREAKPOINT;
	}
}
