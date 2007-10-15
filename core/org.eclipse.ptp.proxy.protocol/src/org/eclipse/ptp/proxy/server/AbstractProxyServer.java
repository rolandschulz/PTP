package org.eclipse.ptp.proxy.server;

import java.io.IOException;
import java.net.Socket;

public abstract class AbstractProxyServer {

	private Socket sessSocket;
	
	public void sessionConnect(String host, int port, int timeout) throws IOException {
		sessSocket = new Socket(host, port);
	}
}
