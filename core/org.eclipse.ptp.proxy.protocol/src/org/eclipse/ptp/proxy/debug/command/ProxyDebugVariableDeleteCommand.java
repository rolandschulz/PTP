package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugVariableDeleteCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugVariableDeleteCommand(String bits, String name) {
		super(VARIABLEDELETE, bits);
		addArgument(name);
	}
	
	public ProxyDebugVariableDeleteCommand(int transID, String[] args) {
		super(VARIABLEDELETE, transID, args);
	}
}
