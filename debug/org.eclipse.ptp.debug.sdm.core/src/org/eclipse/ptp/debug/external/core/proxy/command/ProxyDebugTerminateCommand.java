package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugTerminateCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugTerminateCommand(IProxyClient client, BitList procs) {
		super(client, procs);
	}

	public int getCommandID() {
		return TERMINATE;
	}
}
