package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugGetParitalAIFCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetParitalAIFCommand(IProxyClient client, BitList procs, String arg) {
		super(client, procs);
		addArgument(arg);
	}

	public int getCommandID() {
		return DEBUG_CMD_SIGNALINFO;
	}
}
