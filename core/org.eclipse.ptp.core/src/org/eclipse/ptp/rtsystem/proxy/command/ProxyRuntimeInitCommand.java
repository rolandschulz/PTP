package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyRuntimeInitCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {
	
	public ProxyRuntimeInitCommand(IProxyClient client, int baseId) {
		super(client);
		addArgument(IProxyClient.WIRE_PROTOCOL_VERSION);
		addArgument(baseId);
	}

	public int getCommandID() {
		return INIT;
	}
}
