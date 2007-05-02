package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugCLICommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugCLICommand(IProxyClient client, BitList procs, String arg) {
		super(client, procs);
		addArgument(arg);
	}

	public int getCommandID() {
		return CLIHANDLE;
	}
}
