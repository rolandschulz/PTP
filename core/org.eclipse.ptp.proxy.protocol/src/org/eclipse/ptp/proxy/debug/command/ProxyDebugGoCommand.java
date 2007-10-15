package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugGoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGoCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return GO;
	}
}
