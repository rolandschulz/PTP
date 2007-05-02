package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugSetWatchpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetWatchpointCommand(IProxyClient client, BitList procs, 
			int bpid, String expression, boolean isAccess, boolean isRead, String condition, 
			int ignoreCount) {
		super(client, procs);
		addArgument(bpid);
		addArgument(expression);
		addArgument(isAccess);
		addArgument(isRead);
		addArgument(condition);
		addArgument(ignoreCount);
	}

	public int getCommandID() {
		return SETWATCHPOINT;
	}
}
