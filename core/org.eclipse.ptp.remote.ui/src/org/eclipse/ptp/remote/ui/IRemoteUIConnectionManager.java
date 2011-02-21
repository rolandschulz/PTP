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
	 * @since 5.0
	 */
	public static String CONNECTION_ADDRESS_HINT = "CONNECTION_ADDRESS_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String CONNECTION_PORT_HINT = "CONNECTION_PORT_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String CONNECTION_TIMEOUT_HINT = "CONNECTION_TIMEOUT_HINT"; //$NON-NLS-1$
	/**
	 * @since 5.0
	 */
	public static String LOGIN_USERNAME_HINT = "LOGIN_USERNAME_HINT"; //$NON-NLS-1$

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
	 * Create a new connection using the remote service provider new connection
	 * dialog. If attrHints and attrHintValues are provided then the dialog will
	 * attempt to use these values as the default values for the appropriate
	 * dialog fields.
	 * 
	 * @param shell
	 *            shell used to display dialog
	 * @param attrHints
	 *            array containing attribute hints
	 * @param attrHintValues
	 *            array containing default values for each attribute specified
	 *            in attrHints
	 * @return
	 * @since 5.0
	 */
	public IRemoteConnection newConnection(Shell shell, String[] attrHints, String[] attrHintValues);

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
