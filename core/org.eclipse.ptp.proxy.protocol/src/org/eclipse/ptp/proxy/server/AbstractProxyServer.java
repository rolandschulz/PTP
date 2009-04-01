/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.proxy.server;

import java.io.IOException;
import java.net.Socket;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyCommandFactory;
import org.eclipse.ptp.proxy.command.IProxyCommandListener;
import org.eclipse.ptp.proxy.command.IProxyExtendedCommand;
import org.eclipse.ptp.proxy.command.IProxyQuitCommand;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public abstract class AbstractProxyServer implements IProxyServer {
	private enum ServerState {WAITING, CONNECTED, RUNNING, SHUTTING_DOWN, SHUTDOWN}

	public static final int MAX_ERRORS = 5;
	
	private ServerState 				state = ServerState.SHUTDOWN;

	private String						sessHost;
	private int							sessPort;
	private Socket 						sessSocket;
	private ReadableByteChannel 		sessInput;
	private IProxyCommandFactory		proxyCommandFactory;
	private Thread						commandThread;
	private List<IProxyCommandListener>	listeners = 
		Collections.synchronizedList(new ArrayList<IProxyCommandListener>());

	public AbstractProxyServer(String host, int port, IProxyCommandFactory factory) {
		this.sessHost = host;
		this.sessPort = port;
		this.proxyCommandFactory = factory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.server.IProxyServer#addListener(org.eclipse.ptp.proxy.command.IProxyCommandListener)
	 */
	public void addListener(IProxyCommandListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.server.IProxyServer#addListener(org.eclipse.ptp.proxy.command.IProxyCommandListener)
	 */
	public void removeListener(IProxyCommandListener listener) {
		listeners.remove(listener);
	}

	public void connect() throws IOException {
		sessSocket = new Socket(sessHost, sessPort);
		sessInput = sessSocket.getChannel();
	}
	
	/**
	 * Start a thread to process commands from the proxy by repeatedly 
	 * calling sessionProgress(). 
	 */
	public void start() {
		commandThread = new Thread("Proxy Server Command Thread") { //$NON-NLS-1$
			public void run() {
				boolean error = false;
				int errorCount = 0;			
				
				System.out.println("server command thread starting..."); //$NON-NLS-1$
				try {
					while (errorCount < MAX_ERRORS && !isInterrupted()) {
						synchronized(state) {
							if (state == ServerState.SHUTDOWN) {
								break;
							}
						}
						if (!sessionProgress()) {
							errorCount++;
						}
					}
				} catch (IOException e) {
					synchronized(state) {
						if (!isInterrupted() && state != ServerState.SHUTTING_DOWN) {
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
				
				synchronized(state) {
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

		try {
			commandThread.join();
		} catch (InterruptedException e) {
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
		if (!packet.read(sessInput)) {
			return false;
		}
		
		/*
		 * Now convert the event into an IProxyEvent
		 */
		IProxyCommand cmd = proxyCommandFactory.toCommand(packet);
				
		if (cmd != null) {
			if (cmd instanceof IProxyQuitCommand) {
				fireProxyQuitCommand((IProxyQuitCommand)cmd);
			} else if (cmd instanceof IProxyExtendedCommand) {
				fireProxyExtendedCommand((IProxyExtendedCommand)cmd);
			}
			
			return true;
		}
		
		return false;
	}
	
	/**
	 * Send command to command handlers
	 * 
	 * @param cmd
	 */
	protected void fireProxyExtendedCommand(IProxyExtendedCommand cmd) {
		IProxyCommandListener[] la = listeners.toArray(new IProxyCommandListener[0]);
		for (IProxyCommandListener listener : la) {
			listener.handleCommand(cmd);
		}
	}
	
	/**
	 * Send command to command handlers
	 * 
	 * @param cmd
	 */
	protected void fireProxyQuitCommand(IProxyQuitCommand cmd) {
		IProxyCommandListener[] la = listeners.toArray(new IProxyCommandListener[0]);
		for (IProxyCommandListener listener : la) {
			listener.handleCommand(cmd);
		}
	}

}
