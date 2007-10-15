package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugGetPartialAIFCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugGetPartialAIFCommand(String bits, String name, String key, boolean listChildren, boolean express) {
		super(GETPARTIALAIF, bits);
		addArgument(name);
		addArgument(key);
		addArgument(listChildren);
		addArgument(express);
	}
	
	public ProxyDebugGetPartialAIFCommand(int transID, String[] args) {
		super(GETPARTIALAIF, transID, args);
	}
}
