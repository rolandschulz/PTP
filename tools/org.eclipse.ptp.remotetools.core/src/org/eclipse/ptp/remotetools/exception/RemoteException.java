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

abstract public class RemoteException extends Exception
{

	public RemoteException() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RemoteException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	public RemoteException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public RemoteException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

}
