package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListGlobalVariablesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListGlobalVariablesCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return LISTGLOBALVARIABLES;
	}

}
