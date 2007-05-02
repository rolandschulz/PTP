package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyRuntimeSubmitJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeSubmitJobCommand(IProxyClient client, String[] args) {
		super(client);
		addArguments(args);
	}

	public int getCommandID() {
		return SUBMIT_JOB;
	}
}
