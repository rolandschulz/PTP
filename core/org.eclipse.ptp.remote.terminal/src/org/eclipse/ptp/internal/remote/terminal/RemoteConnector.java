/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public class RemoteConnector extends TerminalConnectorImpl {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private RemoteConnection fConnection;
	private final RemoteSettings fSettings;

	public RemoteConnector() {
		this(new RemoteSettings());
	}

	public RemoteConnector(RemoteSettings settings) {
		fSettings = settings;
	}

	public void initialize() throws Exception {
	}

	public void connect(ITerminalControl control) {
		super.connect(control);
		fConnection = new RemoteConnection(this, control);
		fConnection.start();
	}
	
	public IProject getProject() {
		String projectName = fSettings.getProjectName();
		if (projectName != null && !"".equals(projectName.trim())) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			return root.getProject(projectName);
		}
		return null;
	}

	public synchronized void doDisconnect() {
		if (getInputStream() != null) {
			try {
				getInputStream().close();
			} catch (Exception exception) {
				Activator.log(exception);
			}
		}

		if (getTerminalToRemoteStream() != null) {
			try {
				getTerminalToRemoteStream().close();
			} catch (Exception exception) {
				Activator.log(exception);
			}
		}
	}

	public void setTerminalSize(int newWidth, int newHeight) {
		// if (fChannel != null && (newWidth != fWidth || newHeight != fHeight)) {
		// // avoid excessive communications due to change size requests by caching previous size
		// fChannel.setPtySize(newWidth, newHeight, 8 * newWidth, 8 * newHeight);
		// fWidth = newWidth;
		// fHeight = newHeight;
		// }
	}

	public InputStream getInputStream() {
		return fInputStream;
	}

	public OutputStream getTerminalToRemoteStream() {
		return fOutputStream;
	}

	public void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}

	public void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}

	/**
	 * Return the SSH Settings.
	 * 
	 * @return the settings for a concrete connection.
	 * @since org.eclipse.tm.terminal.ssh 2.0 renamed from getTelnetSettings()
	 */
	public IRemoteSettings getSshSettings() {
		return fSettings;
	}

	public ISettingsPage makeSettingsPage() {
		return new RemoteSettingsPage(fSettings);
	}

	public String getSettingsSummary() {
		return fSettings.getSummary();
	}

	@SuppressWarnings("restriction")
	public void load(ISettingsStore store) {
		fSettings.load(store);
	}

	@SuppressWarnings("restriction")
	public void save(ISettingsStore store) {
		fSettings.save(store);
	}

}
