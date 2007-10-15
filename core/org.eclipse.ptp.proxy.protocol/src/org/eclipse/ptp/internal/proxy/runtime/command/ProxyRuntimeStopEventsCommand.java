package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeStopEventsCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeStopEventsCommand() {
		super(STOP_EVENTS);
	}
	
	public ProxyRuntimeStopEventsCommand(int transID, String[] args) {
		super(STOP_EVENTS, transID, args);
	}
}
