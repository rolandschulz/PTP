package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyModelDefCommand extends AbstractProxyCommand {

	public ProxyModelDefCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return CMD_MODEL_DEF;
	}

	public String toString() {
		return "ProxyModelDefCommand tid=" + getTransactionID();
	}

}
