package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugGetTypeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetTypeCommand(IProxyClient client, BitList procs, String expr) {
		super(client, procs);
		addArgument(expr);
	}

	public int getCommandID() {
		return GETTYPE;
	}
}
