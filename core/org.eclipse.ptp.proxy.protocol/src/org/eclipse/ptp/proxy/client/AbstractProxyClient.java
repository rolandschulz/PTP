/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/

package org.eclipse.ptp.proxy.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.internal.proxy.command.ProxyQuitCommand;
import org.eclipse.ptp.internal.proxy.event.ProxyConnectedEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyDisconnectedEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyMessageEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyTimeoutEvent;
import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyConnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyDisconnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyEventFactory;
import org.eclipse.ptp.proxy.event.IProxyEventListener;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.event.IProxyOKEvent;
import org.eclipse.ptp.proxy.event.IProxyShutdownEvent;
import org.eclipse.ptp.proxy.event.IProxyTimeoutEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public abstract class AbstractProxyClient implements IProxyClient {

	private enum SessionState {WAITING, CONNECTED, RUNNING, SHUTTING_DOWN, SHUTDOWN}
	
	private int					transactionID = 1;
	private int					sessPort = 0;
	private ServerSocketChannel	sessSvrSock = null;
	private SocketChannel		sessSock = null;
	private IProxyEventFactory	proxyEventFactory;

	private ReadableByteChannel	sessInput;
	private WritableByteChannel	sessOutput;

	private Thread				eventThread;
	private Thread				acceptThread;

	private List<IProxyEventListener>	listeners = Collections.synchronizedList(new ArrayList<IProxyEventListener>());

	private SessionState state = SessionState.SHUTDOWN;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#addProxyEventListener(org.eclipse.ptp.proxy.client.event.IProxyEventListener)
	 */
	public void addProxyEventListener(IProxyEventListener listener) {
		listeners.add(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#getSessionPort()
	 */
	public int getSessionPort() {
		return sessPort;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#isReady()
	 */
	public boolean isReady() {
		synchronized (state) {
			return state == SessionState.RUNNING;
		}
	}
	
	/**
	 * Test if proxy has shut down
	 * 
	 * @return shut down state
	 */
	public boolean isShutdown() {
		synchronized (state) {
			return state == SessionState.SHUTDOWN;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#newTransactionID()
	 */
	public synchronized int newTransactionID() {
		return ++transactionID;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#removeProxyEventListener(org.eclipse.ptp.proxy.client.event.IProxyEventListener)
	 */
	public void removeProxyEventListener(IProxyEventListener listener) {
		listeners.remove(listener);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sendCommand(java.lang.String)
	 */
	public void sendCommand(IProxyCommand cmd) throws IOException {
		ProxyPacket packet = new ProxyPacket(cmd);
		packet.send(sessOutput);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionConnect()
	 */
	public int sessionConnect() {
		return 0; // Not implemented
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate()
	 */
	public void sessionCreate() throws IOException {
		sessionCreate(0);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate(int)
	 */
	public void sessionCreate(int timeout) throws IOException {
		sessionCreate(0, timeout);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate(int, int)
	 */
	public void sessionCreate(int port, int timeout) throws IOException {
		System.out.println("sessionCreate("+port+","+timeout+")");
		sessSvrSock = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress(port);
		System.out.println("bind("+isa.toString()+")");
		sessSvrSock.socket().bind(isa);
		if (timeout > 0)
			sessSvrSock.socket().setSoTimeout(timeout);
		sessPort = sessSvrSock.socket().getLocalPort();
		
		synchronized (state) {
			state = SessionState.WAITING;
		}
		
		System.out.println("port=" + sessPort);
		acceptThread = new Thread("Proxy Client Accept Thread") {
			public void run() {
				boolean error = false;
				try {
					System.out.println("accept thread starting...");
					sessSock = sessSvrSock.accept();
					sessInput = sessSock;
					sessOutput = sessSock;
				} catch (SocketTimeoutException e) {
					error = true;
					fireProxyTimeoutEvent(new ProxyTimeoutEvent());
				} catch (ClosedByInterruptException e) {
					error = true;
					fireProxyMessageEvent(new ProxyMessageEvent(Level.WARNING, "Accept cancelled by user"));
				} catch (IOException e) {
					error = true;
					fireProxyMessageEvent(new ProxyMessageEvent(Level.FATAL, "IOException in accept"));
				} finally {		
					try {
						sessSvrSock.close();
					} catch (IOException e) {
						System.out.println("IO Exception trying to close server socket (non fatal)");
					}
					synchronized (state) {
						if (isInterrupted()) {
							error = true;
							fireProxyMessageEvent(new ProxyMessageEvent(Level.WARNING, "Connection cancelled by user"));
						}
						if (!error && state == SessionState.WAITING) {
							state = SessionState.CONNECTED;
							fireProxyConnectedEvent(new ProxyConnectedEvent());
						} else {
							state = SessionState.SHUTDOWN;
						}
					}
					System.out.println("accept thread exiting...");
				}
			}
		};
		acceptThread.start();
	}

	/**
	 * Create a proxy session that will read from InputStream and write to OutputStream
	 * 
	 * Generates a ProxyConnectedEvent
	 * 
	 * @param	output		stream to write to
	 * @param	input		stream to read from
	 */
	public void sessionCreate(OutputStream output, InputStream input) {
		System.out.println("sessionCreate(stdin, stdout)");
		sessInput = Channels.newChannel(input);
		sessOutput = Channels.newChannel(output);
		synchronized (state) {
			state = SessionState.CONNECTED;
		}
		fireProxyConnectedEvent(new ProxyConnectedEvent());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionFinish()
	 */
	public void sessionFinish() throws IOException {
		synchronized (state) {
			SessionState oldState = state;
			
			state = SessionState.SHUTTING_DOWN;
			
			switch (oldState) {
			case WAITING:
				if (acceptThread.isAlive()) {
					/*
					 * Force interrupt of accept. Note that this will cause
					 * a ProxyErrorEvent to be generated
					 */
					acceptThread.interrupt();
				}
				break;
			case CONNECTED:
				try {
					sessSock.close();
				} catch (IOException e) {
					state = SessionState.SHUTDOWN;
				} 
				break;
			case RUNNING:
				/*
				 * Send quit command. Proxy will shut down when OK is
				 * received or after shutdownTimeout.
				 */
				IProxyCommand cmd = new ProxyQuitCommand();
				// TODO: start shutdown timeout
				try {
					sendCommand(cmd);
				} catch (IOException e) {
					// Tell event thread to exit
					state = SessionState.SHUTDOWN;
					// TODO: stop shutdown timeout
				}
				break;
			}
		}
	}

	/**
	 * Start a thread to process events from the proxy by repeatedly calling sessionProgress(). 
	 * The thread is guaranteed to produce a ProxyDisconnectedEvent when it exits.
	 * 
	 * @throws IOException	if the session is not connected or the event thread fails to start
	 */
	public void sessionHandleEvents() throws IOException {
		eventThread = new Thread("Proxy Client Event Thread") {
			public void run() {
				boolean error = false;
				int errorCount = 0;			
				
				System.out.println("event thread starting...");
				try {
					while (errorCount < MAX_ERRORS && !isInterrupted()) {
						synchronized (state) {
							if (state == SessionState.SHUTDOWN) {
								break;
							}
						}
						if (!sessionProgress()) {
							errorCount++;
						}
					}
				} catch (IOException e) {
					synchronized (state) {
						if (!isInterrupted() && state != SessionState.SHUTTING_DOWN) {
							error = true;
							System.out.println("event thread IOException . . . " + e.getMessage());
						}
					}
				} 
				
				if (errorCount >= MAX_ERRORS) {
					error = true;
				}
				
				try {
					sessSock.close();
				} catch (IOException e) {
				} 
				
				synchronized (state) {
					state = SessionState.SHUTDOWN;
				}

				fireProxyDisconnectedEvent(new ProxyDisconnectedEvent(error));
				System.out.println("event thread exited");
			}
		};

		synchronized (state) {
			if (state != SessionState.CONNECTED) {
				throw new IOException("Not ready to receive events");
			}
			state = SessionState.RUNNING;
		}
		eventThread.start();
	}
	
	/**
	 * Set the factory used to decode events
	 * 
	 * @param factory
	 */
	public void setEventFactory(IProxyEventFactory factory) {
		this.proxyEventFactory = factory;
	}
	
	/**
	 * Process incoming events
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
		IProxyEvent e = proxyEventFactory.toEvent(packet);
				
		if (e != null) {
			if (e instanceof IProxyMessageEvent) {
				fireProxyMessageEvent((IProxyMessageEvent) e);
			} else if (e instanceof IProxyOKEvent) {
				fireProxyOKEvent((IProxyOKEvent) e);
			} else if (e instanceof IProxyErrorEvent) {
				fireProxyErrorEvent((IProxyErrorEvent) e);
			} else if (e instanceof IProxyShutdownEvent) {
				if (state == SessionState.SHUTTING_DOWN) { 
					state = SessionState.SHUTDOWN;
					// TODO: stop shutdown timeout
				}
			} else if (e instanceof IProxyExtendedEvent) {
				fireProxyExtendedEvent((IProxyExtendedEvent) e);
			}
			
			return true;
		}
		
		return false;
	}

	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyConnectedEvent(IProxyConnectedEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyDisconnectedEvent(IProxyDisconnectedEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyErrorEvent(IProxyErrorEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyExtendedEvent(IProxyExtendedEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyMessageEvent(IProxyMessageEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyOKEvent(IProxyOKEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
	
	/**
	 * Send event to event handlers
	 * 
	 * @param event
	 */
	protected void fireProxyTimeoutEvent(IProxyTimeoutEvent event) {
		IProxyEventListener[] la = listeners.toArray(new IProxyEventListener[0]);
		for (IProxyEventListener listener : la) {
			listener.handleEvent(event);
		}
	}
}
