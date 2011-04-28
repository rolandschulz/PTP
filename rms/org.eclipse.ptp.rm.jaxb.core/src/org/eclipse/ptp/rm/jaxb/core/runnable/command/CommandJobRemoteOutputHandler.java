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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler;
import org.eclipse.ptp.rm.jaxb.core.IFileReadyListener;
import org.eclipse.ptp.rm.jaxb.core.IJAXBNonNLSConstants;
import org.eclipse.ptp.rm.jaxb.core.IJAXBResourceManagerControl;
import org.eclipse.ptp.rm.jaxb.core.JAXBCorePlugin;
import org.eclipse.ptp.rm.jaxb.core.messages.Messages;
import org.eclipse.ptp.rm.jaxb.core.utils.CoreExceptionUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.FileUtils;
import org.eclipse.ptp.rm.jaxb.core.utils.RemoteServicesDelegate;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;

/**
 * Replaces the tail -f monitor. Provides handling of listeners waiting for
 * notification that the remote file can be read, as well as a method for
 * obtaining its contents.
 * 
 * @author arossi
 * 
 */
public class CommandJobRemoteOutputHandler implements ICommandJobRemoteOutputHandler, IJAXBNonNLSConstants {

	private final IJAXBResourceManagerControl rm;
	private String remoteFilePath;
	private final List<IFileReadyListener> readyListeners;
	private String jobId;

	/**
	 * Registers a remote file to read.
	 * 
	 * @param rm
	 *            resource manager providing remote service
	 * @param remoteFilePath
	 *            of the file to be read
	 */
	public CommandJobRemoteOutputHandler(IJAXBResourceManagerControl rm, String remoteFilePath) {
		this.remoteFilePath = remoteFilePath;
		this.rm = rm;
		readyListeners = new ArrayList<IFileReadyListener>();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#
	 * addFileReadyListener(org.eclipse.ptp.rm.jaxb.core.IFileReadyListener)
	 */
	public void addFileReadyListener(IFileReadyListener listener) {
		if (listener != null) {
			readyListeners.add(listener);
		}
	}

	/*
	 * Checks for file existence, then waits 3 seconds to compare file length.
	 * If block is false, the listeners may be notified that the file is still
	 * not ready; else the listeners will receive a ready = true notification
	 * when the file does finally stabilize. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#checkForReady
	 * ()
	 */
	public Thread checkForReady(final boolean block) {
		Thread t = new Thread() {
			@Override
			public void run() {
				boolean ready = false;
				RemoteServicesDelegate d = rm.getRemoteServicesDelegate();
				while (!ready) {
					try {
						ready = FileUtils.isStable(d.getRemoteFileManager(), remoteFilePath, 3, new NullProgressMonitor());
					} catch (Throwable t) {
						JAXBCorePlugin.log(t);
					}

					if (!block) {
						break;
					}

					synchronized (this) {
						try {
							wait(READY_FILE_PAUSE);
						} catch (InterruptedException ignored) {
						}
					}
				}

				for (IFileReadyListener listener : readyListeners) {
					listener.handleReadyFile(jobId, remoteFilePath, ready);
				}
			}
		};
		t.start();
		return t;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#getFileContents
	 * ()
	 */
	public String getFileContents() {
		if (remoteFilePath == null) {
			JAXBCorePlugin.log(CoreExceptionUtils.getErrorStatus(Messages.CommandJobNullMonitorStreamError, null));
			return ZEROSTR;
		}

		if (rm == null) {
			JAXBCorePlugin.log(CoreExceptionUtils.getErrorStatus(Messages.CommandJobNullMonitorStreamError, null));
			return ZEROSTR;
		}
		RemoteServicesDelegate d = rm.getRemoteServicesDelegate();
		try {
			return FileUtils.read(d.getRemoteFileManager(), remoteFilePath, new NullProgressMonitor());
		} catch (Throwable t) {
			JAXBCorePlugin.log(t);
		}
		return ZEROSTR;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#getRemoteFilePath
	 * ()
	 */
	public String getRemoteFilePath() {
		return remoteFilePath;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#
	 * initializeFilePath(java.lang.String)
	 */
	public void initialize(String jobId) {
		this.jobId = jobId;
		if (remoteFilePath != null) {
			if (rm != null) {
				RMVariableMap env = rm.getEnvironment();
				if (env != null) {
					remoteFilePath = env.getString(jobId, remoteFilePath);
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.ICommandJobRemoteOutputHandler#
	 * removeFileReadyListener(org.eclipse.ptp.rm.jaxb.core.IFileReadyListener)
	 */
	public void removeFileReadyListener(IFileReadyListener listener) {
		if (listener != null) {
			readyListeners.remove(listener);
		}
	}
}
