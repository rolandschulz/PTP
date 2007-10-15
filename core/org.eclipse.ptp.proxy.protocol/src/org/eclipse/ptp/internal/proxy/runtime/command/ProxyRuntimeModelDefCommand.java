package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeModelDefCommand 
	extends AbstractProxyCommand implements IProxyRuntimeCommand {
	
	public ProxyRuntimeModelDefCommand() {
		super(MODEL_DEF);
	}
	
	public ProxyRuntimeModelDefCommand(int transID, String[] args) {
		super(MODEL_DEF, transID, args);
	}
}