/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.remote.ui;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.RemoteServicesDelegate;
import org.eclipse.ptp.remote.ui.messages.Messages;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.swt.widgets.Shell;

/**
 * Convenience methods for handling various actions involving IRemoteUIServices.
 * 
 * @see org.eclipse.ptp.remote.ui.IRemoteUIServices
 * @see org.eclipse.ptp.remote.ui.IRemoteUIFileManager
 * @see org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager
 * @see org.eclipse.ptp.remote.core.RemoteServicesDelegate
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class RemoteUIServicesUtils {

	/**
	 * Opens a browse dialog using the indicated remote or local service and
	 * connection.
	 * 
	 * @param shell
	 *            for the dialog
	 * @param current
	 *            initial uri to display
	 * @param delegate
	 *            containing remote services data
	 * @param remote
	 *            whether to use the remote or the local connection and service
	 *            provided by the delegate
	 * @param readOnly
	 *            whether to disallow the user to type in a path (default is
	 *            <code>true</code>)
	 * @param dir
	 *            whether to browse/return a directory (default is file)
	 * @return the selected file path as URI or <code>null</code> if canceled
	 * @throws URISyntaxException
	 */
	public static URI browse(Shell shell, URI current, RemoteServicesDelegate delegate, boolean remote, boolean readOnly,
			boolean dir) throws URISyntaxException {
		IRemoteUIServices uIServices = null;
		IRemoteUIFileManager uiFileManager = null;
		IRemoteConnection conn = null;
		IRemoteFileManager manager = null;

		URI home = null;
		String path = null;
		int type = readOnly ? IRemoteUIConstants.OPEN : IRemoteUIConstants.SAVE;

		if (!remote) {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getLocalServices());
			uiFileManager = uIServices.getUIFileManager();
			manager = delegate.getLocalFileManager();
			conn = delegate.getLocalConnection();
			home = delegate.getLocalHome();
		} else {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getRemoteServices());
			uiFileManager = uIServices.getUIFileManager();
			manager = delegate.getRemoteFileManager();
			conn = delegate.getRemoteConnection();
			home = delegate.getRemoteHome();
		}

		path = (current == null) ? home.getPath() : current.getPath();

		String title = dir ? Messages.RemoteResourceBrowser_directoryTitle : Messages.RemoteResourceBrowser_fileTitle;
		try {
			uiFileManager.setConnection(conn);
			uiFileManager.showConnections(remote);
			if (dir) {
				path = uiFileManager.browseDirectory(shell, title, path, type);
			} else {
				path = uiFileManager.browseFile(shell, title, path, type);
			}
		} catch (Throwable t) {
			PTPRemoteUIPlugin.log(t);
		}

		if (path == null) {
			return null;
		}

		return manager.toURI(path);
	}

	/**
	 * Used to configure the default host and port in the wizard used for
	 * choosing a resource manager connection.
	 * 
	 * @see org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget
	 * 
	 * @param connectionWidget
	 *            the widget allowing the user to choose the connection
	 * @param connection
	 *            name of the connection
	 * @throws URISyntaxException
	 */
	public static void setConnectionHints(RemoteConnectionWidget connectionWidget, IRemoteConnection connection)
			throws URISyntaxException {
		Map<String, String> result = new HashMap<String, String>();
		result.put(IRemoteUIConnectionManager.CONNECTION_ADDRESS_HINT, connection.getAddress());
		result.put(IRemoteUIConnectionManager.LOGIN_USERNAME_HINT, connection.getUsername());
		result.put(IRemoteUIConnectionManager.CONNECTION_PORT_HINT, String.valueOf(connection.getPort()));
		String[] hints = new String[result.size()];
		String[] defaults = new String[hints.length];
		int i = 0;
		for (String s : result.keySet()) {
			hints[i] = s;
			defaults[i++] = result.get(s);
		}
		connectionWidget.setHints(hints, defaults);
	}
}
