package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugStackInfoDepthCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStackInfoDepthCommand(String bits) {
		super(STACKINFODEPTH, bits);
	}
	
	public ProxyDebugStackInfoDepthCommand(int transID, String[] args) {
		super(STACKINFODEPTH, transID, args);
	}
}
