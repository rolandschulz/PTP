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

package org.eclipse.ptp.proxy.runtime.client;

import java.io.IOException;


public interface IProxyRuntimeClient {
	/**
	 * Register for events
	 * 
	 * @param listener
	 */
	public void addProxyRuntimeEventListener(IProxyRuntimeEventListener listener);
	
	/**
	 * Unregister for events
	 * 
	 * @param listener
	 */
	public void removeProxyRuntimeEventListener(IProxyRuntimeEventListener listener);
	
	/**
	 * Shut down the proxy
	 * 
	 * @throws IOException if proxy fails to stop
	 */
	public void shutdown() throws IOException;
	
	/**
	 * Tell proxy to start sending events
	 * 
	 * @throws IOException 
	 */
	public void startEvents() throws IOException;
	
	/**
	 * Start up the proxy
	 * 
	 * @throws IOException if proxy fails to start
	 */
	public void startup() throws IOException;
	
	/**
	 * Tell proxy to stop sending events
	 * 
	 * @throws IOException
	 */
	public void stopEvents() throws IOException;
	
	/**
	 * Submit a job for execution
	 * 
	 * @param args
	 * @throws IOException
	 */
	public void submitJob(String[] args) throws IOException;
	
	/**
	 * Terminate a job
	 * 
	 * @param jobId
	 * @throws IOException
	 */
	public void terminateJob(String jobId) throws IOException;
}
