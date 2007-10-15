package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListArgumentsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListArgumentsCommand(String bits, 
			int low, int high) {
		super(bits);
		addArgument(low);
		addArgument(high);
	}

	public int getCommandID() {
		return LISTARGUMENTS;
	}
}
