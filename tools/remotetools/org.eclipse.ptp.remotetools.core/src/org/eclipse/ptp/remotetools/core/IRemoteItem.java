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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

/**
 * @author richardm
 * 
 */
public interface IRemoteItem {

	/**
	 * Apply all the attributes to the item on remote host. For efficiency, only
	 * changed attributes may be written.
	 * 
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @since 3.0
	 */
	public void commitAttributes(IProgressMonitor monitor) throws RemoteConnectionException, RemoteOperationException,
			CancelException;

	/**
	 * Test if this remote item actually exists on the remote system.
	 * 
	 * @return true if the remote item exists
	 */
	public boolean exists();

	/**
	 * Get the last accessed time of the item
	 * 
	 * @return last access time
	 */
	public long getAccessTime();

	/**
	 * Get the target of a symbolic link. Only valid if {@link #isSymLink()} returns true.
	 * 
	 * @return link target
	 * @since 6.0
	 */
	public String getLinkTarget();

	/**
	 * Get the modification time of the item.
	 * 
	 * @return last modified time
	 */
	public long getModificationTime();

	/**
	 * Get the path represented by this item.
	 * 
	 * @return path
	 */
	public String getPath();

	/**
	 * Get the item size in bytes.
	 * 
	 * @return item size in bytes
	 */
	public long getSize();

	/**
	 * Tests if the remote item is a directory.
	 * 
	 * @return true if the remote item is a directory
	 */
	public boolean isDirectory();

	/**
	 * Test if the remote item is executable (accessible for a directory)
	 * 
	 * @return true if the remote imtem is executable
	 */
	public boolean isExecutable();

	/**
	 * Returns true if the remote item has a permission that allows to read this
	 * item. Several permissions are considered (user, group, others) to
	 * calculate the effective permission.
	 * 
	 * @return true/false
	 */
	public boolean isReadable();

	/**
	 * Returns true if the remote item is a symbolic link.
	 * 
	 * @return true/false
	 * @since 6.0
	 */
	public boolean isSymLink();

	/**
	 * Returns true if the remote item has a permission that allows to write
	 * this item. Several permissions are considered (user, group, others) to
	 * calculate the effective permission.
	 * 
	 * @return false
	 */
	public boolean isWritable();

	/**
	 * Fetch all the attributes from the item on remote host.
	 * 
	 * @param monitor
	 *            progress monitor used to report operation progress to the user
	 * @throws RemoteOperationException
	 * @since 3.0
	 */
	public void refreshAttributes(IProgressMonitor monitor) throws RemoteConnectionException, RemoteOperationException,
			CancelException;

	/**
	 * Set the executable status of the remote item
	 * 
	 * @param flag
	 *            true if the remote item is executable
	 */
	public void setExecutable(boolean flag);

	/**
	 * Set the modification time of the item
	 * 
	 * @param time
	 *            modification time
	 */
	public void setModificationTime(long time);

	/**
	 * Set the permission on the remote item enable/disable read access.
	 * 
	 * @param flag
	 */
	public void setReadable(boolean flag);

	/**
	 * Set the permission on the remote item to enable/disable write access.
	 * 
	 * @param flag
	 */
	public void setWriteable(boolean flag);

}
