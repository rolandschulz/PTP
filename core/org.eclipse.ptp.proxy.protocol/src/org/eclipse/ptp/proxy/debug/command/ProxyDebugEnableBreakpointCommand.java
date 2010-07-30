package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugEnableBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugEnableBreakpointCommand(int transID, String[] args) {
		super(ENABLEBREAKPOINT, transID, args);
	}

	public ProxyDebugEnableBreakpointCommand(String bits, int bpid) {
		super(ENABLEBREAKPOINT, bits);
		addArgument(bpid);
	}
}
