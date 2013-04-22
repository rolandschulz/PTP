/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others.
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
 * Exception for a missing remote connection. A specific exception is defined as this situation often requires special handling.
 * Normally, it should be expected to happen on occasion, and the client should recover gracefully.
 * 
 * @since 3.0
 */
public class MissingConnectionException extends Exception {
	private static final long serialVersionUID = 1L;
	private final String connectionName;

	public MissingConnectionException(String name) {
		super();
		connectionName = name;
	}

	public MissingConnectionException(String name, String message) {
		super(message);
		connectionName = name;
	}

	/**
	 * Get connection name
	 * 
	 * @return
	 */
	public String getConnectionName() {
		return connectionName;
	}
}
