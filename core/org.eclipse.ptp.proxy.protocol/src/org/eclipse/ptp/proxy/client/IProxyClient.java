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

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.event.IProxyEventListener;

public interface IProxyClient {
	/* wire protocol version */
	public static final String WIRE_PROTOCOL_VERSION = "2.0";
	public static final int MAX_ERRORS = 5;
	
	/**
	 * Add listener to receive proxy events.
	 * 
	 * @param listener listener to receive events
	 */
	public void addProxyEventListener(IProxyEventListener listener);
	
	/**
	 * Get the port that has been allocated for incoming connections.
	 * 
	 * @return port number
	 */
	public int getSessionPort();
	
	/**
	 * Check if the client is ready to send commands
	 * 
	 * @return true if client is ready to send commands
	 */
	public boolean isReady();

	/**
	 * Generate a new transaction ID.
	 * 
	 * @return new transaction ID
	 */
	public int newTransactionID();	
	
	/**
	 * Remove listener from receiving proxy events
	 * 
	 * @param listener listener to remove
	 */
	public void removeProxyEventListener(IProxyEventListener listener);
	
	/**
	 * Send a command to the proxy server. 
	 * 
	 * @param command command to send
	 * @throws IOException
	 */
	public void sendCommand(IProxyCommand command) throws IOException;
	
	/**
	 * Connect to a remote proxy server. This is not currently implemented.
	 * 
	 * @return
	 */
	public int sessionConnect();
	
	/**
	 * Convenience method. Same as sessionCreate(0, 0)
	 * 
	 * @throws IOException
	 */
	public void sessionCreate() throws IOException;	
	
	/**
	 * Convenience method. Same as sessionCreate(0, timeout)
	 * 
	 * @param timeout
	 * @throws IOException
	 */
	public void sessionCreate(int timeout) throws IOException;
	
	/**
	 * Create a proxy session. This starts a thread that waits for an incoming proxy connection.
	 * If the connection is successful, then an event thread is started.
	 * 
	 * On a successful return one of three events are guaranteed to be generated:
	 * 
	 * ProxyConnectedEvent	if the incoming connection succeeded
	 * ProxyTimeoutEvent	if no connection is established before the timeout expires
	 * ProxyErrorEvent		if the accept fails or is canceled
	 * 
	 * @param	port		port number to use for incoming connection (0 = autogenerate)
	 * @param	timeout		delay (in ms) to wait for incoming connection (0 = wait forever)
	 * @throws	IOException	if accept thread fails to start 
	 */
	public void sessionCreate(int port, int timeout) throws IOException;
	
	/**
	 * Attempt to shut down the proxy session regardless of state.
	 * 
	 * Events that can be generated as a result of sessionFinish() are:
	 * 
	 * ProxyErrorEvent			if sessionCreate() was waiting for an incoming connection
	 * ProxyDisconnectedEvent	if the proxy shut down successfully
	 * 
	 * @throws	IOException	if the session is already shut down
	 */
	public void sessionFinish() throws IOException;

}
