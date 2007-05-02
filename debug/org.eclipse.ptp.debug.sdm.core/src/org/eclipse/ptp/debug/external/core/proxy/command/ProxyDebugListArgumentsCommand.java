package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugListArgumentsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListArgumentsCommand(IProxyClient client, BitList procs, 
			int low, int high) {
		super(client, procs);
		addArgument(low);
		addArgument(high);
	}

	public int getCommandID() {
		return DEBUG_CMD_LISTARGUMENTS;
	}
}
