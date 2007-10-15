package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugListInfoThreadsCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugListInfoThreadsCommand(String bits) {
		super(bits);
	}

	public int getCommandID() {
		return LISTINFOTHREADS;
	}

}
