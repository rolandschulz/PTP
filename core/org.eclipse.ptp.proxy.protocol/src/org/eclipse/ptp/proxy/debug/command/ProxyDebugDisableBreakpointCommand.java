package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDisableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDisableBreakpointCommand(String bits, int bpid) {
		super(bits);
		addArgument(bpid);
	}

	public int getCommandID() {
		return DISABLEBREAKPOINT;
	}
}
