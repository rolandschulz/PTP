package org.eclipse.ptp.debug.external.core.proxy;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyDebugStartSessionComand extends AbstractProxyCommand {

	public ProxyDebugStartSessionComand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return CMD_MODEL_DEF;
	}

	public String toString() {
		return "ProxyModelDefCommand tid=" + getTransactionID();
	}

}
