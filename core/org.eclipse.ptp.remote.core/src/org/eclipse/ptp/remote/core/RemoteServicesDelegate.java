/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.remote.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.remote.core.messages.Messages;

/**
 * A wrapper for holding initialized remote services information. <br>
 * 
 * Also contains convenience utilities associated with remote file operations.
 * 
 * @see org.eclipse.ptp.remote.core.IRemoteFileManager
 * @see org.eclipse.core.filesystem.IFileStore
 * 
 * @author arossi
 * @since 5.0
 * 
 */
public class RemoteServicesDelegate {
	private static final String COSP = ": ";//$NON-NLS-1$ 
	private static final int UNDEFINED = -1;
	private static final int COPY_BUFFER_SIZE = 64 * 1024;

	private final IRemoteServices remoteServices;
	private final IRemoteServices localServices;
	private final IRemoteConnectionManager remoteConnectionManager;
	private final IRemoteConnectionManager localConnectionManager;
	private final IRemoteConnection remoteConnection;
	private final IRemoteConnection localConnection;
	private final IRemoteFileManager remoteFileManager;
	private final IRemoteFileManager localFileManager;
	private final URI localHome;
	private final URI remoteHome;

	/**
	 * On the basis of the passed in identifiers, constructs the local and
	 * remote services, connection manager, connection, file manager and home
	 * URIs.
	 * 
	 * @param remoteServicesId
	 *            e.g., "local", "remotetools", "rse"
	 * @param remoteConnectionName
	 *            e.g., "ember.ncsa.illinois.edu"
	 */
	public RemoteServicesDelegate(String remoteServicesId, String remoteConnectionName) {
		localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		localConnectionManager = localServices.getConnectionManager();
		assert (localConnectionManager != null);
		localConnection = localConnectionManager.getConnection("Local");//$NON-NLS-1$
		assert (localConnection != null);
		localFileManager = localServices.getFileManager(localConnection);
		assert (localFileManager != null);

		if (remoteServicesId != null) {
			remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remoteServicesId, new NullProgressMonitor());
			assert (null != remoteServices);
			remoteConnectionManager = remoteServices.getConnectionManager();
			assert (null != remoteConnectionManager);
			remoteConnection = remoteConnectionManager.getConnection(remoteConnectionName);
			assert (null != remoteConnection);
			remoteFileManager = remoteServices.getFileManager(remoteConnection);
			assert (null != remoteFileManager);
		} else {
			remoteServices = localServices;
			remoteConnectionManager = localConnectionManager;
			remoteConnection = localConnection;
			remoteFileManager = localFileManager;
		}

		localHome = localFileManager.toURI(localConnection.getWorkingDirectory());
		remoteHome = remoteFileManager.toURI(remoteConnection.getWorkingDirectory());
	}

	public IRemoteConnection getLocalConnection() {
		return localConnection;
	}

	public IRemoteConnectionManager getLocalConnectionManager() {
		return localConnectionManager;
	}

	public IRemoteFileManager getLocalFileManager() {
		return localFileManager;
	}

	public URI getLocalHome() {
		return localHome;
	}

	public IRemoteServices getLocalServices() {
		return localServices;
	}

	public IRemoteConnection getRemoteConnection() {
		return remoteConnection;
	}

	public IRemoteConnectionManager getRemoteConnectionManager() {
		return remoteConnectionManager;
	}

	public IRemoteFileManager getRemoteFileManager() {
		return remoteFileManager;
	}

	public URI getRemoteHome() {
		return remoteHome;
	}

	public IRemoteServices getRemoteServices() {
		return remoteServices;
	}

	/**
	 * @param from
	 *            manager for source resource
	 * @param source
	 *            file to copy
	 * @param to
	 *            manager for target resource
	 * @param target
	 *            destination file
	 * @param progress
	 * @throws CoreException
	 */
	public static void copy(IRemoteFileManager from, String source, IRemoteFileManager to, String target, IProgressMonitor progress)
			throws CoreException {
		if (from == null) {
			throw CoreExceptionUtils.newException(Messages.Copy_Operation_NullSourceFileManager, null);
		}
		if (to == null) {
			throw CoreExceptionUtils.newException(Messages.Copy_Operation_NullTargetFileManager, null);
		}
		if (source == null) {
			throw CoreExceptionUtils.newException(Messages.Copy_Operation_NullSource, null);
		}
		if (target == null) {
			throw CoreExceptionUtils.newException(Messages.Copy_Operation_NullTarget, null);
		}

		IFileStore lres = from.getResource(source);
		if (!lres.fetchInfo(EFS.NONE, new SubProgressMonitor(progress, 5)).exists()) {
			throw CoreExceptionUtils.newException(Messages.Copy_Operation_Local_resource_does_not_exist + COSP + lres.getName(),
					null);
		}
		IFileStore rres = to.getResource(target);
		lres.copy(rres, EFS.OVERWRITE, new SubProgressMonitor(progress, 5));
	}

	/**
	 * Checks for existence of file. If it does exist, tests to see if it is
	 * stable by checking size after the given timeout.
	 * 
	 * @param manager
	 *            for resource where file is located
	 * @param path
	 *            of file
	 * @param intervalInSecs
	 *            time after which to check size of file again
	 * @param progress
	 * @return true if file exists and is not being written to over the given
	 *         interval.
	 */
	public static boolean isStable(IRemoteFileManager manager, String path, int intervalInSecs, IProgressMonitor progress)
			throws CoreException {
		if (manager == null) {
			throw CoreExceptionUtils.newException(Messages.Read_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw CoreExceptionUtils.newException(Messages.Read_Operation_NullPath, null);
		}

		IFileStore lres = manager.getResource(path);
		IFileInfo info = lres.fetchInfo(EFS.NONE, new SubProgressMonitor(progress, 5));
		if (!info.exists()) {
			return false;
		}
		long l0 = info.getLength();
		try {
			Thread.sleep(1000 * intervalInSecs);
		} catch (InterruptedException ignored) {
		}
		info = lres.fetchInfo(EFS.NONE, new SubProgressMonitor(progress, 5));
		long l1 = info.getLength();
		return l0 == l1;
	}

	/**
	 * @param manager
	 *            for resource where file is located
	 * @param path
	 *            of file
	 * @param progress
	 * @return contents of file
	 * @throws CoreException
	 */
	public static String read(IRemoteFileManager manager, String path, IProgressMonitor progress) throws CoreException {
		if (manager == null) {
			throw CoreExceptionUtils.newException(Messages.Read_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw CoreExceptionUtils.newException(Messages.Read_Operation_NullPath, null);
		}

		IFileStore lres = manager.getResource(path);
		if (!lres.fetchInfo(EFS.NONE, new SubProgressMonitor(progress, 5)).exists()) {
			throw CoreExceptionUtils.newException(Messages.Read_Operation_resource_does_not_exist + COSP + lres.getName(), null);
		}
		BufferedInputStream is = new BufferedInputStream(lres.openInputStream(EFS.NONE, progress));
		StringBuffer sb = new StringBuffer();
		byte[] buffer = new byte[COPY_BUFFER_SIZE];
		int rcvd = 0;

		try {
			while (true) {
				try {
					rcvd = is.read(buffer, 0, COPY_BUFFER_SIZE);
				} catch (EOFException eof) {
					break;
				}

				if (rcvd == UNDEFINED) {
					break;
				}
				sb.append(new String(buffer, 0, rcvd));
			}
		} catch (IOException ioe) {
			throw CoreExceptionUtils.newException(Messages.Read_OperationFailed + path, null);
		} finally {
			try {
				is.close();
			} catch (IOException ioe) {
				PTPRemoteCorePlugin.log(ioe);
			}
		}
		return sb.toString();
	}

	/**
	 * @param manager
	 *            for resource where file is to be written
	 * @param path
	 *            of file to write
	 * @param contents
	 *            to write to file
	 * @param progress
	 * @throws CoreException
	 */
	public static void write(IRemoteFileManager manager, String path, String contents, IProgressMonitor progress)
			throws CoreException {
		if (contents == null) {
			return;
		}
		if (manager == null) {
			throw CoreExceptionUtils.newException(Messages.Write_Operation_NullSourceFileManager, null);
		}
		if (path == null) {
			throw CoreExceptionUtils.newException(Messages.Write_Operation_NullPath, null);
		}

		IFileStore lres = manager.getResource(path);
		BufferedOutputStream os = new BufferedOutputStream(lres.openOutputStream(EFS.NONE, progress));
		try {
			os.write(contents.getBytes());
			os.flush();
		} catch (IOException ioe) {
			throw CoreExceptionUtils.newException(Messages.Write_OperationFailed + path, ioe);
		} finally {
			try {
				os.close();
			} catch (IOException ioe) {
				PTPRemoteCorePlugin.log(ioe);
			}
		}
	}
}
