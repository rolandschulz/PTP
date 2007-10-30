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
package org.eclipse.ptp.remotetools.exception;

/**
 * Specific RemoteOperationException that arises when creating a tunnel 
 * that binds to a local port that is already alloc'ed.
 * 
 * @author Richard Maciel
 *
 */
public class LocalPortBoundException extends RemoteOperationException {
	private static final long serialVersionUID = 5497137298332719485L;

	/**
	 * @param message
	 * @param cause
	 */
	public LocalPortBoundException(String message, Throwable cause) {
		super(message, cause);
	}

	/**
	 * @param message
	 */
	public LocalPortBoundException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public LocalPortBoundException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
