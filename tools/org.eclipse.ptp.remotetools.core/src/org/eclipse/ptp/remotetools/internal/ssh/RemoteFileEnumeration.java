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
package org.eclipse.ptp.remotetools.internal.ssh;

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

	private IRemoteItem[] items = null;
	private int currentItem = 0;
	
	/**
	 * Enumerates all files on a given root directory.
	 * 
	 * @param fileTools
	 * @param directoryPath The remote directory. Must be a valid existing directory.
	 * @throws RemoteOperationException
	 * @throws RemoteConnectionException The root directory does not exist or is not a directory.
	 * @throws CancelException
	 */
	public RemoteFileEnumeration(FileTools fileTools, String directoryPath) throws RemoteOperationException, RemoteConnectionException, CancelException {
		// don't need, listItems(root) will test
		// fileTools.test();

		this.items = fileTools.listItems(directoryPath, null);
		this.currentItem = 0;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return currentItem < items.length;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration#hasMoreExceptions()
	 */
	public boolean hasMoreExceptions() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public IRemoteItem nextElement() {
		return nextElementAsItem();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration#nextElementAsItem()
	 */
	public IRemoteItem nextElementAsItem() {
		if (currentItem >= items.length) {
			throw new NoSuchElementException();
		}
		return items[currentItem++];
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration#nextException()
	 */
	public Exception nextException() {
		return null; // always null
	}
}

