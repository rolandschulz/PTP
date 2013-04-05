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

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.internal.terminal.provisional.api.ITerminalControl;
import org.eclipse.tm.internal.terminal.provisional.api.Logger;
import org.eclipse.tm.internal.terminal.provisional.api.provider.TerminalConnectorImpl;

public class RemoteToolsConnector extends TerminalConnectorImpl {
	private OutputStream fOutputStream;
	private InputStream fInputStream;
	private RemoteToolsConnection fConnection;
	private final RemoteToolsSettings fSettings;

	public RemoteToolsConnector() {
		this(new RemoteToolsSettings());
	}

	public RemoteToolsConnector(RemoteToolsSettings settings) {
		fSettings = settings;
	}

	public void initialize() throws Exception {
	}

	public void connect(ITerminalControl control) {
		super.connect(control);
		fConnection = new RemoteToolsConnection(this, control);
		fConnection.start();
	}

	synchronized public void doDisconnect() {
		if (getInputStream() != null) {
			try {
				getInputStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
			}
		}

		if (getTerminalToRemoteStream() != null) {
			try {
				getTerminalToRemoteStream().close();
			} catch (Exception exception) {
				Logger.logException(exception);
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

	void setInputStream(InputStream inputStream) {
		fInputStream = inputStream;
	}

	void setOutputStream(OutputStream outputStream) {
		fOutputStream = outputStream;
	}

	/**
	 * Return the SSH Settings.
	 * 
	 * @return the settings for a concrete connection.
	 * @since org.eclipse.tm.terminal.ssh 2.0 renamed from getTelnetSettings()
	 */
	public IRemoteToolsSettings getSshSettings() {
		return fSettings;
	}

	public ISettingsPage makeSettingsPage() {
		return new RemoteToolsSettingsPage(fSettings);
	}

	public String getSettingsSummary() {
		return fSettings.getSummary();
	}

	public void load(ISettingsStore store) {
		fSettings.load(store);
	}

	public void save(ISettingsStore store) {
		fSettings.save(store);
	}

}
