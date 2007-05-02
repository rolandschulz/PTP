package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSetCurrentStackframeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetCurrentStackframeCommand(IProxyClient client, BitList procs, int level) {
		super(client, procs);
		addArgument(level);
	}

	public int getCommandID() {
		return SETCURRENTSTACKFRAME;
	}
}
