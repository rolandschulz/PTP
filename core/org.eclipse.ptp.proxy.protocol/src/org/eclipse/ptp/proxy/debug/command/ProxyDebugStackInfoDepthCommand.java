package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugStackInfoDepthCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugStackInfoDepthCommand(int transID, String[] args) {
		super(STACKINFODEPTH, transID, args);
	}

	public ProxyDebugStackInfoDepthCommand(String bits) {
		super(STACKINFODEPTH, bits);
	}
}
