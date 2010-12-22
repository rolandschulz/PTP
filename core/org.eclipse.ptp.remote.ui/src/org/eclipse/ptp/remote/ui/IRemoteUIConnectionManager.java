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
package org.eclipse.ptp.remote.ui;

import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.swt.widgets.Shell;

public interface IRemoteUIConnectionManager {
	/**
	 * Create a new connection. The implementation can chose to do this in any
	 * way, but typically will use a dialog or wizard.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @return newly created remote connection
	 */
	public IRemoteConnection newConnection(Shell shell);

	/**
	 * Attempt to open a connection using a progress monitor. Can be called on
	 * either open or closed connections. Users should check connection.isOpen()
	 * on return to determine if the connection was actually opened.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @param connection
	 *            connection to open
	 * @since 5.0
	 */
	public void openConnectionWithProgress(Shell shell, IRunnableContext context, IRemoteConnection connection);

	/**
	 * Change a connection configuration. The implementation can chose to do
	 * this in any way, but typically will use a dialog or wizard.
	 * 
	 * @param shell
	 *            shell used to display dialogs
	 * @param connection
	 *            connection to modify
	 */
	public void updateConnection(Shell shell, IRemoteConnection connection);
}
