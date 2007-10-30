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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;


public interface IRemoteScriptExecution extends IRemoteOperation
{	
	/**
	 * Process return exit status.
	 */
	public int getReturnCode();
	
	/**
	 * Get the input stream (user's perspective)
	 * @throws IOException 
	 * 
	 */
	public InputStream getInputStreamFromProcessOutputStream() throws IOException;
	
	/**
	 * Get the input stream (user's perspective)
	 * @throws IOException 
	 * 
	 */
	public InputStream getInputStreamFromProcessErrorStream() throws IOException;

	/**
	 * Get the output stream (user's perspective)
	 * 
	 */
	public OutputStream getOutputStreamToProcessInputStream() throws IOException;
	
	/**
	 * Free allocated resources for the execution.
	 */
	public void close();
}
