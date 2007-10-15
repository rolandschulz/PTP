package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeTerminateJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeTerminateJobCommand(String jobId) {
		addArgument(jobId);
	}

	public int getCommandID() {
		return TERMINATE_JOB;
	}
}
