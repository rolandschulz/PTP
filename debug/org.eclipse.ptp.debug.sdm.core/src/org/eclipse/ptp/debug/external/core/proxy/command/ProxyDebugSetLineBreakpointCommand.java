package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSetLineBreakpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetLineBreakpointCommand(IProxyClient client, BitList procs, 
			int bpid, boolean isTemporary, boolean isHardware, String file, int line, 
			String expression, int ignoreCount, int tid) {
		super(client, procs);
		addArgument(bpid);
		addArgument(isTemporary);
		addArgument(isHardware);
		addArgument(file);
		addArgument(line);
		addArgument(expression);
		addArgument(ignoreCount);
		addArgument(tid);
	}

	public int getCommandID() {
		return SETLINEBREAKPOINT;
	}
}
