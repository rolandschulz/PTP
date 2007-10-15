package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetThreadSelectCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetThreadSelectCommand(String bits, int thread) {
		super(SETTHREADSELECT, bits);
		addArgument(thread);
	}
	
	public ProxyDebugSetThreadSelectCommand(int transID, String[] args) {
		super(SETTHREADSELECT, transID, args);
	}
}
