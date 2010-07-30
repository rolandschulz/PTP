package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugSetCurrentStackframeCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugSetCurrentStackframeCommand(int transID, String[] args) {
		super(SETCURRENTSTACKFRAME, transID, args);
	}

	public ProxyDebugSetCurrentStackframeCommand(String bits, int level) {
		super(SETCURRENTSTACKFRAME, bits);
		addArgument(level);
	}
}
