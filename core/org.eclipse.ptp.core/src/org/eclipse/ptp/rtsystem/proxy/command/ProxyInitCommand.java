package org.eclipse.ptp.rtsystem.proxy.command;

import org.eclipse.ptp.core.proxy.IProxyClient;
import org.eclipse.ptp.core.proxy.command.AbstractProxyCommand;

public class ProxyInitCommand extends AbstractProxyCommand {
	
	public ProxyInitCommand(IProxyClient client, int baseId) {
		super(client);
		addArgument(IProxyClient.WIRE_PROTOCOL_VERSION);
		addArgument(baseId);
	}

	public int getCommandID() {
		return CMD_INIT;
	}
	
	public String toString() {
		String str =  "ProxyInitCommand tid=" + getTransactionID();
		
		for (String arg : args) {
			str += " " + arg;
		}
		return str;
	}

}
