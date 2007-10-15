package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugVariableDeleteCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugVariableDeleteCommand(String bits, String name) {
		super(bits);
		addArgument(name);
	}

	public int getCommandID() {
		return VARIABLEDELETE;
	}
}
