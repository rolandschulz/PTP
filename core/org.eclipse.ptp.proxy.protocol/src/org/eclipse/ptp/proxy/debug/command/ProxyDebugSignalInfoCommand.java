package org.eclipse.ptp.proxy.debug.command;

/**
 * @deprecated
 */
public class ProxyDebugSignalInfoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSignalInfoCommand(String bits, String arg) {
		super(SIGNALINFO, bits);
		addArgument(arg);
	}
	
	public ProxyDebugSignalInfoCommand(int transID, String[] args) {
		super(SIGNALINFO, transID, args);
	}
}
