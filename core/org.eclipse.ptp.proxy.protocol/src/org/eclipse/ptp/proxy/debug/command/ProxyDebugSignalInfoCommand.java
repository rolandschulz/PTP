package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSignalInfoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSignalInfoCommand(String bits, String arg) {
		super(SIGNALINFO, bits);
		addArgument(arg);
	}
	
	public ProxyDebugSignalInfoCommand(int transID, String[] args) {
		super(SIGNALINFO, transID, args);
	}
}
