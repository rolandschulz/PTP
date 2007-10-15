package org.eclipse.ptp.proxy.debug.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;

public class ProxyDebugStartSessionCommand extends AbstractProxyCommand implements IProxyDebugCommand {
	
	public ProxyDebugStartSessionCommand(String bits, String path, 
			String dir, String[] args) {
		addArgument(bits);
		addArgument(path);
		addArgument(dir);
		addArguments(args);
	}

	public int getCommandID() {
		return STARTSESSION;
	}
}
