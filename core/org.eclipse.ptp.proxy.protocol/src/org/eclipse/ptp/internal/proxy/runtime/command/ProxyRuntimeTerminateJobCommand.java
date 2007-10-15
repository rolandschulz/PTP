package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeTerminateJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeTerminateJobCommand(String jobId) {
		super(TERMINATE_JOB);
		addArgument(jobId);
	}

	public ProxyRuntimeTerminateJobCommand(int transID, String[] args) {
		super(TERMINATE_JOB, transID, args);
	}
}
