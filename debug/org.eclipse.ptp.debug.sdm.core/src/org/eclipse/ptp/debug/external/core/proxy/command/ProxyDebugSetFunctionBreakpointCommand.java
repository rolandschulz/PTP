package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSetFunctionBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetFunctionBreakpointCommand(IProxyClient client, BitList procs, 
			int bpid, boolean isTemporary, boolean isHardware, String file, String func, 
			String expression, int ignoreCount, int tid) {
		super(client, procs);
		addArgument(bpid);
		addArgument(isTemporary);
		addArgument(isHardware);
		addArgument(file);
		addArgument(func);
		addArgument(expression);
		addArgument(ignoreCount);
		addArgument(tid);
	}

	public int getCommandID() {
		return SETFUNCBREAKPOINT;
	}
}
