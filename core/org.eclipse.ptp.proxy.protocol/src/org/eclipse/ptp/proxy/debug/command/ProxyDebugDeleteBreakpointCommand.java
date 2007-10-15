package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDeleteBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDeleteBreakpointCommand(String bits, int bpid) {
		super(DELETEBREAKPOINT, bits);
		addArgument(bpid);
	}
	
	public ProxyDebugDeleteBreakpointCommand(int transID, String[] args) {
		super(DELETEBREAKPOINT, transID, args);
	}
}
