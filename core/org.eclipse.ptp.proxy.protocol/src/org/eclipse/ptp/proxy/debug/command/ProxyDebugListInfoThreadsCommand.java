package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListInfoThreadsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListInfoThreadsCommand(String bits) {
		super(LISTINFOTHREADS, bits);
	}
	
	public ProxyDebugListInfoThreadsCommand(int transID, String[] args) {
		super(LISTINFOTHREADS, transID, args);
	}
}
