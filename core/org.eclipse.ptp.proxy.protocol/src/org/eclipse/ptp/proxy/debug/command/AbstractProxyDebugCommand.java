package org.eclipse.ptp.proxy.debug.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;

public abstract class AbstractProxyDebugCommand extends AbstractProxyCommand {
	
	public AbstractProxyDebugCommand(int eventID) {
		super(eventID);
	}

	public AbstractProxyDebugCommand(int eventID, String bits) {
		super(eventID);
		addArgument(bits);
	}

	public AbstractProxyDebugCommand(int eventID, int transactionID, String[] args) {
		super(eventID, transactionID, args);
	}
}
