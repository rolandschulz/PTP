package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeSubmitJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeSubmitJobCommand(String[] args) {
		addArguments(args);
	}

	public int getCommandID() {
		return SUBMIT_JOB;
	}
}
