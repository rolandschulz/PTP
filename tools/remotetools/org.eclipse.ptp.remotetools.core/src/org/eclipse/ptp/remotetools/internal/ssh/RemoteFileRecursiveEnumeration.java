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

import java.util.LinkedList;
import java.util.NoSuchElementException;
import java.util.Stack;

import org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;



public class RemoteFileRecursiveEnumeration implements IRemoteFileEnumeration {

	private Stack<String> directories;
	private RemoteFileEnumeration currentItems;
	private FileTools fileTools;
	private LinkedList<Exception> currentExceptions = new LinkedList<Exception>();
	private IRemoteItem nextRemoteItem;
	
	public RemoteFileRecursiveEnumeration(FileTools fileTools, String root) throws RemoteOperationException, RemoteConnectionException, CancelException {
		this.fileTools = fileTools;
		// dont need, getItem(root) will test
		// fileTools.test();
		directories = new Stack<String>();
		directories.add(root);
		prefetchNextRemoteItem();
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return nextRemoteItem != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration#hasMoreExceptions()
	 */
	public boolean hasMoreExceptions() {
		return currentExceptions.size() > 0;
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
		if (nextRemoteItem == null) {
			throw new NoSuchElementException();
		}
		IRemoteItem returnItem = nextRemoteItem;
		prefetchNextRemoteItem();
		return returnItem;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFileEnumeration#nextException()
	 */
	public Exception nextException() {
		if (currentExceptions.size() == 0) {
			return null;
		}
		return (Exception) currentExceptions.removeFirst();
	}

	private void prefetchNextRemoteItem() {
		if (directories == null) {
			// Enumeration stopped due to error.
			nextRemoteItem = null;
			return;
		}
		
		nextRemoteItem = null;
		while (currentItems == null || ! currentItems.hasMoreElements()) {
			if (directories.empty()) {
				// No more directories are in stack. Terminate without any item.
				return;
			}
			String root = (String)directories.pop();
			try {
				currentItems = new RemoteFileEnumeration(fileTools, root);
			} catch (CancelException e) {
				currentExceptions.clear();
				currentExceptions.addLast(e);
				return;
			} catch (RemoteConnectionException e) {
				currentExceptions.clear();
				currentExceptions.addLast(e);
				return;
			} catch (RemoteOperationException e) {
				currentExceptions.addLast(e);
				continue;
			}
		}
			
		IRemoteItem item = currentItems.nextElementAsItem();
		if (item.isDirectory()) {
			directories.add(item.getPath());
		}
		nextRemoteItem = item;
	}
	
	
}
