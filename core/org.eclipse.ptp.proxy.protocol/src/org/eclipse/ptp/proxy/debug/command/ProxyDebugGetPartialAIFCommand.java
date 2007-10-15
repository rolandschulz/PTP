package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugGetPartialAIFCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetPartialAIFCommand(String bits, String name, String key, boolean listChildren, boolean express) {
		super(bits);
		addArgument(name);
		addArgument(key);
		addArgument(listChildren);
		addArgument(express);
	}

	public int getCommandID() {
		return GETPARTIALAIF;
	}
}
