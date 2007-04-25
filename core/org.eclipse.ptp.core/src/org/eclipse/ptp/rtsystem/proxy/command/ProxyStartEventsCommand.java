package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyStartEventsCommand extends AbstractProxyCommand {

	public ProxyStartEventsCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return CMD_START_EVENTS;
	}

	public String toString() {
		return "ProxyStartEventsCommand tid=" + getTransactionID();
	}

}
