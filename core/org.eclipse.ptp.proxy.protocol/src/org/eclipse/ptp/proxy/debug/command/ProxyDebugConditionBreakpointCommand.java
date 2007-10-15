package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugConditionBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugConditionBreakpointCommand(String bits, 
			int bpid, String expr) {
		super(bits);
		addArgument(bpid);
		addArgument(expr);
	}

	public int getCommandID() {
		return CONDITIONBREAKPOINT;
	}
}
