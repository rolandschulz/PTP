package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugStepCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStepCommand(String bits, 
			int count, int type) {
		super(STEP, bits);
		addArgument(count);
		addArgument(type);
	}
	
	public ProxyDebugStepCommand(int transID, String[] args) {
		super(STEP, transID, args);
	}
}
