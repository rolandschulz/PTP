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
package org.eclipse.ptp.remotetools.core;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Bean that holds configuration for a script that is to run remotely.
 * 
 * @author Richard Maciel, Daniel Ferber
 * @since 1.1 <b>Review OK</b>.
 */
public interface IRemoteScript {
	/**
	 * Add an environment variable to be used by the script.
	 * 
	 * @param variable
	 *            Variable name and value, informed as string like "variable=value"
	 */
	public void addEnvironment(String variable);

	public void addEnvironment(String[] environment);

	/**
	 * Clear environment. This will cause the script to clear the remote environment before adding the environment variables.
	 * 
	 * @since 5.0
	 */
	public void clearEnvironment();

	public void setAllocateTerminal(boolean flag);

	public void setFetchProcessErrorStream(boolean flag);

	public void setFetchProcessInputStream(boolean flag);

	public void setFetchProcessOutputStream(boolean flag);

	/**
	 * Set if X11 of the script will be forwarded to local host.
	 * 
	 * @param willForward
	 */
	public void setForwardX11(boolean willForward);

/**
	 * Set process error stream. Process stdout will write to this stream.
	 * <p>
	 * If not set, or if set to <code>null</code>, then the remote execution
	 * will create an inputstream where one may read data that the process has
	 * written to stderr.
	 * (see {@link IRemoteExecution.getInputStreamFromProcessErrorStream()).
	 * @param input Input stream or null to run without input.
	 */
	public void setProcessErrorStream(OutputStream output);

/**
	 * Set process input stream. Process stdin will read from this stream.
	 * <p>
	 * If not set, or if set to <code>null</code>, then the remote execution
	 * will create an outputstream where one may write data to the process stdin.
	 * (see {@link IRemoteExecution.getOutputStreamToProcessInputStream()).
	 * @param input Input stream or null to run without input.
	 */
	public void setProcessInputStream(InputStream input);

/**
	 * Set process output stream. Process stdout will write to this stream.
	 * <p>
	 * If not set, or if set to <code>null</code>, then the remote execution
	 * will create an inputstream where one may read data that the process has
	 * written to stdout.
	 * (see {@link IRemoteExecution.getInputStreamFromProcessOutputStream()).
	 * @param input Input stream or null to run without input.
	 */
	public void setProcessOutputStream(OutputStream output);

	/**
	 * Set a single bash command that will be run as script.
	 * 
	 * @param script
	 *            A bash script command.
	 */
	public void setScript(String script);

	/**
	 * Set a bash script (list of sequential commands) to be run. The script may contain while/for/if/switch construction.
	 * 
	 * @param script
	 *            An array of bash script lines.
	 */
	public void setScript(String[] script);

}
