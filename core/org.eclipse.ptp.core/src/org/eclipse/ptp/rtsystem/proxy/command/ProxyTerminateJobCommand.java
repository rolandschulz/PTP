package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyTerminateJobCommand extends AbstractProxyCommand {

	public ProxyTerminateJobCommand(IProxyClient client, String jobId) {
		super(client, new String[] {jobId});
	}

	public int getCommandID() {
		return CMD_TERM_JOB;
	}

	public String toString() {
		String str = "ProxyTerminateJobCommand";
		for (String arg : args) {
			str += " " + arg;
		}
		return str;
	}

}
