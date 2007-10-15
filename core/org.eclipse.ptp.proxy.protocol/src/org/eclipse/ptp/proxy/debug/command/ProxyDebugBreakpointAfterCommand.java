package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugBreakpointAfterCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugBreakpointAfterCommand(String bits, 
			int bpid, int icount) {
		super(bits);
		addArgument(bpid);
		addArgument(icount);
	}

	public int getCommandID() {
		return BREAKPOINTAFTER;
	}
}
