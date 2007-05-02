package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugListSignalsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListSignalsCommand(IProxyClient client, BitList procs, String name) {
		super(client, procs);
		addArgument(name);
	}

	public int getCommandID() {
		return DEBUG_CMD_LISTSIGNALS;
	}
}
