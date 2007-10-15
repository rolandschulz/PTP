package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetCurrentStackframeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetCurrentStackframeCommand(String bits, int level) {
		super(bits);
		addArgument(level);
	}

	public int getCommandID() {
		return SETCURRENTSTACKFRAME;
	}
}
