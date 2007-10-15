package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugEnableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugEnableBreakpointCommand(String bits, int bpid) {
		super(bits);
		addArgument(bpid);
	}

	public int getCommandID() {
		return ENABLEBREAKPOINT;
	}
}
