package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugBreakpointAfterCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugBreakpointAfterCommand(IProxyClient client, BitList procs, 
			int bpid, int icount) {
		super(client, procs);
		addArgument(bpid);
		addArgument(icount);
	}

	public int getCommandID() {
		return DEBUG_CMD_BREAKPOINTAFTER;
	}
}
