package org.eclipse.ptp.proxy.runtime.server;

import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.server.AbstractProxyServer;

public abstract class AbstractProxyRuntimeServer extends AbstractProxyServer {

	public AbstractProxyRuntimeServer(String host, int port) {
		super(host, port, new ProxyRuntimeCommandFactory());
	}
}
