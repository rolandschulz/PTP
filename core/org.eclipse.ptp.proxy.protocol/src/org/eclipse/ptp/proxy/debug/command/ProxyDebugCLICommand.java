package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugCLICommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugCLICommand(String bits, String arg) {
		super(bits);
		addArgument(arg);
	}

	public int getCommandID() {
		return CLIHANDLE;
	}
}
