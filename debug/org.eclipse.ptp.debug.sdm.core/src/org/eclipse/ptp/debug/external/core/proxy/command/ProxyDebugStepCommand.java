package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugStepCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStepCommand(IProxyClient client, BitList procs, 
			int count, int type) {
		super(client, procs);
		addArgument(count);
		addArgument(type);
	}

	public int getCommandID() {
		return DEBUG_CMD_STEP;
	}
}
