package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyStopEventsCommand extends AbstractProxyCommand {

	public ProxyStopEventsCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return CMD_STOP_EVENTS;
	}

	public String toString() {
		return "ProxyStopEventsCommand tid=" + getTransactionID();
	}

}
