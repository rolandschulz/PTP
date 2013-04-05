/*******************************************************************************
 * Copyright (c) 2009, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Mike Kucera (IBM) - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.internal.rdt.ui.RSEUtils;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.IRemoteUIConstants;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.RemoteUIServices;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

/**
 * @since 2.0
 */
public class RemoteBuildServiceFileLocationWidget extends Composite {

	private IRemoteConnection fRemoteConnection = null;

	// /private final Label label;
	private final Text text;
	private final Button browseButton;
	// private final Button validateButton;
	private final Button defaultButton;

	private final ListenerList pathListeners = new ListenerList();

	private final Map<String, String> previousSelections = new HashMap<String, String>();

	/**
	 * @since 5.0
	 */
	public RemoteBuildServiceFileLocationWidget(Composite parent, int style,
			IRemoteConnection initialConnection, String defaultPath) {
		super(parent, style);

		fRemoteConnection = initialConnection;

		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		this.setLayout(layout);
		this.setLayoutData(new GridData(GridData.FILL_BOTH));

		Group group = new Group(this, SWT.NONE);
		group.setText(Messages.getString("RemoteBuildServiceFileLocationWidget.1")); //$NON-NLS-1$
		group.setLayout(new GridLayout(2, false));
		group.setLayoutData(new GridData(GridData.FILL_BOTH));

		text = new Text(group, SWT.BORDER);
		GridData data = new GridData(GridData.FILL_HORIZONTAL);
		// data.widthHint = 300;
		data.horizontalSpan = 2;
		text.setLayoutData(data);
		text.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				String path = text.getText();

				previousSelections.put(key(fRemoteConnection), path);

				for (Object listener : pathListeners.getListeners()) {
					((IIndexFilePathChangeListener) listener).pathChanged(path);
				}
			}
		});

		browseButton = new Button(group, SWT.NONE);
		browseButton.setText(Messages.getString("IndexFileLocationWidget.1")); //$NON-NLS-1$
		browseButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		browseButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				browse();
			}
		});

		defaultButton = new Button(group, SWT.NONE);
		defaultButton.setText(Messages.getString("IndexFileLocationWidget.2")); //$NON-NLS-1$
		defaultButton.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		defaultButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				restoreDefault();
			}
		});

		if (defaultPath != null)
			text.setText(defaultPath);
	}

	public IRemoteConnection getConnection() {
		return fRemoteConnection;
	}

	// public void setRemoteConnection(IRemoteServices remoteServices,
	// IRemoteConnection connection) {
	// if(connection == null || remoteServices == null)
	// throw new IllegalArgumentException();
	//
	// fRemoteConnection = connection;
	// fRemoteServices = remoteServices;
	//
	// String path = previousSelections.get(key(remoteServices, connection));
	// if(path == null)
	// path = getDefaultPath(remoteServices, connection);
	// if(path == null)
	//			path = ""; //$NON-NLS-1$
	//
	// text.setText(path); // modify event listener updates map
	// }

	/**
	 * @since 5.0
	 */
	public static String getDefaultPath(IRemoteConnection connection) {
		// get the user's home directory
		if(connection != null && connection.isOpen()) {
			String homeDir = connection.getProperty(IRemoteConnection.USER_HOME_PROPERTY);
			if (homeDir != null) {
				IFileStore homeStore = connection.getRemoteServices().getFileManager(connection).getResource(homeDir);
				URI uri = homeStore.toURI();
				String pathString = EFSExtensionManager.getDefault().getPathFromURI(uri);
				if(pathString!=null){
					IPath path = new Path(pathString);
					path = path.append(RSEUtils.DEFAULT_CONFIG_DIR_NAME);
					return path.toString();
				}
			}
		}
		return null;
	}

	private static String key(IRemoteConnection connection) {
		return connection.getRemoteServices().getName() + ":" + connection.getName(); //$NON-NLS-1$
	}

	public String getConfigLocationPath() {
		return text.getText();
	}

	public void addPathListener(IIndexFilePathChangeListener listener) {
		pathListeners.add(listener);
	}

	public void removePathListener(IIndexFilePathChangeListener listener) {
		pathListeners.remove(listener);
	}

	private void browse() {
		IRemoteUIServices uiServices = RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
		IRemoteUIFileManager fmConnection = uiServices.getUIFileManager();
		fmConnection.setConnection(fRemoteConnection);
		
		String remotePath = fmConnection.browseDirectory(this.getShell(), Messages.getString("RemoteBuildServiceFileLocationWidget.0"), text.getText(), IRemoteUIConstants.NONE); //$NON-NLS-1$
 		if(remotePath != null)
 			text.setText(remotePath);
	}

	private void restoreDefault() {
		String defaultPath = getDefaultPath(fRemoteConnection);
		if (defaultPath == null) {
			defaultPath = ""; //$NON-NLS-1$
		}
		text.setText(defaultPath);
	}

	/**
	 * @since 5.0
	 */
	public void update(IRemoteConnection connection) {
		fRemoteConnection = connection;
		if (connection != null) {
			if (!connection.isOpen()) {
				boolean res = MessageDialog.openQuestion(getShell(), Messages.getString("RemoteBuildServiceFileLocationWidget.2"),  //$NON-NLS-1$
						NLS.bind(Messages.getString("RemoteBuildServiceFileLocationWidget.3"), connection.getName()));  //$NON-NLS-1$
				if (res) {
					IRemoteUIServices uiServices = RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
					uiServices.getUIConnectionManager().openConnectionWithProgress(getShell(), null, connection);
				}
			}
			String defaultPath = getDefaultPath(fRemoteConnection);
			if (defaultPath != null) {
				text.setText(defaultPath);
			}
		}
	}

}
