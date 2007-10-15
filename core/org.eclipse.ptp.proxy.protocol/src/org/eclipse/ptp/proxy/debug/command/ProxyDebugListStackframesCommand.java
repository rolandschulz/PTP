package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListStackframesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListStackframesCommand(String bits, 
			int low, int high) {
		super(LISTSTACKFRAMES, bits);
		addArgument(low);
		addArgument(high);
	}
	
	public ProxyDebugListStackframesCommand(int transID, String[] args) {
		super(LISTSTACKFRAMES, transID, args);
	}
}
