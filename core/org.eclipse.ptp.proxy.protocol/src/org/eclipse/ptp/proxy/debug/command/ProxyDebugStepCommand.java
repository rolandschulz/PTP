package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugStepCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStepCommand(String bits, 
			int count, int type) {
		super(bits);
		addArgument(count);
		addArgument(type);
	}

	public int getCommandID() {
		return STEP;
	}
}
