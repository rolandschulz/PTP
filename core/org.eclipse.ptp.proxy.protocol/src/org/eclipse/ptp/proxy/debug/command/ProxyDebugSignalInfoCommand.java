package org.eclipse.ptp.proxy.debug.command;

/**
 * @deprecated
 */
@Deprecated
public class ProxyDebugSignalInfoCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {

	public ProxyDebugSignalInfoCommand(int transID, String[] args) {
		super(SIGNALINFO, transID, args);
	}

	public ProxyDebugSignalInfoCommand(String bits, String arg) {
		super(SIGNALINFO, bits);
		addArgument(arg);
	}
}
