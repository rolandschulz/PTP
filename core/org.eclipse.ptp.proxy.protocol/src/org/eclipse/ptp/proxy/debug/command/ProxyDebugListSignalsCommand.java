package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListSignalsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListSignalsCommand(String bits, String name) {
		super(bits);
		addArgument(name);
	}

	public int getCommandID() {
		return LISTSIGNALS;
	}
}
