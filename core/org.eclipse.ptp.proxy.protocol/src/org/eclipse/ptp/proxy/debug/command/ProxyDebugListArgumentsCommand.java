package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListArgumentsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListArgumentsCommand(String bits, 
			int low, int high) {
		super(LISTARGUMENTS, bits);
		addArgument(low);
		addArgument(high);
	}
	
	public ProxyDebugListArgumentsCommand(int transID, String[] args) {
		super(LISTARGUMENTS, transID, args);
	}
}
