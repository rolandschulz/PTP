/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.core.runnable.command;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Subclass which does tail -F on a remote file, presumably one redirected from
 * a process stream.
 * 
 * @author arossi
 * 
 */
public class CommandJobStreamTailFMonitor extends CommandJobStreamMonitor {

	private final RemoteServicesDelegate delegate;
	private final RMVariableMap rmVarMap;
	private String remoteFilePath;
	private IRemoteProcess process;

	/**
	 * Registers a remote file to monitor via tail -F.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 */
	public CommandJobStreamTailFMonitor(IJAXBResourceManagerControl rm, RMVariableMap rmVarMap, String remoteFilePath) {
		this(rm, rmVarMap, remoteFilePath, null);
	}

	/**
	 * Registers a remote file to monitor via tail -F.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 * @param encoding
	 *            stream encoding or <code>null</code> for system default
	 */
	public CommandJobStreamTailFMonitor(IJAXBResourceManagerControl rm, RMVariableMap rmVarMap, String remoteFilePath,
			String encoding) {
		this.remoteFilePath = remoteFilePath;
		this.rmVarMap = rmVarMap;
		delegate = rm.getRemoteServicesDelegate();
		bufferLimit = UNDEFINED;
	}

	/*
	 * Kills the tail -F process. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobStreamMonitor
	 * #close()
	 */
	@Override
	public synchronized void close() {
		if (process != null) {
			process.destroy();
		}
		super.close();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobStreamMonitor#initializeFilePath
	 * (java.lang.String)
	 */
	@Override
	public void initializeFilePath(String jobId) {
		if (remoteFilePath != null) {
			remoteFilePath = rmVarMap.getString(jobId, remoteFilePath);
		}
	}

	/*
	 * An actual execution of tail -F using the remote process builder. Retries
	 * for the inexistent file.
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.runnable.command.CommandJobStreamMonitor
	 * #read()
	 */
	@Override
	protected void read() {
		if (remoteFilePath == null) {
			JAXBCorePlugin.log(CoreExceptionUtils.getErrorStatus(Messages.CommandJobNullMonitorStreamError, null));
			return;
		}

		String[] args = new String[] { TAIL, MINUS_F, remoteFilePath };

		IRemoteProcessBuilder builder = delegate.getRemoteServices().getProcessBuilder(delegate.getRemoteConnection(), args);
		try {
			process = builder.start();
		} catch (IOException t) {
			JAXBCorePlugin.log(t);
			return;
		}

		/*
		 * separate thread; execute first
		 */
		readError(process.getErrorStream());
		readStream(process.getInputStream(), true);
	}

	/**
	 * Just consume the stream.
	 * 
	 * @param error
	 *            stream from tail -F
	 */
	private void readError(final InputStream error) {
		new Thread() {
			@Override
			public void run() {
				readStream(error, false);
			}
		}.start();
	}

	/**
	 * Loop until killed.
	 * 
	 * @param stream
	 * @param notify
	 *            whether to fireStreamAppended or not
	 */
	private void readStream(InputStream stream, boolean notify) {
		int read = 0;
		byte[] bytes = new byte[STREAM_BUFFER_SIZE];
		while (!fKilled) {
			try {
				read = stream.read(bytes);
				if (notify) {
					if (read > 0) {
						String text;
						if (fEncoding != null) {
							text = new String(bytes, 0, read, fEncoding);
						} else {
							text = new String(bytes, 0, read);
						}
						synchronized (this) {
							fContents.append(text);
							int len = fContents.length();
							if (bufferLimit != UNDEFINED && len > bufferLimit) {
								fContents.delete(0, len - bufferLimit);
							}
							fireStreamAppended(text);
						}
					}
				}
			} catch (EOFException eof) {
				break;
			} catch (IOException ioe) {
				if (!fKilled) {
					JAXBCorePlugin.log(ioe);
				}
				return;
			}
			if (read == 0 && !fKilled) {
				try {
					Thread.sleep(TAILF_PAUSE);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
