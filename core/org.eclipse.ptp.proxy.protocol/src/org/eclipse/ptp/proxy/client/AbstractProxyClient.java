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
import java.net.InetSocketAddress;
import java.net.SocketTimeoutException;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.eclipse.ptp.internal.proxy.command.ProxyQuitCommand;
import org.eclipse.ptp.internal.proxy.event.ProxyConnectedEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyDisconnectedEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyMessageEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyShutdownEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyTimeoutEvent;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeClearFiltersCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeResumeEventsCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeSetFiltersCommand;
import org.eclipse.ptp.internal.proxy.runtime.command.ProxyRuntimeSuspendEventsCommand;
import org.eclipse.ptp.proxy.command.AbstractProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyConnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyDisconnectedEvent;
import org.eclipse.ptp.proxy.event.IProxyErrorEvent;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyEventFactory;
import org.eclipse.ptp.proxy.event.IProxyEventListener;
import org.eclipse.ptp.proxy.event.IProxyExtendedEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.proxy.event.IProxyOKEvent;
import org.eclipse.ptp.proxy.event.IProxyShutdownEvent;
import org.eclipse.ptp.proxy.event.IProxyTimeoutEvent;
import org.eclipse.ptp.proxy.messages.Messages;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.util.DebugOptions;
import org.eclipse.ptp.proxy.util.compression.IEncoder;
import org.eclipse.ptp.proxy.util.compression.IDecoder;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteCompress;
import org.eclipse.ptp.proxy.util.compression.huffmancoder.HuffmanByteUncompress;

public abstract class AbstractProxyClient implements IProxyClient {

	private enum FlowControlState {
		NORMAL_FLOW,
		SUSPEND_FLOW,
		TERMINATE_FLOW
	}

	private enum SessionState {
		WAITING,
		CONNECTED,
		RUNNING,
		SHUTTING_DOWN,
		SHUTDOWN
	}

	private static final int LOW_EVENT_THRESHOLD = 0;
	private static final int HIGH_EVENT_THRESHOLD = 100;
	private static final int SHUTDOWN_THRESHOLD = 5000;
	private static final String stdioFilters[] = { "stdout", "stderr" }; //$NON-NLS-1$//$NON-NLS-2$

	private int transactionID = 1;
	private int sessPort = 0;
	private ServerSocketChannel sessSvrSock = null;
	private SocketChannel sessSock = null;
	private IProxyEventFactory proxyEventFactory;
	private FlowControlState flowState = FlowControlState.NORMAL_FLOW;
	
	private IEncoder compressor;
	private IDecoder uncompressor;

	private Thread eventThread;
	private Thread acceptThread;
	private Thread packetThread;

	private volatile SessionState state;
	private ProxyEventQueue eventQueue;

	private final DebugOptions debugOptions;
	private final List<IProxyEventListener> listeners = Collections.synchronizedList(new ArrayList<IProxyEventListener>());
	private final Vector<AbstractProxyCommand> pendingCommands;

	public AbstractProxyClient() {
		this.debugOptions = new DebugOptions();
		this.state = SessionState.SHUTDOWN;
		pendingCommands = new Vector<AbstractProxyCommand>();
		compressor = new HuffmanByteCompress(ProxyPacket.getDefaultHuffmanTable());
		uncompressor = new HuffmanByteUncompress();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.client.IProxyClient#addProxyEventListener(org.eclipse
	 * .ptp.proxy.client.event.IProxyEventListener)
	 */
	public void addProxyEventListener(IProxyEventListener listener) {
		listeners.add(listener);
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

	/**
	 * Get the debug options
	 * 
	 * @return debug options
	 */
	protected DebugOptions getDebugOptions() {
		return debugOptions;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#getSessionPort()
	 */
	public int getSessionPort() {
		return sessPort;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#isReady()
	 */
	public boolean isReady() {
		return state == SessionState.RUNNING;
	}

	/**
	 * Test if proxy has shut down
	 * 
	 * @return shut down state
	 */
	public boolean isShutdown() {
		return state == SessionState.SHUTDOWN;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#newTransactionID()
	 */
	public synchronized int newTransactionID() {
		return ++transactionID;
	}

	private void processPacket() throws IOException {
		ProxyPacket packet = new ProxyPacket(uncompressor);
		if (getDebugOptions().PROTOCOL_TRACING) {
			packet.setDebug(true);
		}
		if (!packet.read(sessSock)) {
			return;
		}

		/*
		 * Now convert the event into an IProxyEvent
		 */
		IProxyEvent e = proxyEventFactory.toEvent(packet);
		if (e != null) {
			eventQueue.addProxyEvent(e);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.client.IProxyClient#removeProxyEventListener(org
	 * .eclipse.ptp.proxy.client.event.IProxyEventListener)
	 */
	public void removeProxyEventListener(IProxyEventListener listener) {
		listeners.remove(listener);
	}

	private void runEventThread() throws IOException {
		eventThread = new Thread("Proxy Client Event Thread") { //$NON-NLS-1$
			@Override
			public void run() {
				boolean error = false;
				int errorCount = 0;

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("event thread starting..."); //$NON-NLS-1$
				}
				try {
					while (errorCount < MAX_ERRORS && !isInterrupted()) {
						if (state == SessionState.SHUTDOWN) {
							break;
						}
						if (!sessionProgress()) {
							errorCount++;
						}
					}
				} catch (IOException e) {
					if (!isInterrupted() && state != SessionState.SHUTTING_DOWN) {
						error = true;
						if (getDebugOptions().CLIENT_TRACING) {
							System.out.println("event thread IOException . . . " + e.getMessage()); //$NON-NLS-1$
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

				state = SessionState.SHUTDOWN;

				fireProxyDisconnectedEvent(new ProxyDisconnectedEvent(error));

				if (getDebugOptions().CLIENT_TRACING) {
					System.out.println("event thread exited"); //$NON-NLS-1$
				}
			}
		};

		if (state != SessionState.CONNECTED) {
			throw new IOException(Messages.getString("AbstractProxyClient_4")); //$NON-NLS-1$
		}
		state = SessionState.RUNNING;
		eventThread.start();
	}

	/**
	 * Top level method for thread receiving incoming events from the proxy.
	 */
	private void runPacketThread() {
		packetThread = new Thread("Proxy Client Packet Thread") { //$NON-NLS-1$
			@Override
			public void run() {
				// Loop processing messages from the proxy for the duration of
				// the session
				for (;;) {
					if (state == SessionState.SHUTDOWN) {
						break;
					}
					try {
						processPacket();
					} catch (IOException e) {
						/*
						 * Handle the situation where the proxy doesn't respond
						 * to a quit command with an OK event. In this case, we
						 * fake a shutdown event.
						 */
						if (state == SessionState.SHUTTING_DOWN) {
							eventQueue.addPriorityProxyEvent(new ProxyShutdownEvent(0));
						} else {
							eventQueue.addPriorityProxyEvent(new ProxyDisconnectedEvent(true));
						}
						/*
						 * If the connection closes this loop loops on an
						 * IOException thrown by ProxyPacket.fullRead(); To
						 * prevent this thread from looping, exit on an
						 * IOException.
						 */
						break;
					}
				}
			}
		};
		packetThread.start();
	}

	/**
	 * Send a command to the proxy instructing it to remove filtering for the
	 * specified event types
	 * 
	 * @param args
	 *            Array of event types to remove filtering
	 */
	private void sendClearFiltersCommand(String args[]) {
		try {
			AbstractProxyCommand cmd;

			cmd = new ProxyRuntimeClearFiltersCommand();
			for (int i = 0; i < args.length; i++) {
				cmd.addArgument(args[i]);
			}
			sendCommand(cmd);
			pendingCommands.add(cmd);
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.client.IProxyClient#sendCommand(java.lang.String)
	 */
	public synchronized void sendCommand(IProxyCommand cmd) throws IOException {
		if (!isReady()) {
			throw new IOException(Messages.getString("AbstractProxyClient_0")); //$NON-NLS-1$
		}
		ProxyPacket packet = new ProxyPacket(cmd, compressor);
		if (getDebugOptions().PROTOCOL_TRACING) {
			packet.setDebug(true);
		}
		packet.send(sessSock);
	}

	/**
	 * Send a command to the proxy instructing it to resume sending messages to
	 * the client
	 */
	private void sendResumeEventsCommand() {
		try {
			AbstractProxyCommand cmd;

			cmd = new ProxyRuntimeResumeEventsCommand();
			sendCommand(cmd);
			pendingCommands.add(cmd);
		} catch (IOException e) {
		}
	}

	/**
	 * Send a command to the proxy instructing it to apply filtering for the
	 * specified event types
	 * 
	 * @param args
	 *            Array of event types to apply filtering
	 */
	private void sendSetFiltersCommand(String args[]) {
		try {
			AbstractProxyCommand cmd;

			cmd = new ProxyRuntimeSetFiltersCommand();
			for (int i = 0; i < args.length; i++) {
				cmd.addArgument(args[i]);
			}
			sendCommand(cmd);
			pendingCommands.add(cmd);
		} catch (IOException e) {
		}
	}

	/**
	 * Send a command to the proxy instructing it to shutdown
	 */
	private void sendShutdownServerCommand() {
		try {
			AbstractProxyCommand cmd;

			cmd = new ProxyQuitCommand();
			sendCommand(cmd);
			pendingCommands.add(cmd);
		} catch (IOException e) {
		}
	}

	/**
	 * Send a command to the proxy instructing it to suspend sending messages to
	 * the client
	 */
	private void sendSuspendEventsCommand() {
		try {
			AbstractProxyCommand cmd;

			cmd = new ProxyRuntimeSuspendEventsCommand();
			sendCommand(cmd);
			pendingCommands.add(cmd);
		} catch (IOException e) {
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionConnect()
	 */
	public int sessionConnect() {
		return 0; // Not implemented
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate()
	 */
	public void sessionCreate() throws IOException {
		sessionCreate(0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate(int)
	 */
	public void sessionCreate(int timeout) throws IOException {
		sessionCreate(0, timeout);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionCreate(int, int)
	 */
	public void sessionCreate(int port, int timeout) throws IOException {
		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println("sessionCreate(" + port + "," + timeout + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		}
		sessSvrSock = ServerSocketChannel.open();
		InetSocketAddress isa = new InetSocketAddress(port);
		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println("bind(" + isa.toString() + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		sessSvrSock.socket().bind(isa);
		if (timeout > 0) {
			sessSvrSock.socket().setSoTimeout(timeout);
		}
		sessPort = sessSvrSock.socket().getLocalPort();

		state = SessionState.WAITING;

		if (getDebugOptions().CLIENT_TRACING) {
			System.out.println("port=" + sessPort); //$NON-NLS-1$
		}

		acceptThread = new Thread("Proxy Client Accept Thread") { //$NON-NLS-1$
			@Override
			public void run() {
				boolean error = false;
				try {
					if (getDebugOptions().CLIENT_TRACING) {
						System.out.println("accept thread starting..."); //$NON-NLS-1$
					}
					sessSock = sessSvrSock.accept();
				} catch (SocketTimeoutException e) {
					error = true;
					fireProxyTimeoutEvent(new ProxyTimeoutEvent());
				} catch (ClosedByInterruptException e) {
					error = true;
					fireProxyMessageEvent(new ProxyMessageEvent(Level.WARNING, Messages.getString("AbstractProxyClient_1"))); //$NON-NLS-1$
				} catch (IOException e) {
					error = true;
					fireProxyMessageEvent(new ProxyMessageEvent(Level.FATAL, Messages.getString("AbstractProxyClient_2"))); //$NON-NLS-1$
				} finally {
					try {
						sessSvrSock.close();
					} catch (IOException e) {
						if (getDebugOptions().CLIENT_TRACING) {
							System.out.println("IO Exception trying to close server socket (non fatal)"); //$NON-NLS-1$
						}
					}
					if (isInterrupted()) {
						error = true;
						fireProxyMessageEvent(new ProxyMessageEvent(Level.WARNING, Messages.getString("AbstractProxyClient_3"))); //$NON-NLS-1$
					}
					if (!error && state == SessionState.WAITING) {
						state = SessionState.CONNECTED;
						fireProxyConnectedEvent(new ProxyConnectedEvent());
					} else {
						state = SessionState.SHUTDOWN;
					}
					if (getDebugOptions().CLIENT_TRACING) {
						System.out.println("accept thread exiting..."); //$NON-NLS-1$
					}
				}
			}
		};
		acceptThread.start();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionFinish()
	 */
	public void sessionFinish() throws IOException {
		switch (state) {
		case WAITING:
			if (acceptThread.isAlive()) {
				/*
				 * Force interrupt of accept. Note that this will cause a
				 * ProxyErrorEvent to be generated
				 */
				acceptThread.interrupt();
			}
			state = SessionState.SHUTTING_DOWN;
			break;
		case CONNECTED:
			try {
				sessSock.close();
				state = SessionState.SHUTTING_DOWN;
			} catch (IOException e) {
				state = SessionState.SHUTDOWN;
			}
			break;
		case RUNNING:
			/*
			 * Send quit command. Proxy will shut down when OK is received or
			 * after shutdownTimeout.
			 */
			IProxyCommand cmd = new ProxyQuitCommand();
			// TODO: start shutdown timeout
			try {
				sendCommand(cmd);
				state = SessionState.SHUTTING_DOWN;
			} catch (IOException e) {
				// Tell event thread to exit
				state = SessionState.SHUTDOWN;
				// TODO: stop shutdown timeout
			}
			break;
		case SHUTDOWN:
			/* ignore */
			break;
		case SHUTTING_DOWN:
			/* ignore */
			break;
		}
	}

	/**
	 * Start threads to read packets sent to the client by a proxy and to
	 * process events created from the packets. Communication between these two
	 * threads is thru a ProxyEventQueue object The event thread is guaranteed
	 * to produce a ProxyDisconnectedEvent when it exits.
	 * 
	 * @throws IOException
	 *             if the session is not connected or the event thread fails to
	 *             start
	 */
	public void sessionHandleEvents() throws IOException {
		eventQueue = new ProxyEventQueue();
		runEventThread();
		runPacketThread();
	}

	/**
	 * Handle events that have been queued by the proxy packet handling thread
	 * 
	 * @return true if an event has beep handled, false if no event to process
	 * @throws IOException
	 */
	private boolean sessionProgress() throws IOException {
		IProxyEvent e;
		int queueSize;

		// Implement flow control for the proxy. Flow control is implemented by
		// checking the size of the pending event queue.
		// If the number of pending messages reaches the upper queue size
		// threshold then send commands to the proxy instructing it
		// to suspend sending messages to the client.
		//
		// Once the size of the pending event queue is <= the lower
		// threshold, send commands to the proxy instructing it to resume
		// sending messages to the client.
		//
		// If the proxy fails to honor thecommands to suspend
		// sending messages, the pending event queue will continue to grow in
		// size. If the size reaches a second threshold, the
		// proxy is terminated.
		queueSize = eventQueue.size();
		if (queueSize >= SHUTDOWN_THRESHOLD) {
			if (flowState != FlowControlState.TERMINATE_FLOW) {
				sendShutdownServerCommand();
				flowState = FlowControlState.TERMINATE_FLOW;
			}
		} else {
			if (queueSize >= HIGH_EVENT_THRESHOLD) {
				if (flowState != FlowControlState.SUSPEND_FLOW) {
					sendSuspendEventsCommand();
					sendSetFiltersCommand(stdioFilters);
					flowState = FlowControlState.SUSPEND_FLOW;
				}
			} else {
				if (queueSize <= LOW_EVENT_THRESHOLD) {
					if (flowState != FlowControlState.NORMAL_FLOW) {
						sendResumeEventsCommand();
						sendClearFiltersCommand(stdioFilters);
						flowState = FlowControlState.NORMAL_FLOW;
					}
				}
			}
		}
		e = eventQueue.getProxyEvent();
		if (e != null) {
			if (e instanceof IProxyMessageEvent) {
				fireProxyMessageEvent((IProxyMessageEvent) e);
			} else if (e instanceof IProxyOKEvent) {
				AbstractProxyCommand matchingCommand;

				matchingCommand = null;
				for (AbstractProxyCommand cmd : pendingCommands) {
					if (cmd.getTransactionID() == e.getTransactionID()) {
						matchingCommand = cmd;
						break;
					}
				}
				if (matchingCommand == null) {
					fireProxyOKEvent((IProxyOKEvent) e);
				} else {
					pendingCommands.remove(matchingCommand);
					// matchingCommand.completed();
				}
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
	 * Set the factory used to decode events
	 * 
	 * @param factory
	 */
	public void setEventFactory(IProxyEventFactory factory) {
		this.proxyEventFactory = factory;
	}

}
