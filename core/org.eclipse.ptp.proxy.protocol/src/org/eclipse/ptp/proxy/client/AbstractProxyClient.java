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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

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
import org.eclipse.ptp.proxy.util.ProtocolUtil;

public abstract class AbstractProxyClient implements IProxyClient {

	private enum SessionState {WAITING, CONNECTED, RUNNING, SHUTTING_DOWN, SHUTDOWN}
	
	private boolean				debug = false;
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

	private Charset			charset = Charset.forName("US-ASCII");
	private CharsetEncoder	encoder = charset.newEncoder();
	private CharsetDecoder	decoder = charset.newDecoder();
	
	private SessionState state = SessionState.SHUTDOWN;
	
	private final ReentrantLock stateLock = new ReentrantLock();

	public AbstractProxyClient(IProxyEventFactory factory) {
		proxyEventFactory = factory;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#addProxyEventListener(org.eclipse.ptp.proxy.client.event.IProxyEventListener)
	 */
	public void addProxyEventListener(IProxyEventListener listener) {
		listeners.add(listener);
	}

	/**
	 * Character set decoder
	 * 
	 * @return decoder
	 */
	public CharsetDecoder decoder() {
		return decoder;
	}
	
	/**
	 * Character set encoder
	 * 
	 * @return encoder
	 */
	public CharsetEncoder encoder() {
		return encoder;
	}
	
	/**
	 * Write a full buffer to the socket.
	 * 
	 * @param buf
	 * @return number of bytes written
	 * @throws IOException
	 */
	public int fullWrite(ByteBuffer buf) throws IOException {
		int n = 0;
		while (buf.remaining() > 0) {
			n = sessOutput.write(buf);
			if (n < 0) {
				throw new IOException("EOF from proxy");
			}
		}
		return n;
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
		stateLock.lock();
		try {
			return state == SessionState.RUNNING;
		} finally {
			stateLock.unlock();
		}
	}

	/**
	 * Test if proxy has shut down
	 * 
	 * @return shut down state
	 */
	public boolean isShutdown() {
		stateLock.lock();
		try {
			return state == SessionState.SHUTDOWN;
		} finally {
			stateLock.unlock();
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
		if (isReady()) {
			sendCommandBuffer(cmd.getEncodedMessage());
		}
		else {
			throw new IOException("proxy not ready to send");
		}
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
		
		stateLock.lock();
		try {
			state = SessionState.WAITING;
		} finally {
			stateLock.unlock();
		}
		System.out.println("port=" + sessPort);
		acceptThread = new Thread("Proxy Client Accept Thread") {
			public void run() {
				boolean error = false;
				try {
					System.out.println("accept thread starting...");
					sessSock = sessSvrSock.accept();
					sessInput = sessSock.socket().getChannel();
					sessOutput = sessSock.socket().getChannel();
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
					stateLock.lock();
					try {
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
					} finally {
						stateLock.unlock();
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
		stateLock.lock();
		try {
			state = SessionState.CONNECTED;
		} finally {
			stateLock.unlock();
		}
		fireProxyConnectedEvent(new ProxyConnectedEvent());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.client.IProxyClient#sessionFinish()
	 */
	public void sessionFinish() throws IOException {
		stateLock.lock();
		try {
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
				String cmdBuf = cmd.getEncodedMessage();
				// TODO: start shutdown timeout
				try {
					sendCommandBuffer(cmdBuf);
				} catch (IOException e) {
					// Tell event thread to exit
					state = SessionState.SHUTDOWN;
					// TODO: stop shutdown timeout
				}
				break;
			}
		} finally {
			stateLock.unlock();
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
						stateLock.lock();
						try {
							if (state == SessionState.SHUTDOWN) {
								break;
							}
						} finally {
							stateLock.unlock();
						}
						if (!sessionProgress()) {
							errorCount++;
						}
					}
				} catch (IOException e) {
					stateLock.lock();
					try {
						if (!isInterrupted() && state != SessionState.SHUTTING_DOWN) {
							error = true;
							System.out.println("event thread IOException . . . " + e.getMessage());
						}
					} finally {
						stateLock.unlock();
					}
				} 
				
				if (errorCount >= MAX_ERRORS) {
					error = true;
				}
				
				try {
					sessSock.close();
				} catch (IOException e) {
				} 
				
				stateLock.lock();
				try {
					state = SessionState.SHUTDOWN;
				} finally {
					stateLock.unlock();
				}

				fireProxyDisconnectedEvent(new ProxyDisconnectedEvent(error));
				System.out.println("event thread exited");
			}
		};

		stateLock.lock();
		try {
			if (state != SessionState.CONNECTED) {
				throw new IOException("Not ready to receive events");
			}
			state = SessionState.RUNNING;
		} finally {
			stateLock.unlock();
		}
		eventThread.start();
	}

	/**
	 * Read a full buffer from the socket.
	 * 
	 * @return	number of bytes read
	 * @throws	IOException if EOF
	 */
	private int fullRead(ByteBuffer buf) throws IOException {
		int n = 0;
		buf.clear();
		while (buf.remaining() > 0) {
			n = sessInput.read(buf);
			if (n < 0) {
				throw new IOException("EOF from proxy");
			}
		}
		buf.flip();
		return n;
	}

	/**
	 * Send the supplied string to the remote proxy. Formats the string
	 * into the correct proxy format.
	 * 
	 * @param buf
	 * @throws IOException
	 */
	private void sendCommandBuffer(String buf) throws IOException {
		/*
		 * Note: command length includes the first space!
		 */
		String sendCmd = ProtocolUtil.encodeIntVal(buf.length() + 1, 
				IProxyCommand.CMD_LENGTH_SIZE) + " " + buf;
		if (debug) {
			System.out.println("COMMAND: " + sendCmd);
		}
		fullWrite(encoder.encode(CharBuffer.wrap(sendCmd)));
		
	}

	/**
	 * Process packets from the wire. Each packet comprises a length, header and a body 
	 * formatted as follows:
	 * 
	 * LENGTH HEADER BODY
	 * 
	 * where:
	 * 
	 * LENGTH	is an IProxyEvent.EVENT_LENGTH_SIZE hexadecimal number representing
	 * 			the total length of the event excluding the LENGTH field.
	 * 
	 * HEADER consists of the following fields:
	 * 
	 * ' ' EVENT_ID ':' TRANS_ID ':' NUM_ARGS
	 * 
	 * where:
	 * 
	 * EVENT_ID	is an IProxyEvent.EVENT_ID_SIZE hexadecimal number representing
	 * 			the type of this event.
	 * TRANS_ID	is an IProxyEvent.EVENT_TRANS_ID_SIZE hexadecimal number representing
	 * 			the transaction ID of the event.
	 * NUM_ARGS	is an IProxyEvent.EVENT_ARGS_SIZE hexadecimal number representing
	 * 			the number of arguments. 
	 * 
	 * The event body is formatted as a list of NUM_ARGS string arguments, each 
	 * preceeded by a space (0x20) characters as follows:
	 * 	
	 * ' ' LENGTH ':' BYTES ... ' ' LENGTH ':' BYTES
	 * 
	 * where:
	 * 
	 * LENGTH	is an IProxyEvent.EVENT_ARG_SIZE hexadecimal number representing
	 * 			the length of the string.
	 * BYTES	are LENGTH bytes of the string. Any characters are permitted, 
	 * 			including spaces
	 * 	
	 * @return	false if a protocol error occurs
	 * @throws	IOException if the connection is terminated (read returns < 0)
	 * 		
	 */
	private boolean sessionProgress() throws IOException {
		/*
		 * First EVENT_LENGTH_SIZE bytes are the length of the event
		 */
		ByteBuffer lengthBytes = ByteBuffer.allocate(IProxyEvent.EVENT_LENGTH_SIZE);
		int readLen;
		
		readLen = fullRead(lengthBytes);
		if (readLen != IProxyEvent.EVENT_LENGTH_SIZE) {
			return false;
		}
		
		CharBuffer len_str = decoder.decode(lengthBytes);

		int len = Integer.parseInt(len_str.subSequence(0, IProxyEvent.EVENT_LENGTH_SIZE).toString(), 16);
		
		/*
		 * Read len bytes of rest of event
		 */
		ByteBuffer eventBytes = ByteBuffer.allocate(len);

		readLen = fullRead(eventBytes);
		if (readLen < IProxyEvent.EVENT_ID_SIZE + IProxyEvent.EVENT_TRANS_ID_SIZE + IProxyEvent.EVENT_NARGS_SIZE + 3) {
			return false;
		}

		CharBuffer eventBuf = decoder.decode(eventBytes);
		
		/*
		 * Extract transaction ID and event type
		 */
		
		int idStart = 1; // Skip ' '
		int idEnd = idStart + IProxyEvent.EVENT_ID_SIZE;
		int transStart = idEnd + 1; // Skip ':'
		int transEnd = transStart + IProxyEvent.EVENT_TRANS_ID_SIZE;
		int numArgsStart = transEnd + 1; // Skip ':'
		int numArgsEnd = numArgsStart + IProxyEvent.EVENT_NARGS_SIZE;
		
		int eventTransID;
		int eventID;
		String[] eventArgs;
		
		try {
			eventID = Integer.parseInt(eventBuf.subSequence(idStart, idEnd).toString(), 16);
			eventTransID = Integer.parseInt(eventBuf.subSequence(transStart, transEnd).toString(), 16);
			int eventNumArgs = Integer.parseInt(eventBuf.subSequence(numArgsStart, numArgsEnd).toString(), 16);
			
			/*
			 * Extract rest of event arguments. Each argument is an 8 byte hex length, ':' and
			 * then the characters of the argument.
			 */
			
			eventArgs = new String[eventNumArgs];
			int argPos = numArgsEnd + 1;
			
			for (int i = 0; i < eventNumArgs; i++) {
				eventArgs[i] = ProtocolUtil.decodeString(eventBuf, argPos);
				argPos += eventArgs[i].length() + IProxyEvent.EVENT_ARG_LEN_SIZE + 2;
			}
			
			if (debug) {
				System.out.print("EVENT ID:" + eventID + " TID:" + eventTransID);
				for (String arg : eventArgs) {
					System.out.print(" ARG:\"" + arg + "\"");
				}
				System.out.println();
			}
		} catch (IndexOutOfBoundsException e1) {
			return false;
		}

		/*
		 * Now convert the event into an IProxyEvent
		 */
		IProxyEvent e = proxyEventFactory.toEvent(eventID, eventTransID, eventArgs);
				
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
