package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSignalInfoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSignalInfoCommand(IProxyClient client, BitList procs, String arg) {
		super(client, procs);
		addArgument(arg);
	}

	public int getCommandID() {
		return DEBUG_CMD_SIGNALINFO;
	}
}
