package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugDeleteBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugDeleteBreakpointCommand(String bits, int bpid) {
		super(bits);
		addArgument(bpid);
	}

	public int getCommandID() {
		return DELETEBREAKPOINT;
	}
}
