package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugSignalInfoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugSignalInfoCommand(String bits, String arg) {
		super(bits);
		addArgument(arg);
	}

	public int getCommandID() {
		return SIGNALINFO;
	}
}
