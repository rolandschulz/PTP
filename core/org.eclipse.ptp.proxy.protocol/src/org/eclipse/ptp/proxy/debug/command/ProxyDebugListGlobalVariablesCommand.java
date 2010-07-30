package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugListGlobalVariablesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugListGlobalVariablesCommand(int transID, String[] args) {
		super(LISTGLOBALVARIABLES, transID, args);
	}

	public ProxyDebugListGlobalVariablesCommand(String bits) {
		super(LISTGLOBALVARIABLES, bits);
	}
}
