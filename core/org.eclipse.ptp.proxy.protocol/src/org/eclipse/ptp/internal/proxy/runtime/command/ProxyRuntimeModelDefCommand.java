package org.eclipse.ptp.internal.proxy.runtime.command;

import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeCommand;

public class ProxyRuntimeModelDefCommand extends AbstractProxyCommand implements IProxyRuntimeCommand {

	public ProxyRuntimeModelDefCommand() {
	}

	public int getCommandID() {
		return MODEL_DEF;
	}
}
