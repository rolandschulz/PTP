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

package org.eclipse.ptp.core.proxy.event;

public interface IProxyEvent {
	public static final int EVENT_OK = 0;
	public static final int EVENT_MESSAGE = 1;
	public static final int EVENT_CONNECTED = 2;
	public static final int EVENT_DISCONNECTED = 3;
	public static final int EVENT_TIMEOUT = 4;
	
	/*
	 * Sizeof encoded values (bytes)
	 */
	public static final int EVENT_LENGTH_SIZE = 8;
	public static final int EVENT_ID_SIZE = 4;
	public static final int EVENT_TRANS_ID_SIZE = 8;
	public static final int EVENT_NARGS_SIZE = 8;
	public static final int EVENT_ARG_LEN_SIZE = 8;
	
	public int getEventID();
	public int getTransactionID();
	public String[] getAttributes();
}
