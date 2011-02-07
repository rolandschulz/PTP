/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.internal.common;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.ptp.remotetools.core.IRemoteScript;

/**
 * Default implementation of <code>IRemoteScript</code>.
 * 
 * @author Richard Maciel <b>Review OK</b>
 */
public class RemoteScript implements IRemoteScript {
	private final List<String> environment = new ArrayList<String>();

	private boolean willForwardX11;

	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private OutputStream errorStream = null;

	private String[] script;

	private boolean fetchProcessErrorStream = false;
	private boolean fetchProcessInputStream = false;
	private boolean fetchProcessOutputStream = false;

	private boolean allocateTerminal = false;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteScript#addEnvironment(java.lang.String
	 * )
	 */
	public void addEnvironment(String variable) {
		environment.add(variable);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteScript#addEnvironment(java.lang
	 * .String[])
	 */
	public void addEnvironment(String[] variables) {
		environment.addAll(Arrays.asList(variables));
	}

	public boolean getAllocateTerminal() {
		return allocateTerminal;
	}

	public OutputStream getErrorStream() {
		return errorStream;
	}

	public boolean getFetchProcessErrorStream() {
		return fetchProcessErrorStream;
	}

	public boolean getFetchProcessInputStream() {
		return fetchProcessInputStream;
	}

	public boolean getFetchProcessOutputStream() {
		return fetchProcessOutputStream;
	}

	public InputStream getInputStream() {
		return inputStream;
	}

	public OutputStream getOutputStream() {
		return outputStream;
	}

	public String getScriptString() {
		if (script == null) {
			return null;
		}

		StringBuffer sb = new StringBuffer();

		for (String env : environment) {
			sb.append("export \"" + env + "\"; "); //$NON-NLS-1$ //$NON-NLS-2$
		}

		for (int i = 0; i < script.length; i++) {
			sb.append(script[i] + "; "); //$NON-NLS-1$
		}
		return sb.toString();
	}

	public void setAllocateTerminal(boolean flag) {
		this.allocateTerminal = flag;
	}

	public void setFetchProcessErrorStream(boolean flag) {
		this.fetchProcessErrorStream = flag;
		this.errorStream = null;
	}

	public void setFetchProcessInputStream(boolean flag) {
		this.fetchProcessInputStream = flag;
		this.inputStream = null;
	}

	public void setFetchProcessOutputStream(boolean flag) {
		this.fetchProcessOutputStream = flag;
		this.outputStream = null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setForwardX11(boolean)
	 */
	public void setForwardX11(boolean willForward) {
		this.willForwardX11 = willForward;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.core.IRemoteScript#setProcessErrorStream(
	 * java.io.OutputStream)
	 */
	public void setProcessErrorStream(OutputStream output) {
		errorStream = output;
		fetchProcessErrorStream = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteScript#setProcessInputStream(java.
	 * io.InputStream)
	 */
	public void setProcessInputStream(InputStream input) {
		inputStream = input;
		fetchProcessInputStream = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteScript#setProcessOutputStream(java
	 * .io.OutputStream)
	 */
	public void setProcessOutputStream(OutputStream output) {
		outputStream = output;
		fetchProcessOutputStream = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteScript#setScript(java.lang.String)
	 */
	public void setScript(String script) {
		this.script = new String[1];
		this.script[0] = script;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remotetools.IRemoteScript#setScript(java.lang.String[])
	 */
	public void setScript(String[] script) {
		this.script = script;
	}

	public boolean willForwardX11() {
		return willForwardX11;
	}
}
