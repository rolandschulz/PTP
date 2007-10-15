package org.eclipse.ptp.proxy.debug.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;

public abstract class AbstractProxyDebugCommand extends AbstractProxyCommand {
	
	protected AbstractProxyDebugCommand(String bits) {
		addArgument(bits);
	}
}
