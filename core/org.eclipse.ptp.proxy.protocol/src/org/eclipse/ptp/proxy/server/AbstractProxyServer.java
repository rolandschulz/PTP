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
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyCommandFactory;
import org.eclipse.ptp.proxy.command.IProxyCommandListener;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.util.compression.IEncoder;
import org.eclipse.ptp.proxy.util.compression.IDecoder;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteCompress;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteUncompress;

public abstract class AbstractProxyServer implements IProxyServer {
	protected enum ServerState {
		INIT, DISCOVERY, NORMAL, SUSPENDED, SHUTDOWN
	}

	public static final int MAX_ERRORS = 5;

	/**
	 * @since 4.0
	 */
	protected ServerState state = ServerState.INIT;

	private final String sessHost;
	private final int sessPort;
	private SocketChannel sessSocket;
	private IEncoder compressor;
	private IDecoder uncompressor;
	private final IProxyCommandFactory proxyCommandFactory;
	private Thread commandThread;
	/**
	 * @since 4.0
	 */
	protected Thread stateMachineThread;
	private final List<IProxyCommandListener> listeners = Collections.synchronizedList(new ArrayList<IProxyCommandListener>());

	public AbstractProxyServer(String host, int port, IProxyCommandFactory factory) {
		this.sessHost = host;
		this.sessPort = port;
		this.proxyCommandFactory = factory;
		compressor = new HuffmanByteCompress(ProxyPacket.getDefaultHuffmanTable());
		uncompressor = new HuffmanByteUncompress();
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

	/**
	 * @throws IOException
	 */
	public void connect() throws IOException {
		sessSocket = SocketChannel.open();
		sessSocket.connect(new InetSocketAddress(sessHost, sessPort));
	}

	/**
	 * Send event to server
	 * 
	 * @param event
	 *            event to send
	 * @since 5.0
	 */
	protected void sendEvent(IProxyEvent event) throws IOException {
		ProxyPacket packet = new ProxyPacket(event, compressor);
		packet.send(sessSocket);
	}

	/**
	 * Send command to command handlers
	 * 
	 * @param cmd
	 * @since 4.0
	 */
	protected void fireProxyCommand(IProxyCommand cmd) {
		System.out.println("fireProxyCommand: " + cmd.getCommandID()); //$NON-NLS-1$
		for (IProxyCommandListener listener : listeners) {
			listener.handleCommand(cmd);
		}
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
	 * @since 4.0
	 */
	protected abstract void runStateMachine() throws InterruptedException, IOException;

	/**
	 * Process commands from the wire
	 * 
	 * @return
	 * @throws IOException
	 */
	private boolean sessionProgress() throws IOException {
		ProxyPacket packet = new ProxyPacket(uncompressor);
		System.out.print("sessionProgress: "); //$NON-NLS-1$
		if (!packet.read(sessSocket)) {
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
	 * Start a thread to process commands from the proxy by repeatedly calling
	 * sessionProgress().
	 * 
	 * @throws IOException
	 */
	public void start() throws IOException {
		commandThread = new Thread("Proxy Server Command Thread") { //$NON-NLS-1$
			@Override
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
					sessSocket.close();
				} catch (IOException e) {
				}

				synchronized (state) {
					state = ServerState.SHUTDOWN;
				}

				System.out.println("server thread exited"); //$NON-NLS-1$
				if (error) {
					System.out.println(" due to errors .. shutting down"); //$NON-NLS-1$
					stateMachineThread.interrupt();
				} else {
					System.out.println(" normally"); //$NON-NLS-1$
				}
			}
		};
		commandThread.start();
		try {
			stateMachineThread = Thread.currentThread();
			runStateMachine();
			commandThread.interrupt();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
