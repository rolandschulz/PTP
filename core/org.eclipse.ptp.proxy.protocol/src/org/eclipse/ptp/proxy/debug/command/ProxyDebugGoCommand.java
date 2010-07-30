package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugGoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugGoCommand(int transID, String[] args) {
		super(GO, transID, args);
	}

	public ProxyDebugGoCommand(String bits) {
		super(GO, bits);
	}
}
