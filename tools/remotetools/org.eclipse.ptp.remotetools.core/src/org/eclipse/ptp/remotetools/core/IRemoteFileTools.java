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

import java.util.Enumeration;

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
	 * Return an IRemoteCopyTools object
	 * @throws RemoteConnectionException
	 * @return IRemoteCopyTools
	 */
	public IRemoteCopyTools getRemoteCopyTools() throws RemoteConnectionException;
	
	/**
	 * Move a file on remote host.
	 * <p>
	 * Create (sub)directories as necessary for the new path.
	 * 
	 * @param from
	 *            Absolute path to source on remote host.
	 * @param to
	 *            Absolute path to destination on remote host.
	 * @throws RemoteExecutionException
	 *             The file could not be moved.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The move operation was canceled by another thread. If the
	 *             move involves several files, then the move may remain
	 *             incomplete.
	 */
	public void moveFile(String from, String to)
			throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * Copy a file on remote host.
	 * <p>
	 * Create (sub)directories as necessary for the new path.
	 * 
	 * @param from
	 *            Absolute path to source on remote host.
	 * @param to
	 *            Absolute path to destination on remote host.
	 * @throws RemoteExecutionException
	 *             The file could not be copied.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The copy operation was canceled by another thread. If the
	 *             copy involves several files, then the copy may remain
	 *             incomplete.
	 */
	public void copyFile(String from, String to) throws RemoteOperationException,
		RemoteConnectionException, CancelException;
	
	/**
	 * Delete a file or directory on remote host.
	 * <p>
	 * If the path is a directory, remove recursively.
	 * 
	 * @param file
	 *            Absolute path of file or directory to be deleted.
	 * @throws RemoteExecutionException
	 *             The file could not be deleted.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The remove operation was canceled by another thread. The
	 *             file(s) may have been all removed, partially not removed or
	 *             not removed at all.
	 */
	public void removeFile(String file) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	
	/**
	 * Create a new directory.
	 * <p>
	 * If directory exists, nothings is done.
	 * Create parent directories as necessary.
	 * 
	 * @param file
	 *            Directory to be created.
	 * @throws RemoteExecutionException
	 *             The directory could not be created.
	 * @throws RemoteConnectionException
	 *             The connection failed.
	 * @throws CancelException
	 *             The operation was canceled by another thread.
	 */
	public void createDirectory(String file) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	/**
	 * Assure that a directory exists. 
	 * <p>
	 * Works like {@link #createDirectory(String)}, but may be more efficient.
	 * 
	 * @param directory
	 *            Directory that must be guaranteed to exist
	 * @throws RemoteExecutionException
	 *             The directory could not be created.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public void assureDirectory(String directory)
			throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	/**
	 * Query if the path exists and is a directory on the remote host.
	 * 
	 * @param directory
	 * @return
	 * @throws RemoteExecutionException
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public boolean hasDirectory(String directory)
			throws RemoteOperationException, RemoteConnectionException,
			CancelException;

	public boolean hasFile(String directory) throws RemoteOperationException,
			RemoteConnectionException, CancelException;

	public boolean canWrite(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException;

	public boolean canRead(String remotePath) throws RemoteOperationException, RemoteConnectionException, CancelException;

	public boolean canExecute(String remotePath)
			throws RemoteOperationException, RemoteConnectionException, CancelException;

	/**
	 * List all items from a given path on the remote host. 
	 * If the item is a directory, it will
	 * list all its files and directories. If the item is a file, only list 
	 * itself.
	 * The result is an array of objects that describe the propreties of
	 * the items. The properties differ according to the nature of the item.
	 * 
	 * @param path
	 *            string that represents the remote path
	 * @return An array of items of the remote path. If the item is a directory
	 * and it is empty, then the array is empty.
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public IRemoteItem[] listItems(String remotePath)
			throws RemoteConnectionException, RemoteOperationException,
			CancelException;

	/**
	 * Query properties of the given item on the remote host.
	 * 
	 * @param path
	 *            string to a file
	 * @return A {@link IRemoteFile} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 */
	public IRemoteItem getItem(String path) throws RemoteConnectionException,
	RemoteOperationException, CancelException;

	/**
	 * Convenience method to query properties of the given file on the remote
	 * host.
	 * 
	 * @param filePath
	 *            Absolute path to a file
	 * @return A {@link IRemoteFile} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @throws RemoteOperationException 
	 */
	public IRemoteFile getFile(String filePath)
			throws RemoteConnectionException,
			CancelException, RemoteOperationException;

	/**
	 * Convenience method to query properties of the given directory on the
	 * remote host.
	 * 
	 * @param directoryPath
	 *            Absolute path to a directory
	 * @return A {@link IRemoteDirectory} object
	 * @throws RemoteExecutionException
	 *             The path does not exist on the remote host or is not a file.
	 * @throws RemoteConnectionException
	 * @throws CancelException
	 * @throws RemoteOperationException 
	 */
	public IRemoteDirectory getDirectory(String directoryPath)
			throws RemoteConnectionException,
			CancelException, RemoteOperationException;
	
	public IRemoteFileEnumeration createFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException;
	public IRemoteFileEnumeration createRecursiveFileEnumeration(String path) throws RemoteOperationException, RemoteConnectionException, CancelException;

}