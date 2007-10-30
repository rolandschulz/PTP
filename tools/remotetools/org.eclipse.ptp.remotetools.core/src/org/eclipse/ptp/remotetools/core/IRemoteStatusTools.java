/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.core;

import java.util.Set;

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


/**
 * Provides access to several status for the Host and Remote machines
 * 
 * @author Richard Maciel
 *
 */
public interface IRemoteStatusTools {
	/**
	 * Verifies if the given address is being used in the remote host.
	 * 
	 * @param addr Address that will be checked
	 * @return True if the address is being used, false otherwise.
	 */
//	 TODO: Create this method
	//public boolean checkRemoteAddressUse(InetAddress addr);
	
	/**
	 * Verifies if the given mac address is being used in the remote host.
	 * 
	 * @param macAddr Mac address to be checked
	 * @return True if the mac address is in use, false otherwise.
	 */
	// TODO: Create this method
	//public boolean checkRemoteMacAddressUse(MacAddress macAddr);
	
	/**
	 * TCP protocol identification constant
	 */
	public static final int PROTO_TCP = 0;
	/**
	 * UDP protocol identification constant
	 */
	public static final int PROTO_UDP = 1;
	
	/**
	 * Returns a @link{Set} containing alloc'ed port numbers for the given
	 * protocol.
	 * This method considers that hosts which support both ipv4 and ipv6 
	 * has a single stack to handle both protocols.
	 * 
	 * @param protocol Transport protocol where the list of alloc'ed ports will come from 
	 * @return True if the port is in use, false otherwise.
	 * @throws RemoteConnectionException 
	 * @throws RemoteExecutionException 
	 * @throws RemoteOperationException 
	 * @throws CancelException 
	 */
	public Set getRemotePortsInUse(int protocol) throws RemoteConnectionException, RemoteOperationException, CancelException;

	/**
	 * Return the username of the user who logged into the system.
	 * 
	 * @return String containing the username.
	 * @throws CancelException 
	 * @throws RemoteOperationException 
	 * @throws RemoteConnectionException 
	 */
	public String getUsername() throws RemoteConnectionException, RemoteOperationException, CancelException;
	
	/**
	 * Return the user id of the user who logged into the system.
	 * 
	 * @return int containing the user id
	 * @throws CancelException 
	 * @throws RemoteOperationException 
	 * @throws RemoteConnectionException 
	 */
	public int getUserID() throws RemoteConnectionException, RemoteOperationException, CancelException;
	
	/**
	 * Return a {@link Set} of group ids for which the user belongs
	 *  
	 * @return Set containing the group ids (stored as Integer)
	 * @throws CancelException 
	 * @throws RemoteOperationException 
	 * @throws RemoteConnectionException 
	 */
	public Set getGroupIDSet() throws RemoteConnectionException, RemoteOperationException, CancelException;
	
	public long getTime() throws RemoteConnectionException, RemoteOperationException, CancelException;
	/*
	 * TODO: To the next version, add some methods to extract cpu info, memory size, disk size...
	 * 
	 * 
	 */
}
