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
package org.eclipse.ptp.rdt.sync.core;

// Used for cases when a remote command fails. If it fails with an unexpected non-zero exit code, include the error message in the
// exception. If failure is indicated by another exception, nest the other exception.
public class RemoteExecutionException extends Exception {
	private static final long serialVersionUID = 1L;

	public RemoteExecutionException() {
		super();
	}

	public RemoteExecutionException(String arg0) {
		super(arg0);
	}

	public RemoteExecutionException(String arg0, Throwable arg1) {
		super(arg0, arg1);
	}

	public RemoteExecutionException(Throwable arg0) {
		super(arg0);
	}
}