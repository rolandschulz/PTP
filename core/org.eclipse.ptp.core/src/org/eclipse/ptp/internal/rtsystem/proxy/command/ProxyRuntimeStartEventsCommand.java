package org.eclipse.ptp.internal.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.rtsystem.proxy.command.IProxyRuntimeCommand;

public class ProxyRuntimeStartEventsCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeStartEventsCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return START_EVENTS;
	}
}
