package org.eclipse.ptp.bluegene;

import java.io.IOException;

import org.eclipse.ptp.proxy.runtime.server.AbstractProxyRuntimeServer;

public class BGLProxyServer extends AbstractProxyRuntimeServer {

	public BGLProxyServer(String host, int port) {
		super(host, port);
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String host = "localhost";
		int port = 0;
		
		for (String arg : args) {
			if (arg.startsWith("--host=")) {
				host = arg.substring(7);
			} else if (arg.startsWith("--port=")) {
				port = Integer.parseInt(arg.substring(7));
			}
		}
		
		BGLProxyServer server = new BGLProxyServer(host, port);
		
		try {
			server.connect();
		} catch (IOException e) {
			System.err.println("Could not connect to client \"" + host + "\"");
			return;
		}
		
		server.start();
	}
}
