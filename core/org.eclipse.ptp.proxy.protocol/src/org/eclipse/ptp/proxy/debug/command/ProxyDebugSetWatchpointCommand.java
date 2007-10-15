package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSetWatchpointCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSetWatchpointCommand(String bits, 
			int bpid, String expression, boolean isAccess, boolean isRead, String condition, 
			int ignoreCount) {
		super(SETWATCHPOINT, bits);
		addArgument(bpid);
		addArgument(expression);
		addArgument(isAccess);
		addArgument(isRead);
		addArgument(condition);
		addArgument(ignoreCount);
	}
	
	public ProxyDebugSetWatchpointCommand(int transID, String[] args) {
		super(SETWATCHPOINT, transID, args);
	}
}
