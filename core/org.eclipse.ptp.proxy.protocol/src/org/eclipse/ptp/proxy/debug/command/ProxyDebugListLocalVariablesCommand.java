package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugListLocalVariablesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugListLocalVariablesCommand(int transID, String[] args) {
		super(LISTLOCALVARIABLES, transID, args);
	}

	public ProxyDebugListLocalVariablesCommand(String bits) {
		super(LISTLOCALVARIABLES, bits);
	}
}
