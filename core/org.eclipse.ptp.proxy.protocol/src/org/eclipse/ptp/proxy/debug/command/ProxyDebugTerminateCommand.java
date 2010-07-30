package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugTerminateCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugTerminateCommand(int transID, String[] args) {
		super(TERMINATE, transID, args);
	}

	public ProxyDebugTerminateCommand(String bits) {
		super(TERMINATE, bits);
	}
}
