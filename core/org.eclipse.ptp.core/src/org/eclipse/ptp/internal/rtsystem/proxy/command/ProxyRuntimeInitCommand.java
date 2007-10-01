package org.eclipse.ptp.internal.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.rtsystem.proxy.command.IProxyRuntimeCommand;

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
