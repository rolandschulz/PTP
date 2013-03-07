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

import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.SftpATTRS;

/**
 * @author Richard Maciel
 * 
 */
class RemoteItem implements IRemoteItem {

	protected String path;
	protected String linkTarget;
	protected boolean isReadable;
	protected boolean isWritable;
	protected boolean isExecutable;
	protected boolean isSymLink;
	protected boolean exist;
	protected FileTools fileTools;
	protected int userID;
	protected int permissions;
	protected long size;
	protected int accessTime;
	protected int modificationTime;
	protected int groupID;
	protected int changes;
	protected boolean isDirectory;

	protected final int PERMISSION = 1;
	protected final int MODIFICATION_TIME = 2;

	protected RemoteItem() {
	}

	RemoteItem(FileTools fileTools, String path) {
		this.fileTools = fileTools;
		this.path = path;
	}

	RemoteItem(FileTools fileTools, String path, SftpATTRS attrs) {
		this.fileTools = fileTools;
		this.path = path;

		RemoteFileAttributes remoteAttrs = new RemoteFileAttributes(attrs);
		parseAttrs(remoteAttrs);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#commitAttributes()
	 */
	public void commitAttributes(IProgressMonitor monitor) throws RemoteConnectionException, CancelException,
			RemoteOperationException {
		fileTools.test();
		if ((changes & PERMISSION) != 0) {
			fileTools.chmod(permissions, path, monitor);
			changes &= ~PERMISSION;
		}
		if ((changes & MODIFICATION_TIME) != 0) {
			fileTools.setMtime(path, modificationTime, monitor);
			changes &= ~MODIFICATION_TIME;
		}
		// changes = 0;
	}

	protected void copyAttributesFrom(RemoteItem other) {
		this.path = other.path;
		this.isReadable = other.isReadable;
		this.isWritable = other.isWritable;
		this.isExecutable = other.isExecutable;
		this.isSymLink = other.isSymLink;
		this.linkTarget = other.linkTarget;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#exists()
	 */
	public boolean exists() {
		return exist;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#getAccessTime()
	 */
	public long getAccessTime() {
		return (long) accessTime * 1000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#getLinkTarget()
	 */
	public String getLinkTarget() {
		return linkTarget;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#getModificationTime()
	 */
	public long getModificationTime() {
		return (long) modificationTime * 1000;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#getPathname()
	 */
	public String getPath() {
		return path;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#getSize()
	 */
	public long getSize() {
		return size;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#isDirectory()
	 */
	public boolean isDirectory() {
		return isDirectory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#isExecutable()
	 */
	public boolean isExecutable() {
		return isExecutable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#isReadableByUser()
	 */
	public boolean isReadable() {
		return isReadable;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#isSymLink()
	 */
	public boolean isSymLink() {
		return isSymLink;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItemProperties#isWritableByUser()
	 */
	public boolean isWritable() {
		return isWritable;
	}

	private void parseAttrs(RemoteFileAttributes attrs) {
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
		isSymLink = attrs.isLink();
		if (isSymLink) {
			linkTarget = attrs.getLinkTarget();
		}

		isReadable = false;
		isWritable = false;
		isExecutable = false;

		if (userID == fileTools.getCachedUserID()) {
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

		Set<Integer> groupIDSet = fileTools.getCachedGroupIDSet();
		if (groupIDSet.contains(new Integer(groupID))) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#refreshAttributes()
	 */
	public void refreshAttributes(IProgressMonitor monitor) throws RemoteConnectionException, RemoteOperationException,
			CancelException {
		fileTools.test();
		RemoteFileAttributes attrs = fileTools.fetchRemoteAttr(path, monitor);
		parseAttrs(attrs);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#setExecutable(boolean)
	 */
	public void setExecutable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.getCachedUserID()) {
			if (flag) {
				permissions |= 0100;
			} else {
				permissions &= ~0100;
			}
		}
		if (fileTools.getCachedGroupIDSet().contains(new Integer(groupID))) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#setModificationTime(long)
	 */
	public void setModificationTime(long time) {
		int oldModificationTime = modificationTime;
		modificationTime = (int) (time / 1000);
		if (oldModificationTime != modificationTime) {
			changes |= MODIFICATION_TIME;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#setReadable(boolean)
	 */
	public void setReadable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.getCachedUserID()) {
			if (flag) {
				permissions |= 0400;
			} else {
				permissions &= ~0400;
			}
		}
		Set<Integer> groupIDSet = fileTools.getCachedGroupIDSet();
		if (groupIDSet.contains(new Integer(groupID))) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteItem#setWriteable(boolean)
	 */
	public void setWriteable(boolean flag) {
		int oldPermissions = permissions;
		if (userID == fileTools.getCachedUserID()) {
			if (flag) {
				permissions |= 0200;
			} else {
				permissions &= ~0200;
			}
		}
		Set<Integer> groupIDSet = fileTools.getCachedGroupIDSet();
		if (groupIDSet.contains(new Integer(groupID))) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		if (exist) {
			return path + (isDirectory ? "(d)" : "(f)"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return path + " does not exist"; //$NON-NLS-1$
		}
	}
}
