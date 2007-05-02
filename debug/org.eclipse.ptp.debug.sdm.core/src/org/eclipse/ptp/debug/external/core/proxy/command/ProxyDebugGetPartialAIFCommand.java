package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugGetPartialAIFCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetPartialAIFCommand(IProxyClient client, BitList procs, String name, String key, boolean listChildren, boolean express) {
		super(client, procs);
		addArgument(name);
		addArgument(key);
		addArgument(listChildren);
		addArgument(express);
	}

	public int getCommandID() {
		return DEBUG_CMD_GETPARTIALAIF;
	}
}
