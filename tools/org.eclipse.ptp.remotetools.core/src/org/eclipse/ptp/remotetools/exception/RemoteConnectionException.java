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

public class RemoteConnectionException extends RemoteException {
	private static final long serialVersionUID = 1L;

	int errorCode = UNKNOWN;
	public static final int UNKNOWN = 0;
	public static final int LOST_CONNECTION = 1;

	public RemoteConnectionException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RemoteConnectionException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RemoteConnectionException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public RemoteConnectionException(int errorCode) {
		super();
		this.errorCode = errorCode;
	}

	public int getErrorCode() {
		return errorCode;
	}

	@Override
	public String getLocalizedMessage() {
		return super.getMessage();
	}
}
