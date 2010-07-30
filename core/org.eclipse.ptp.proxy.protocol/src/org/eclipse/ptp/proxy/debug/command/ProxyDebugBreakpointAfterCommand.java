package org.eclipse.ptp.proxy.debug.command;

public class ProxyDebugBreakpointAfterCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugBreakpointAfterCommand(int transID, String[] args) {
		super(BREAKPOINTAFTER, transID, args);
	}

	public ProxyDebugBreakpointAfterCommand(String bits,
			int bpid, int icount) {
		super(BREAKPOINTAFTER, bits);
		addArgument(bpid);
		addArgument(icount);
	}
}
