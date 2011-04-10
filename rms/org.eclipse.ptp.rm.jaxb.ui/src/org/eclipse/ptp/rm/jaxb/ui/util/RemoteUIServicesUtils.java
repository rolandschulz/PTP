/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.PTPRemoteUIPlugin;
import org.eclipse.ptp.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.rm.jaxb.core.utils.FileUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Shell;

public class RemoteUIServicesUtils implements IJAXBUINonNLSConstants {

	public static URI browse(Shell shell, URI current, RemoteServicesDelegate delegate, boolean remote, boolean readOnly)
			throws URISyntaxException {
		IRemoteUIServices uIServices = null;
		IRemoteUIFileManager uiFileManager = null;
		IRemoteConnection conn = null;
		URI home = null;
		String path = null;
		int type = readOnly ? IRemoteUIConstants.OPEN : IRemoteUIConstants.SAVE;

		if (!remote) {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getLocalServices());
			uiFileManager = uIServices.getUIFileManager();
			conn = delegate.getLocalConnection();
			home = delegate.getLocalHome();
		} else {
			uIServices = PTPRemoteUIPlugin.getDefault().getRemoteUIServices(delegate.getRemoteServices());
			uiFileManager = uIServices.getUIFileManager();
			conn = delegate.getRemoteConnection();
			home = delegate.getRemoteHome();
		}

		path = current == null ? home.getPath() : current.getPath();

		try {
			uiFileManager.setConnection(conn);
			uiFileManager.showConnections(remote);
			path = uiFileManager.browseFile(shell, Messages.JAXBRMConfigurationSelectionWizardPage_0, path, type);
		} catch (Throwable t) {
			t.printStackTrace();
		}

		if (path == null) {
			return current;
		}

		return new URI(home.getScheme(), home.getUserInfo(), home.getHost(), home.getPort(), path, home.getQuery(),
				home.getFragment());
	}

	public static IRemoteServices[] getAvailableServices() {
		IRemoteServices[] remoteServices = PTPRemoteUIPlugin.getDefault().getRemoteServices(null);
		Arrays.sort(remoteServices, new Comparator<IRemoteServices>() {
			public int compare(IRemoteServices c1, IRemoteServices c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		return remoteServices;
	}

	public static String getFileContents(URI file, RemoteServicesDelegate delegate) throws Throwable {
		SubMonitor progress = SubMonitor.convert(new NullProgressMonitor(), 100);
		if (FILE_SCHEME.equals(file.getScheme())) {
			return FileUtils.read(delegate.getLocalFileManager(), file.getPath(), progress);
		} else {
			return FileUtils.read(delegate.getRemoteFileManager(), file.getPath(), progress);
		}
	}

	public static URI getUserHome() {
		return new File(System.getProperty(JAVA_USER_HOME)).toURI();
	}

	public static void setConnectionHints(RemoteConnectionWidget connectionWidget, String connection) throws URISyntaxException {
		String host = null;
		String port = null;
		if (connection != null) {
			if (connection != null) {
				URI uri = new URI(connection);
				host = uri.getHost();
				int p = uri.getPort();
				if (p != UNDEFINED) {
					port = String.valueOf(p);
				}
			}
		}
		Map<String, String> result = new HashMap<String, String>();
		if (host != null && !ZEROSTR.equals(host)) {
			result.put(IRemoteUIConnectionManager.CONNECTION_ADDRESS_HINT, host);
		}
		if (port != null && !ZEROSTR.equals(port)) {
			result.put(IRemoteUIConnectionManager.CONNECTION_PORT_HINT, port);
		}
		String[] hints = new String[result.size()];
		String[] defaults = new String[hints.length];
		int i = 0;
		for (String s : result.keySet()) {
			hints[i] = s;
			defaults[i++] = result.get(s);
		}
		connectionWidget.setHints(hints, defaults);
	}

	public static URI writeContentsToFile(Shell shell, String contents, URI file, RemoteServicesDelegate delegate) throws Throwable {
		IRemoteFileManager manager = null;
		String path = file.getPath();

		path = WidgetActionUtils.openInputDialog(shell, Messages.RenameFile, Messages.RenameFileTitle, path);

		if (path == null) {
			return null;
		}

		if (FILE_SCHEME.equals(file.getScheme())) {
			manager = delegate.getLocalFileManager();
		} else {
			manager = delegate.getRemoteFileManager();
		}

		SubMonitor progress = SubMonitor.convert(new NullProgressMonitor(), 100);
		FileUtils.write(manager, path, contents, progress);

		return new URI(file.getScheme(), file.getUserInfo(), file.getHost(), file.getPort(), path, file.getQuery(),
				file.getFragment());
	}
}
