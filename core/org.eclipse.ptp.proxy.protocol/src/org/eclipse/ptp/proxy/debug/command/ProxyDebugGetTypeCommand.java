package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugGetTypeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetTypeCommand(String bits, String expr) {
		super(bits);
		addArgument(expr);
	}

	public int getCommandID() {
		return GETTYPE;
	}
}
