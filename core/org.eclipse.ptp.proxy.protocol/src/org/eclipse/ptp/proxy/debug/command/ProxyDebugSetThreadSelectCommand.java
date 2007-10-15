package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetThreadSelectCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetThreadSelectCommand(String bits, int thread) {
		super(bits);
		addArgument(thread);
	}

	public int getCommandID() {
		return SETTHREADSELECT;
	}
}
