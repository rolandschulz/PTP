package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyDebugStartSessionCommand extends AbstractProxyCommand implements IProxyDebugCommand {
	
	public ProxyDebugStartSessionCommand(IProxyClient client, String prog, String path, String dir, String[] args) {
		super(client);
		addArgument(prog);
		addArgument(path);
		addArgument(dir);
		addArguments(args);
	}

	public int getCommandID() {
		return DEBUG_CMD_STARTSESSION;
	}
}
