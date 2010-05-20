/******************************************************************************
 * Copyright (c) 2006, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *     Roland Schulz, University of Tennessee
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.core;

import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

/**
 * Groups all operations about files on the remote host.
 * 
 * @author Daniel Ferber, Richard Maciel
 */
public interface IRemoteFileTools {
	/**
	 * Equivalent to specifying no options
	 */
	public static final int NONE = 0;

	/**
	 * Append output to the end of a file
	 */
	public static final int APPEND = 1 << 0;

	/**
	 * Assure that a directory exists.
	 * <p>
	 * Works like {@link #createDirectory(String)}, but may be more efficient.
	 * 
	 * @param directory
	 *            Directory that must be guaranteed to exist
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The directory could not be created.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @since 3.0
	 */
	public void assureDirectory(String directory, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * @param remotePath
	 * @return
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public boolean canExecute(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException;

	/**
	 * @param remotePath
	 * @return
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public boolean canRead(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException;

	/**
	 * @param remotePath
	 * @return
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public boolean canWrite(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException;

	/**
	 * Copy a file on remote host.
	 * <p>
	 * Create (sub)directories as necessary for the new path.
	 * 
	 * @param from
	 *            Absolute path to source on remote host.
	 * @param to
	 *            Absolute path to destination on remote host.
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The file could not be copied.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The copy operation was canceled by another thread. If the
	 *             copy involves several files, then the copy may remain
	 *             incomplete.
	 * @since 3.0
	 */
	public void copyFile(String from, String to, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Create a new directory.
	 * <p>
	 * If directory exists, nothings is done. Parent directories will be created
	 * as necessary.
	 * 
	 * @param directory
	 *            Directory to be created.
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The directory could not be created.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The operation was canceled by another thread.
	 * @since 3.0
	 */
	public void createDirectory(String directory, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Create a new empty file.
	 * <p>
	 * If file exists, nothings is done.
	 * 
	 * @param file
	 *            File to be created.
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The file could not be created.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The operation was canceled by another thread.
	 * @since 3.0
	 */
	public void createFile(String file, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * @param path
	 * @return
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public IRemoteFileEnumeration createFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * @param path
	 * @return
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public IRemoteFileEnumeration createRecursiveFileEnumeration(String path) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Convenience method to query properties of the given directory on the
	 * remote host.
	 * 
	 * @param directoryPath
	 *            Absolute path to a directory
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @return A {@link IRemoteItem} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @throws RemoteOperationException
	 * @since 3.0
	 */
	public IRemoteItem getDirectory(String directoryPath, IProgressMonitor monitor) throws RemoteConnectionException,
			CancelException, RemoteOperationException;

	/**
	 * Convenience method to query properties of the given file on the remote
	 * host.
	 * 
	 * @param filePath
	 *            Absolute path to a file
	 * @param monitor
	 *            Progress monitor used to report operation progress to the user
	 * @return A {@link IRemoteItem} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @throws RemoteOperationException
	 * @since 3.0
	 */
	public IRemoteItem getFile(String filePath, IProgressMonitor monitor) throws RemoteConnectionException, CancelException,
			RemoteOperationException;

	/**
	 * Get an input stream connected to a file.
	 * 
	 * @param file
	 *            file name of the file
	 * @param monitor
	 *            progress monitor
	 * @return InputStream that can be used to read from the file
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public InputStream getInputStream(String file, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Create a representation of the remote object.
	 * 
	 * @param path
	 *            string to a remote object
	 * @return A {@link IRemoteFile} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public IRemoteItem getItem(String path) throws RemoteConnectionException, RemoteOperationException, CancelException;

	/**
	 * Get an output stream connected to a file.
	 * 
	 * @param file
	 *            file name of the file
	 * @param options
	 *            options to modify the behavior of the method. Legal values are
	 *            NONE and APPEND.
	 * @param monitor
	 *            progress monitor
	 * @return OutputStream that can be used to write to the file
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public OutputStream getOutputStream(String file, int options, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Return an IRemoteCopyTools object
	 * 
	 * @throws RemoteConnectionException
	 * @return IRemoteCopyTools
	 */
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException;

	/**
	 * Query if the path exists and is a directory on the remote host.
	 * 
	 * @param path
	 *            path of the remote directory
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @return true if the remote path is a directory
	 * @throws RemoteExecutionException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @since 3.0
	 */
	public boolean hasDirectory(String path, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * Query if the path exists and is a file on the remote host.
	 * 
	 * @param path
	 *            path of the remote file
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @return true if the remote path is a file
	 * @throws RemoteExecutionException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @since 3.0
	 */
	public boolean hasFile(String path, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * List all items from a given path on the remote host. If the item is a
	 * directory, it will list all its files and directories. If the item is a
	 * file, only list itself. The result is an array of objects that describe
	 * the properties of the items. The properties differ according to the
	 * nature of the item.
	 * 
	 * @param path
	 *            string that represents the remote path
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @return An array of items of the remote path. If the item is a directory
	 *         and it is empty, then the array is empty.
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @since 3.0
	 */
	public IRemoteItem[] listItems(String remotePath, IProgressMonitor monitor) throws RemoteConnectionException,
			RemoteOperationException, CancelException;

	/**
	 * Move a file on remote host.
	 * <p>
	 * Create (sub)directories as necessary for the new path.
	 * 
	 * @param from
	 *            Absolute path to source on remote host.
	 * @param to
	 *            Absolute path to destination on remote host.
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The file could not be moved.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The move operation was canceled by another thread. If the
	 *             move involves several files, then the move may remain
	 *             incomplete.
	 * @since 3.0
	 */
	public void moveFile(String from, String to, IProgressMonitor monitor) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Delete a directory on remote host.
	 * <p>
	 * The directory and all it's sub-directories will be removed
	 * 
	 * @param file
	 *            Absolute path of the directory to be deleted.
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The directory could not be deleted.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The remove operation was canceled by another thread. The
	 *             file(s) may have been all removed, partially not removed or
	 *             not removed at all.
	 * @since 3.0
	 */
	public void removeDirectory(String dir, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * Delete a file on remote host.
	 * 
	 * @param file
	 *            Absolute path of thefile to be deleted.
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @throws RemoteExecutionException
	 *             The file could not be deleted.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The remove operation was canceled by another thread. The
	 *             file(s) may have been all removed, partially not removed or
	 *             not removed at all.
	 * @since 3.0
	 */
	public void removeFile(String file, IProgressMonitor monitor) throws RemoteOperationException, RemoteConnectionException,
			CancelException;
}