package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugStackInfoDepthCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStackInfoDepthCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return STACKINFODEPTH;
	}
}
