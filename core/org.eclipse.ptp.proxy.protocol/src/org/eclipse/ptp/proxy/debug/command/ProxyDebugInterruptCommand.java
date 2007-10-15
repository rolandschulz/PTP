package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugInterruptCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugInterruptCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return INTERRUPT;
	}
}
