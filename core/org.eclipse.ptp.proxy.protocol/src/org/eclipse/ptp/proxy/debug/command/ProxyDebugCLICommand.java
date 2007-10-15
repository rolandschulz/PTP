package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugCLICommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugCLICommand(String bits, String arg) {
		super(CLIHANDLE, bits);
		addArgument(arg);
	}
	
	public ProxyDebugCLICommand(int transID, String[] args) {
		super(CLIHANDLE, transID, args);
	}
}
