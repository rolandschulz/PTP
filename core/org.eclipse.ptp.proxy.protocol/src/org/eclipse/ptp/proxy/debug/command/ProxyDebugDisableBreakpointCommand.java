package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDisableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDisableBreakpointCommand(String bits, int bpid) {
		super(DISABLEBREAKPOINT, bits);
		addArgument(bpid);
	}
	
	public ProxyDebugDisableBreakpointCommand(int transID, String[] args) {
		super(DISABLEBREAKPOINT, transID, args);
	}
}
