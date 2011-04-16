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

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.IOException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Subclass which emulates tail -f on a remote file, presumably one redirected
 * from a process stream.
 * 
 * @author arossi
 * 
 */
public class CommandJobStreamTailFMonitor extends CommandJobStreamMonitor {

	private final RemoteServicesDelegate delegate;
	private String remoteFilePath;
	private BufferedInputStream fStream;
	private long lastByte;

	/**
	 * Registers a remote file to monitor via tail -f.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 */
	public CommandJobStreamTailFMonitor(IJAXBResourceManagerControl rm, String remoteFilePath) {
		this(rm, remoteFilePath, null);
	}

	/**
	 * Registers a remote file to monitor via tail -f.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be monitored
	 * @param encoding
	 *            stream encoding or <code>null</code> for system default
	 */
	public CommandJobStreamTailFMonitor(IJAXBResourceManagerControl rm, String remoteFilePath, String encoding) {
		this.remoteFilePath = remoteFilePath;
		delegate = rm.getRemoteServicesDelegate();
		bufferLimit = UNDEFINED;
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
			remoteFilePath = RMVariableMap.getActiveInstance().getString(jobId, remoteFilePath);
		}
	}

	/*
	 * An actual execution of tail -f using the remote process builder is
	 * problematic, so we repeatedly open and close a stream on the file, but
	 * keep track of the lines we've already read. (non-Javadoc)
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

		IRemoteFileManager manager = delegate.getRemoteFileManager();
		IFileStore fs = manager.getResource(remoteFilePath);
		try {
			waitForFile(fs);
		} catch (Throwable t) {
			JAXBCorePlugin.log(t);
		}
		boolean read = false;
		byte[] bytes = new byte[STREAM_BUFFER_SIZE];
		while (true) {
			try {
				read = openAndSeekLastRead(fs);
				if (read) {
					readToEnd(bytes);
				}
				closeStream();
				if (fKilled) {
					break;
				}
			} catch (NullPointerException e) {
				// killing the stream monitor while reading can cause an NPE
				// when reading from the stream
				if (!fKilled && fThread != null) {
					JAXBCorePlugin.log(e);
				}
				return;
			}
			if (!read && !fKilled) {
				try {
					Thread.sleep(TAILF_PAUSE);
				} catch (InterruptedException e) {
				}
			}
		}
	}

	/**
	 * Closes buffered stream.
	 */
	private void closeStream() {
		if (fStream != null) {
			try {
				fStream.close();
			} catch (Throwable t) {
				JAXBCorePlugin.log(t);
			}
		}
	}

	/**
	 * Opens a new stream on the remote file. Seeks the last byte read.
	 * 
	 * @param file
	 *            remote file store
	 * 
	 */
	private boolean openAndSeekLastRead(IFileStore file) {
		try {
			if (file.fetchInfo().getLength() <= lastByte) {
				return false;
			}
			fStream = new BufferedInputStream(file.openInputStream(EFS.NONE, new NullProgressMonitor()));
			try {
				for (int i = 0; i < lastByte; i++) {
					if (EOF == fStream.read()) {
						CoreExceptionUtils.newException(Messages.UndefinedByteBeforeEndOfFile + i + CM + SP + lastByte, null);
					}
				}
			} catch (EOFException eof) {
			} catch (IOException ioe) {
				if (!fKilled) {
					JAXBCorePlugin.log(ioe);
				}
			}
		} catch (CoreException t) {
			JAXBCorePlugin.log(t);
			return false;
		}
		return true;
	}

	/**
	 * Reads the stream up until EOF and notifies listeners of content.
	 * 
	 * @param bytes
	 *            buffer
	 */
	private void readToEnd(byte[] bytes) {
		int read = 0;
		while (read >= 0) {
			try {
				read = fStream.read(bytes);
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
					lastByte += read;
				}
			} catch (EOFException eof) {
				break;
			} catch (IOException ioe) {
				if (!fKilled) {
					JAXBCorePlugin.log(ioe);
				}
				return;
			}
		}
	}

	/**
	 * Returns when the file exists.
	 * 
	 * @param file
	 *            remote file store
	 */
	private void waitForFile(IFileStore file) {

		while (!file.fetchInfo().exists() && !fKilled) {
			synchronized (this) {
				try {
					wait(1000);
				} catch (InterruptedException ignored) {
				}
			}
		}
	}
}
