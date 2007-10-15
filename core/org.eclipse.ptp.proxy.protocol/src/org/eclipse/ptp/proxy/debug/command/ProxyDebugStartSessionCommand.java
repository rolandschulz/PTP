package org.eclipse.ptp.proxy.debug.command;


public class ProxyDebugStartSessionCommand extends AbstractProxyDebugCommand implements IProxyDebugCommand {
	
	public ProxyDebugStartSessionCommand(String prog, String path, 
			String dir, String[] args) {
		super(STARTSESSION);
		addArgument(prog);
		addArgument(path);
		addArgument(dir);
		addArguments(args);
	}
	
	public ProxyDebugStartSessionCommand(int transID, String[] args) {
		super(STARTSESSION, transID, args);
	}
}
