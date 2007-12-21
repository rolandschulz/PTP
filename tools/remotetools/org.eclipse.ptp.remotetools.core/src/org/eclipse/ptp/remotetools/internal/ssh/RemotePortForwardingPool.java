/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.ptp.remotetools.exception.PortForwardingException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;

import com.jcraft.jsch.JSchException;

/**
 * Keeps references to all active port forwardings of a connection.
 * This forwardings are shared by all execution managers, since it is a ressource that applies only to the connection.
 * 
 * @author dfferber
 *
 */
public class RemotePortForwardingPool {
	/**
	 * Remote port to local forwarding that are active on the Connection.
	 */
	HashMap<Integer, RemotePortForwarding> remotePortForwardings = new HashMap<Integer, RemotePortForwarding>();;
	
	/**
	 * Connection
	 */
	Connection connection;
	
	RemotePortForwardingPool(Connection connection) {
		assert connection != null;
		
		this.connection = connection;
	}
	
	RemotePortForwarding createRemotePortForwarding(ExecutionManager owner, int remotePort, String localAddress, int localPort) throws RemoteConnectionException, PortForwardingException {
		assert (remotePort > 0);
		assert (localPort > 0) ;
		assert (owner != null);
		assert (connection.defaultSession != null);
		
		synchronized (remotePortForwardings) {
			/* 
			 * Test if the forwarding is not already active. 
			 */
			Integer remotePortInteger = new Integer(remotePort);
			RemotePortForwarding remotePortForwarding =  (RemotePortForwarding) remotePortForwardings.get(remotePortInteger);
			if (remotePortForwarding != null) {
				throw new PortForwardingException(PortForwardingException.REMOTE_PORT_ALREADY_FORWARDED);
			}
			
			/*
			 * Create a port forwarding in Jsch.
			 */
			try {
				this.connection.defaultSession.setPortForwardingR(remotePort, localAddress, localPort);
			} catch (JSchException e) {
				throw new PortForwardingException(PortForwardingException.REMOTE_FORWARDING_FAILED, e);
			}
			
			/*
			 * Create new forwarding instance and add it to the table.
			 */
			remotePortForwarding = new RemotePortForwarding(owner, localPort, localAddress, remotePort);
			remotePortForwardings.put(remotePortInteger, remotePortForwarding);
			
			return remotePortForwarding;
		}
	}
	
	void releaseRemotePortForwarding(RemotePortForwarding remoteForwarding, ExecutionManager owner)  throws RemoteConnectionException, PortForwardingException {
		assert remoteForwarding != null;
		assert (connection.defaultSession != null);
		
		synchronized (remotePortForwardings) {
			/*
			 * Assure the port forwarding that is still active.
			 */
			if (! remoteForwarding.isActive()) {
				throw new PortForwardingException(PortForwardingException.REMOTE_FORWARDING_NOT_ATIVE);
			}
			
			/*
			 * If a execution manager is given as owner, then assure the port forwarding belongs to it.
			 */
			if (owner != null) {
				if (remoteForwarding.owner != owner) {
					throw new PortForwardingException(PortForwardingException.REMOTE_FORWARDING_NOT_ATIVE);
				}
			}
			
			Integer remotePortInteger = new Integer(remoteForwarding.remotePort);
			assert remotePortForwardings.containsKey(remotePortInteger);
			
			turnOffRemotePortForwarding(remoteForwarding);
			remotePortForwardings.remove(remotePortInteger);
		}
	}
	
	RemotePortForwarding getRemotePortForwarding(int remotePort) throws RemoteConnectionException, PortForwardingException {
		assert remotePort > 0;
		assert (connection.defaultSession != null);

		synchronized (remotePortForwardings) {
			Integer remotePortInteger = new Integer(remotePort);
			return (RemotePortForwarding) remotePortForwardings.get(remotePortInteger);
		}
		
	}

	void disconnect() {
		Iterator<RemotePortForwarding> iterator = remotePortForwardings.values().iterator();
		while (iterator.hasNext()) {
			RemotePortForwarding forwarding = iterator.next();
				turnOffRemotePortForwarding(forwarding);
				iterator.remove();
		}
	}

	
	void disconnect(ExecutionManager executionManager) {
		Iterator<RemotePortForwarding> iterator = remotePortForwardings.values().iterator();
		while (iterator.hasNext()) {
			RemotePortForwarding forwarding = iterator.next();
			if (forwarding.owner == executionManager) {
				turnOffRemotePortForwarding(forwarding);
				iterator.remove();
			}
		}
	}
	
	/**
	 * Auxiliary method used by other methods to turn off a port forwarding.
	 * @param forwarding The port remote forwarding to be turned off.
	 */
	private void turnOffRemotePortForwarding(RemotePortForwarding forwarding) {
		forwarding.releaseOwner();
		try {
			connection.defaultSession.delPortForwardingR(forwarding.remotePort);
		} catch (JSchException e) {
			// No possible exceptions are known.
			assert false;
		}
	}

}
