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

package org.eclipse.ptp.core.proxy;

import java.io.IOException;

import org.eclipse.ptp.core.proxy.event.IProxyEventListener;

public interface IProxyClient {
	/* wire protocol version */
	public static final String WIRE_PROTOCOL_VERSION = "2.0";
	public static final int MAX_ERRORS = 5;
	
	public int newTransactionID();
	public boolean isReady();
	public void sendCommand(String command) throws IOException;

	public void addProxyEventListener(IProxyEventListener listener);	
	public void removeProxyEventListener(IProxyEventListener listener);
	
	public int sessionConnect();
	public void sessionCreate() throws IOException;
	public void sessionCreate(int timeout) throws IOException;	
	public void sessionCreate(int port, int timeout) throws IOException;
	public int getSessionPort();
	public String getSessionHost();
	public void sessionFinish() throws IOException;

}
