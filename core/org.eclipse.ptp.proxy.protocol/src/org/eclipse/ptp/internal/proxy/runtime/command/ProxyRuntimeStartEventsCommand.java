package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeStartEventsCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeStartEventsCommand() {
		super(START_EVENTS);
	}
	
	public ProxyRuntimeStartEventsCommand(int transID, String[] args) {
		super(START_EVENTS, transID, args);
	}
}
