package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyRuntimeTerminateJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeTerminateJobCommand(IProxyClient client, String jobId) {
		super(client);
		addArgument(jobId);
	}

	public int getCommandID() {
		return TERMINATE_JOB;
	}
}
