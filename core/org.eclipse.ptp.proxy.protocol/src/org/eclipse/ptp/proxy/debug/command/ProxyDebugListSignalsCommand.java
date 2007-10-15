package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListSignalsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListSignalsCommand(String bits, String name) {
		super(LISTSIGNALS, bits);
		addArgument(name);
	}
	
	public ProxyDebugListSignalsCommand(int transID, String[] args) {
		super(LISTSIGNALS, transID, args);
	}
}
