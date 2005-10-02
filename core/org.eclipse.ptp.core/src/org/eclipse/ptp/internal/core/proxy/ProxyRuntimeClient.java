package org.eclipse.ptp.internal.core.proxy;

import org.eclipse.ptp.core.proxy.AbstractProxyClient;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyRuntimeClient extends AbstractProxyClient {

	public ProxyRuntimeClient(String host, int port) {
		super(host, port);
	}

	public IProxyEvent convertEvent(String str) {
		return null;
	}
}
