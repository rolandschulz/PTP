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
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.remotetools.core.IRemoteScript;



/**
 * Default implementation of <code>IRemoteScript</code>.
 * @author Richard Maciel
 * <b>Review OK</b>
 */
public class RemoteScript implements IRemoteScript
{
	private List environment;
	
	private boolean willForwardX11;
	
	private InputStream inputStream = null;
	private OutputStream outputStream = null;
	private OutputStream errorStream = null;
	
	String [] script;

	private boolean fetchProcessErrorStream = false;
	private boolean fetchProcessInputStream = false;
	private boolean fetchProcessOutputStream = false;
	
	private boolean allocateTerminal = false;
	
	public RemoteScript()
	{
		environment = new LinkedList();
		inputStream = null;
		outputStream = null;
		willForwardX11 = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#addEnvironment(java.lang.String)
	 */
	public void addEnvironment(String variable)
	{
		environment.add(variable);
	}
	
	public void addEnvironment(String[] environment) {
		this.environment.addAll(Arrays.asList(environment));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setProcessInputStream(java.io.InputStream)
	 */
	public void setProcessInputStream(InputStream input)
	{
		inputStream = input;
		fetchProcessInputStream = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setProcessOutputStream(java.io.OutputStream)
	 */
	public void setProcessOutputStream(OutputStream output)
	{
		outputStream = output;
		fetchProcessOutputStream = false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteScript#setProcessErrorStream(java.io.OutputStream)
	 */
	public void setProcessErrorStream(OutputStream output) {
		errorStream = output;
		fetchProcessErrorStream = false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setForwardX11(boolean)
	 */
	public void setForwardX11(boolean willForward)
	{
		this.willForwardX11 = willForward;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setScript(java.lang.String)
	 */
	public void setScript(String script)
	{
		this.script = new String[1];
		this.script[0] = script;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.IRemoteScript#setScript(java.lang.String[])
	 */
	public void setScript(String[] script)
	{
		this.script = script;
	}

	public String getScriptString()
	{
		if(script == null)
		{
			return null;
		}

		StringBuffer sb = new StringBuffer();
				
		Iterator it = environment.iterator();
		if(environment.size() > 0)
			sb.append("declare -x "); //$NON-NLS-1$
		while (it.hasNext()) { 
			sb.append("\"" + (String)it.next() + "\" "); //$NON-NLS-1$ //$NON-NLS-2$
		}
		if(environment.size() > 0)
			sb.append("; "); //$NON-NLS-1$
		
		for(int i = 0; i < script.length; i++)
			sb.append(script[i] + "; "); //$NON-NLS-1$
		
		return sb.toString();
	}

	public InputStream getInputStream()
	{
		return inputStream;
	}

	public OutputStream getOutputStream()
	{
		return outputStream;
	}

	public OutputStream getErrorStream()
	{
		return errorStream;
	}

	public boolean willForwardX11()
	{
		return willForwardX11;
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
	
	public boolean getFetchProcessErrorStream() {
		return fetchProcessErrorStream;
	}
	
	public boolean getFetchProcessOutputStream() {
		return fetchProcessOutputStream;
	}

	public boolean getFetchProcessInputStream() {
		return fetchProcessInputStream;
	}

	public void setAllocateTerminal(boolean flag) {
		this.allocateTerminal = flag;	
	}
	
	public boolean getAllocateTerminal() {
		return allocateTerminal;
	}
}
