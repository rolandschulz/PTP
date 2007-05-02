package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugListInfoThreadsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListInfoThreadsCommand(IProxyClient client, BitList procs) {
		super(client, procs);
	}

	public int getCommandID() {
		return DEBUG_CMD_LISTINFOTHREADS;
	}

}
