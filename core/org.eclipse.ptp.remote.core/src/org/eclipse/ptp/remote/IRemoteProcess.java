/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote;

import java.io.InputStream;
import java.io.OutputStream;

public interface IRemoteProcess {
	/**
	 * 
	 */
	public void destroy();

	/**
	 * @return
	 */
	public int exitValue();

	/**
	 * @return
	 */
	public InputStream getErrorStream();

	/**
	 * @return
	 */
	public InputStream getInputStream();

	/**
	 * @return
	 */
	public OutputStream getOutputStream();

	/**
	 * @return
	 * @throws InterruptedException
	 */
	public int waitFor() throws InterruptedException;
	
	/**
	 * Check if the remote process has completed
	 * 
	 * @return true if remote process has completed
	 */
	public boolean isCompleted();
}
