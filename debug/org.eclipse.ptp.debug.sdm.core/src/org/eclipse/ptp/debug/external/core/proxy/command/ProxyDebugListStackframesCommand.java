package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugListStackframesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListStackframesCommand(IProxyClient client, BitList procs, 
			int low, int high) {
		super(client, procs);
		addArgument(low);
		addArgument(high);
	}

	public int getCommandID() {
		return LISTSTACKFRAMES;
	}
}
