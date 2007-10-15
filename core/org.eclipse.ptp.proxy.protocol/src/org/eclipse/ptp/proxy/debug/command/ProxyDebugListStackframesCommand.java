package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListStackframesCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListStackframesCommand(String bits, 
			int low, int high) {
		super(bits);
		addArgument(low);
		addArgument(high);
	}

	public int getCommandID() {
		return LISTSTACKFRAMES;
	}
}
