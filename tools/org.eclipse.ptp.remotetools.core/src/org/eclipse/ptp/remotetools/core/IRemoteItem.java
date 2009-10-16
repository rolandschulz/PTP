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

import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

/**
 * @author richardm
 *
 */
public interface IRemoteItem {

	/**
	 * Get the path represented by this item.
	 * 
	 * @return path
	 */
	public String getPath();
	
	/**
	 * Test if this remote item actually exists on the remote system.
	 * 
	 * @return true if the remote item exists
	 */
	public boolean exists();
	
	/**
	 * Returns true if the remote item has a permission that allows to read this
	 * item. Several permissions are considered (user, group, others) to
	 * calculate the effective permission.
	 * 
	 * @return true/false
	 */
	public boolean isReadable();
	
	/**
	 * Set the permission on the remote item enable/disable read access.
	 * 
	 * @param flag
	 */
	public void setReadable(boolean flag);
	
	/**
	 * Returns true if the remote item has a permission that allows to write
	 * this item. Several permissions are considered (user, group, others) to
	 * calculate the effective permission.
	 * 
	 * @return false
	 */
	public boolean isWritable();
	
	/**
	 * Set the permission on the remote item to enable/disable write access.
	 * 
	 * @param flag
	 */
	public void setWriteable(boolean flag);
	
//	public long getCreationTime();
//	public void setCreationTime(long time);

	/**
	 * Get the modification time of the item.
	 * 
	 * @return last modified time
	 */
	public long getModificationTime();
	
	/**
	 * Set the modification time of the item
	 * 
	 * @param time modification time
	 */
	public void setModificationTime(long time);

	/**
	 * Get the last accessed time of the item
	 * 
	 * @return last access time
	 */
	public long getAccessTime();
//	public void setAccessTime(long time);

//	public String getUserId();
//	public String setUserId();
//	
//	public String getGroupId();
//	public String setGroupId();
	
	/**
	 * Tests if the remote item is a directory.
	 * 
	 * @return true if the remote item is a directory
	 */
	public boolean isDirectory();
	
	/**
	 * Fetch all the attributes from the item on remote host.
	 * @throws RemoteOperationException 
	 */
	public void refreshAttributes() throws RemoteConnectionException, RemoteOperationException, CancelException;
	
	/**
	 * Apply all the attributes to the item on remote host.
	 * For efficiency, only changed attributes may be written.
	 */
	public void commitAttributes() throws RemoteConnectionException, RemoteOperationException, CancelException;
	
}
