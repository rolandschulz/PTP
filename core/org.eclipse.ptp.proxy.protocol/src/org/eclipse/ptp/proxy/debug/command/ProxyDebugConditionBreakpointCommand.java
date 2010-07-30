package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugConditionBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugConditionBreakpointCommand(int transID, String[] args) {
		super(CONDITIONBREAKPOINT, transID, args);
	}

	public ProxyDebugConditionBreakpointCommand(String bits,
			int bpid, String expr) {
		super(CONDITIONBREAKPOINT, bits);
		addArgument(bpid);
		addArgument(expr);
	}
}
