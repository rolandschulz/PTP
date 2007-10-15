package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetFunctionBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetFunctionBreakpointCommand(String bits, 
			int bpid, boolean isTemporary, boolean isHardware, String file, String func, 
			String expression, int ignoreCount, int tid) {
		super(SETFUNCBREAKPOINT, bits);
		addArgument(bpid);
		addArgument(isTemporary);
		addArgument(isHardware);
		addArgument(file);
		addArgument(func);
		addArgument(expression);
		addArgument(ignoreCount);
		addArgument(tid);
	}
	
	public ProxyDebugSetFunctionBreakpointCommand(int transID, String[] args) {
		super(SETFUNCBREAKPOINT, transID, args);
	}
}
