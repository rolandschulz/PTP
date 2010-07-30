package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugGetTypeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugGetTypeCommand(int transID, String[] args) {
		super(GETTYPE, transID, args);
	}

	public ProxyDebugGetTypeCommand(String bits, String expr) {
		super(GETTYPE, bits);
		addArgument(expr);
	}
}
