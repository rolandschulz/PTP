package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListLocalVariablesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListLocalVariablesCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return LISTLOCALVARIABLES;
	}

}
