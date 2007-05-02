package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyRuntimeModelDefCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeModelDefCommand(IProxyClient client) {
		super(client);
	}

	public int getCommandID() {
		return MODEL_DEF;
	}
}
