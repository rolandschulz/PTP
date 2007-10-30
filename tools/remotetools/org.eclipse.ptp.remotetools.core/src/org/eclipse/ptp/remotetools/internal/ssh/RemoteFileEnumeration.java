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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;


/**
 * Enumeration of files on the remote host.
 * The enumeration stops when:
 * <ul>
 * <li>All files have been enumerated.
 * <li>The execution manager gets canceled.
 * <li>The connection fails
 * </ul>
 * If an element fails to be retrieved, an exception is returned instead of the element.
 * 
 * Instead of {@link #nextElement()}, one may call {@link #nextRemoteItem()}.
 * 
 * @author Daniel Ferber
 */
public class RemoteFileEnumeration implements IRemoteFileEnumeration {

	IRemoteItem[] items = null;
	int currentItem = 0;
	FileTools fileTools = null;
	
	/**
	 * Enumerates all files on a given root directory.
	 * 
	 * @param fileTools
	 * @param directoryPath The remote directory. Must be a valid existing directory.
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException The root directory does not exist or is not a directory.
	 * @throws CancelException
	 */
	RemoteFileEnumeration(FileTools fileTools, String directoryPath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		// dont need, listItems(root) will test
		// fileTools.test();

		this.fileTools = fileTools;
		this.items = fileTools.listItems(directoryPath);
		this.currentItem = 0;
	}
	
	public boolean hasMoreElements() {
		return currentItem < items.length;
	}

	public Object nextElement() {
		return nextElementAsItem();
	}
	
	public IRemoteItem nextElementAsItem() {
		if (currentItem >= items.length) {
			throw new NoSuchElementException();
		}
		return items[currentItem++];
	}

	public Exception nextException() {
		return null; // always nulls
	}

	public boolean hasMoreExceptions() {
		return false;
	}
}

