package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetLineBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetLineBreakpointCommand(String bits, 
			int bpid, boolean isTemporary, boolean isHardware, String file, int line, 
			String expression, int ignoreCount, int tid) {
		super(SETLINEBREAKPOINT, bits);
		addArgument(bpid);
		addArgument(isTemporary);
		addArgument(isHardware);
		addArgument(file);
		addArgument(line);
		addArgument(expression);
		addArgument(ignoreCount);
		addArgument(tid);
	}
	
	public ProxyDebugSetLineBreakpointCommand(int transID, String[] args) {
		super(SETLINEBREAKPOINT, transID, args);
	}
}
