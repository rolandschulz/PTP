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

/**
 * Used for cases when a remote command fails. If it fails with an unexpected non-zero exit code, include the error message in the
 * exception. If failure is indicated by another exception, nest the other exception and include its error message.
 * 
 * @since 3.0
 */
public class RemoteExecutionException extends Exception {
	private static final long serialVersionUID = 1L;

	/**
	 * Create a new instance of a RemoteExecutionException
	 */
	public RemoteExecutionException() {
		super();
	}

	/**
	 * Create a new instance of a RemoteExecutionException
	 * 
	 * @param message
	 *            detail message
	 */
	public RemoteExecutionException(String message) {
		super(message);
	}

	/**
	 * Create a new instance of a RemoteExecutionException
	 * 
	 * @param message
	 *            detail message
	 * @param cause
	 *            the cause
	 */
	public RemoteExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * Create a new instance of a RemoteExecutionException
	 * 
	 * @param cause
	 *            the cause
	 */
	public RemoteExecutionException(Throwable cause) {
		super(cause);
	}
}