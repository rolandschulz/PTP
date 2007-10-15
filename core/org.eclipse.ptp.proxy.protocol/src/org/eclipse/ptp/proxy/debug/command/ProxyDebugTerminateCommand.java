package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugTerminateCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugTerminateCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return TERMINATE;
	}
}
