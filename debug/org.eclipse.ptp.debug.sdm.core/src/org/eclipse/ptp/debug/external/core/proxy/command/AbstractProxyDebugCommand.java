package org.eclipse.ptp.debug.external.core.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.core.util.BitList;

public abstract class AbstractProxyDebugCommand extends AbstractProxyCommand {
	
	protected AbstractProxyDebugCommand(IProxyClient client, BitList procs) {
		super(client);
	}

	protected void addArgument(BitList bits) {
		addArgument(bits.toString());
	}
}
