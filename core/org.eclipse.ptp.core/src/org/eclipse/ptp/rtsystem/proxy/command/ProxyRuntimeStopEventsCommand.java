package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyRuntimeStopEventsCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeStopEventsCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return STOP_EVENTS;
	}
}
