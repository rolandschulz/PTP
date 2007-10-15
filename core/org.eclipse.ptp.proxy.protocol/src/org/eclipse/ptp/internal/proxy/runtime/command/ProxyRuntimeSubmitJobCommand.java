package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeSubmitJobCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeSubmitJobCommand(String[] args) {
		super(SUBMIT_JOB);
		addArguments(args);
	}

	public ProxyRuntimeSubmitJobCommand(int transID, String[] args) {
		super(SUBMIT_JOB, transID, args);
	}
}
