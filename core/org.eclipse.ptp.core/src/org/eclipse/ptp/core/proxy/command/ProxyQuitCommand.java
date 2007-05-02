package org.eclipse.ptp.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyQuitCommand extends AbstractProxyCommand {

	public ProxyQuitCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return QUIT;
	}

	public String toString() {
		return "ProxyQuitCommand tid=" + getTransactionID();
	}

}
