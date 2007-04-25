package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxySubmitJobCommand extends AbstractProxyCommand {

	public ProxySubmitJobCommand(IProxyClient client, String[] args) {
		super(client, args);
	}

	public int getCommandID() {
		return CMD_SUBMIT_JOB;
	}

	public String toString() {
		String str = "ProxySubmitJobCommand";
		for (String arg : args) {
			str += " " + arg;
		}
		return str;
	}

}
