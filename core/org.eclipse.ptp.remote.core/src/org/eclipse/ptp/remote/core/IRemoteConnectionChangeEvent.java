/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.core;

public interface IRemoteConnectionChangeEvent {	
	/**
	 * Event indicating that the connection was closed.
	 */
	public static final int CONNECTION_CLOSED = 1;
	
	/**
	 * Event indicating that the connection was opened.
	 */
	public static final int CONNECTION_OPENED = 2;
	
	/**
	 * Event indicating that the connection was closed abnormally.
	 */
	public static final int CONNECTION_ABORTED = 4;

	/**
	 * Get the connection that has changed.
	 * 
	 * @return IRemoteConnection
	 */
	public IRemoteConnection getConnection();
	
	/**
	 * Returns the type of event being reported.
	 *
	 * @return one of the event type constants
	 * @see #CONNECTION_CLOSED
	 * @see #CONNECTION_OPENED
	 * @see #CONNECTION_ABORTED
	 */
	public int getType();
}
