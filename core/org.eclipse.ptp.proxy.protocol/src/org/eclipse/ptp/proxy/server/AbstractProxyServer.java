/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.proxy.server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyCommandFactory;
import org.eclipse.ptp.proxy.command.IProxyCommandListener;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public abstract class AbstractProxyServer implements IProxyServer {
	protected enum ServerState {
		INIT, DISCOVERY, NORMAL, SUSPENDED, SHUTDOWN
	}

	public static final int MAX_ERRORS = 5;

	protected ServerState state = ServerState.INIT;

	private String sessHost;
	private int sessPort;
	// private Socket sessSocket;
	protected ReadableByteChannel sessInput;
	protected WritableByteChannel sessOutput;
	private IProxyCommandFactory proxyCommandFactory;
	private Thread commandThread;
	private List<IProxyCommandListener> listeners = Collections.synchronizedList(new ArrayList<IProxyCommandListener>());

	public AbstractProxyServer(String host, int port, IProxyCommandFactory factory) {
		this.sessHost = host;
		this.sessPort = port;
		this.proxyCommandFactory = factory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.server.IProxyServer#addListener(org.eclipse.ptp
	 * .proxy.command.IProxyCommandListener)
	 */
	public void addListener(IProxyCommandListener listener) {
		listeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.server.IProxyServer#addListener(org.eclipse.ptp
	 * .proxy.command.IProxyCommandListener)
	 */
	public void removeListener(IProxyCommandListener listener) {
		listeners.remove(listener);
	}

	/**
	 * @throws IOException
	 */
	public void connect() throws IOException {
		// sessSocket = new Socket(sessHost, sessPort);
		SocketChannel channel = SocketChannel.open();
		channel.connect(new InetSocketAddress(sessHost, sessPort));
		sessInput = channel;
		sessOutput = channel;
	}

	/**
	 * Start a thread to process commands from the proxy by repeatedly calling
	 * sessionProgress().
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		commandThread = new Thread("Proxy Server Command Thread") { //$NON-NLS-1$
			public void run() {
				boolean error = false;
				int errorCount = 0;

				System.out.println("server command thread starting..."); //$NON-NLS-1$
				try {
					while (errorCount < MAX_ERRORS && !isInterrupted()) {
						synchronized (state) {
							if (state == ServerState.SHUTDOWN) {
								break;
							}
						}
						if (!sessionProgress()) {
							errorCount++;
						}
					}
				} catch (IOException e) {
					synchronized (state) {
						if (!isInterrupted() && state != ServerState.SHUTDOWN) {
							error = true;
							System.out.println("event thread IOException . . . " + e.getMessage()); //$NON-NLS-1$
						}
					}
				}

				if (errorCount >= MAX_ERRORS) {
					error = true;
				}

				try {
					sessInput.close();
				} catch (IOException e) {
				}

				synchronized (state) {
					state = ServerState.SHUTDOWN;
				}

				System.out.println("server thread exited"); //$NON-NLS-1$
				if (error) {
					System.out.println(" due to errors"); //$NON-NLS-1$
				} else {
					System.out.println(" normally"); //$NON-NLS-1$
				}
			}
		};
		commandThread.start();
		try {
			runStateMachine();
			commandThread.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Process commands from the wire
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean sessionProgress() throws IOException {
		ProxyPacket packet = new ProxyPacket();
		System.out.print("sessionProgress: "); //$NON-NLS-1$
		if (!packet.read(sessInput)) {
			System.out.println("false"); //$NON-NLS-1$
			return false;
		}
		System.out.println(packet.getID() + "," + packet.getTransID() + "," + packet.getArgs()); //$NON-NLS-1$ //$NON-NLS-2$
		/*
		 * Now convert the event into an IProxyEvent
		 */
		IProxyCommand cmd = proxyCommandFactory.toCommand(packet);
		System.out.println("cmd: " + cmd); //$NON-NLS-1$
		if (cmd != null) {
			fireProxyCommand(cmd);

			return true;
		}

		return false;
	}

	/**
	 * Send command to command handlers
	 * 
	 * @param cmd
	 */
	protected void fireProxyCommand(IProxyCommand cmd) {
		System.out.println("fireProxyCommand: " + cmd.getCommandID()); //$NON-NLS-1$
		for (IProxyCommandListener listener : listeners) {
			listener.handleCommand(cmd);
		}
	}

	protected abstract void runStateMachine() throws InterruptedException, IOException;
}
