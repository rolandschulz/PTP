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

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;

/**
 * The common interface to all operations that can be executed on the remote host and that
 * may require time to finish.
 * <p>
 * Allows to block the current thread until the operation finishes on the remote host.
 * Allows to cancel the operation on the remote host (not all operation may implement this).
 * @author Richard Maciel, Daniel Felix Ferber
 * <b>Review OK.</b>
 */
public interface IRemoteOperation
{	
	/**
	 * Wait until the operation is finished or until
	 * Canceled by another thread.
	 * if canceled, then the result of the execution will probably be undefined.
	 * 
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The script execution was canceled by another thread.
	 */
	public void waitForEndOfExecution() throws RemoteConnectionException, CancelException, RemoteExecutionException;

	/**
	 * Check if the operation finished because it was canceled.
	 * @return True if the last operation was canceled.
	 */
	public boolean wasCanceled();

	/**
	 * Use this method to verify if the operation on the remote host did already finish or not.
	 * @return true if the operation finished execution.
	 */
	public boolean wasFinished();
	
	/**
	 * Cancel the operation on the remote host.
	 */
	public void cancel();


	/**
	 * Free allocated resources for the execution.
	 */
	public void close();
	
	
	/**
	 * Return the meaning of the exit value.
	 */
	public int getFinishStatus();
	
	/**
	 * Get a text that describes the finish status code.
	 */
	public String getFinishStatusText(int status);

	public boolean isException(int status);
	public boolean isOK(int status);
	public boolean isExecutableError(int status);
	
	public boolean wasException();
	public boolean wasOK();
	public boolean wasCommandError();

}
