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

package org.eclipse.ptp.proxy.event;

public interface IProxyEvent {
	public static final int OK = 0;
	public static final int MESSAGE = 1;
	public static final int CONNECTED = 2;
	public static final int DISCONNECTED = 3;
	public static final int TIMEOUT = 4;
	public static final int ERROR = 5;
	public static final int SHUTDOWN = 6;
		
	/**
	 * Get the event ID (type)
	 * 
	 * @return event ID
	 */
	public int getEventID();
	
	/**
	 * Get the transaction ID
	 * 
	 * @return transaction ID
	 */
	public int getTransactionID();
	
	/**
	 * Get the event attributes
	 * @return event attributes
	 */
	public String[] getAttributes();
}
