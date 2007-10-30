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

import java.util.Set;

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Richard Maciel
 *
 */
class RemoteItem implements IRemoteItem {

	String path;
	boolean isReadable;
	boolean isWritable;
	boolean isExecutable;
	boolean exist;
	FileTools fileTools;
	int userID;
	int permissions;
	long size;
	int accessTime;
	int modificationTime;
	protected int groupID;
	protected int changes;
	boolean isDirectory;
	
	final protected int PERMISSION = 1;
	final protected int MODIFICATION_TIME = 2;
	
	RemoteItem(FileTools fileTools, String path)	{
		this.fileTools = fileTools;
		this.path = path;
	}
	
	RemoteItem(FileTools fileTools, String path, SftpATTRS attrs) {
		this.fileTools = fileTools;
		this.path = path;
		
		parseAttrs(attrs);
	}
	
	protected RemoteItem() {
	}

	protected void copyAttributesFrom(RemoteItem other) {
		this.path = other.path;
		this.isReadable = other.isReadable;
		this.isWritable = other.isWritable;
		this.isExecutable = other.isExecutable;
		this.exist = other.exist;
		this.fileTools = other.fileTools;
		this.userID = other.userID;
		this.permissions = other.permissions;
		this.size = other.size;
		this.accessTime = other.accessTime;
		this.modificationTime = other.modificationTime;
		this.groupID = other.groupID;
		this.changes = other.changes;
		this.isDirectory = other.isDirectory;
	}
	
	public boolean exists() {
		return exist;
	}
	
	public void refreshAttributes() throws RemoteConnectionException, RemoteOperationException, CancelException, RemoteOperationException {
		fileTools.test();
		SftpATTRS attrs = fileTools.fetchRemoteAttr(path);
		
		parseAttrs(attrs);
	}

	private void parseAttrs(SftpATTRS attrs) {
		/*
		 * No file found.
		 */
		if (attrs == null) {
			exist = false;
			return;
		} 
		
		exist = true;
		changes = 0;

		isDirectory = attrs.isDir();
		
		userID = attrs.getUId();
		groupID = attrs.getGId();
		permissions = attrs.getPermissions();
		
		isReadable = false;
		isWritable = false;
		isExecutable = false;
		
		if(userID == fileTools.cachedUserID) {
			if ((permissions & 0400) != 0) {
				isReadable = true;
			}
			if ((permissions & 0200) != 0) {
				isWritable = true;
			} 
			if ((permissions & 0100) != 0) {
				isExecutable = true;
			}
		} 
				
		Set groupIDSet = fileTools.cachedGroupIDSet;
		if(groupIDSet.contains(new Integer(groupID))) {
			if ((permissions & 0040) != 0) {
				isReadable = true;
			}
			if ((permissions & 0020) != 0) {
				isWritable = true;
			} 
			if ((permissions & 0010) != 0) {
				isExecutable = true;
			}
		}
			
		if ((permissions & 0004) != 0) {
			isReadable = true;
		}
		if ((permissions & 0002) != 0) {
			isWritable = true;
		} 
		if ((permissions & 0001) != 0) {
			isExecutable = true;
		}
		
		size = attrs.getSize();
		accessTime = attrs.getATime();
		modificationTime = attrs.getMTime();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#getPathname()
	 */
	public String getPath() {
		return path;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#isReadableByUser()
	 */
	public boolean isReadable() {
		return isReadable;
	}
	
	public void setReadable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.cachedUserID) {
			if (flag) {
				permissions |= 0400;
			} else {
				permissions &= ~0400;
			}
		}
		Set groupIDSet = fileTools.cachedGroupIDSet;
		if(groupIDSet.contains(new Integer(groupID))) {
			if (flag) {
				permissions |= 0040;
			} else {
				permissions &= ~0040;
			}
		}
		if (flag) {
			permissions |= 0004;
		} else {
			permissions &= ~0004;
		}
		if (oldPermissions != permissions) {
			changes |= PERMISSION;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#isWritableByUser()
	 */
	public boolean isWritable() {
		return isWritable;
	}
	
	public void setWriteable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.cachedUserID) {
			if (flag) {
				permissions |= 0200;
			} else {
				permissions &= ~0200;
			}
		}
		Set groupIDSet = fileTools.cachedGroupIDSet;
		if(groupIDSet.contains(new Integer(groupID))) {
			if (flag) {
				permissions |= 0020;
			} else {
				permissions &= ~0020;
			}
		}
		if (flag) {
			permissions |= 0002;
		} else {
			permissions &= ~0002;
		}
		if (oldPermissions != permissions) {
			changes |= PERMISSION;
		}
	}
	
	public long getAccessTime() {
		return (long)accessTime*1000;
	}
	
	public long getModificationTime() {
		return (long)modificationTime*1000;
	}
	
	public void setModificationTime(long time) {
		int oldModificationTime = modificationTime;
		modificationTime = (int)(time/1000);
		if (oldModificationTime != modificationTime) {
			changes |= MODIFICATION_TIME;
		}
	}
	
	public void commitAttributes() throws RemoteConnectionException, CancelException, RemoteOperationException {
		fileTools.test();
		if ((changes & PERMISSION) != 0) {
			try {
				fileTools.manager.connection.sftpChannel.chmod(permissions, path);
				changes &= ~PERMISSION;
			} catch (SftpException e) {
				throw new RemoteOperationException(NLS.bind("Failed to set permission of remote file {0} ({1})", new String[] {path, e.getMessage()}), e);
			}
		}
		if ((changes & MODIFICATION_TIME) != 0) {
			try {
				fileTools.manager.connection.sftpChannel.setMtime(path, modificationTime);
				changes &= ~MODIFICATION_TIME;
			} catch (SftpException e) {
				throw new RemoteOperationException(NLS.bind("Failed to set modification fime of remote file {0} ({1})", new Object[] {path, e}), e);
			}
		}
//		changes = 0;
	}

	protected boolean isExecutable() {
		return isExecutable;
	}

	protected void setExecutable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.cachedUserID) {
			if (flag) {
				permissions |= 0100;
			} else {
				permissions &= ~0100;
			}
		}
		if(fileTools.cachedGroupIDSet.contains(new Integer(groupID))) {
			if (flag) {
				permissions |= 0010;
			} else {
				permissions &= ~0010;
			}
		}
		if (flag) {
			permissions |= 0001;
		} else {
			permissions &= ~0001;
		}
		if (oldPermissions != permissions) {
			changes |= PERMISSION;
		}
	}

	public boolean isDirectory() {
		return isDirectory;
	}
	
	public boolean isFile() {
		return ! isDirectory;
	}
	
	public String toString() {
		if (exist) {
			return path + (isDirectory ? "(d)" : "(f)");
		} else {
			return "does not exit";
		}
	}
}
