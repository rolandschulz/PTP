package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugDeletePartialExpressionCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugDeletePartialExpressionCommand(int transID, String[] args) {
		super(DELETEPARTIALEXPRESSION, transID, args);
	}

	public ProxyDebugDeletePartialExpressionCommand(String bits, String name) {
		super(DELETEPARTIALEXPRESSION, bits);
		addArgument(name);
	}
}
