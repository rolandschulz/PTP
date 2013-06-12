/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core.exceptions;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

/**
 * An exception to wrap exceptions for which the Remote Sync services cannot recover.
 * 
 * @since 3.0
 */
public class RemoteSyncException extends CoreException {
	private static final String pluginID = "org.eclipse.ptp.rdt.sync.git.core"; //$NON-NLS-1$
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new instance of a RemoteSyncException
	 * 
	 * @param message
	 *            detail message
	 */
	public RemoteSyncException(String message) {
		super(new Status(IStatus.ERROR, pluginID, message));
	}

	/**
	 * Create a new instance of a RemoteSyncException
	 * 
	 * @param message
	 *            detail message
	 * @param cause
	 *            the cause
	 */
	public RemoteSyncException(String message, Throwable cause) {
		super(new Status(IStatus.ERROR, pluginID, message, cause));

	}

	/**
	 * Create a new instance of a RemoteSyncException
	 * 
	 * @param cause
	 *            the cause
	 */
	public RemoteSyncException(Throwable cause) {
		super(new Status(IStatus.ERROR, pluginID, (cause == null ? null : cause.toString()), cause));
	}

	/**
	 * Create a new instance of a RemoteSyncException
	 * 
	 * @param status
	 *            status object
	 */
	public RemoteSyncException(Status status) {
		super(status);
	}
}
