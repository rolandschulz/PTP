package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugInterruptCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugInterruptCommand(int transID, String[] args) {
		super(INTERRUPT, transID, args);
	}

	public ProxyDebugInterruptCommand(String bits) {
		super(INTERRUPT, bits);
	}
}
