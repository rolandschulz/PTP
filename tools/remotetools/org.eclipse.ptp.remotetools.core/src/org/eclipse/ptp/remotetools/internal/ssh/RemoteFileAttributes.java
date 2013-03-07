/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.remotetools.internal.ssh;

import java.util.Date;

import com.ibm.icu.text.SimpleDateFormat;
import com.jcraft.jsch.Buffer;
import com.jcraft.jsch.SftpATTRS;

public class RemoteFileAttributes {
	static final int S_ISUID = 04000; // set user ID on execution
	static final int S_ISGID = 02000; // set group ID on execution
	static final int S_ISVTX = 01000; // sticky bit ****** NOT DOCUMENTED *****

	static final int S_IRUSR = 00400; // read by owner
	static final int S_IWUSR = 00200; // write by owner
	static final int S_IXUSR = 00100; // execute/search by owner
	static final int S_IREAD = 00400; // read by owner
	static final int S_IWRITE = 00200; // write by owner
	static final int S_IEXEC = 00100; // execute/search by owner

	static final int S_IRGRP = 00040; // read by group
	static final int S_IWGRP = 00020; // write by group
	static final int S_IXGRP = 00010; // execute/search by group

	static final int S_IROTH = 00004; // read by others
	static final int S_IWOTH = 00002; // write by others
	static final int S_IXOTH = 00001; // execute/search by others

	private static final int pmask = 0xFFF;

	public static final int SSH_FILEXFER_ATTR_SIZE = 0x00000001;
	public static final int SSH_FILEXFER_ATTR_UIDGID = 0x00000002;
	public static final int SSH_FILEXFER_ATTR_PERMISSIONS = 0x00000004;
	public static final int SSH_FILEXFER_ATTR_ACMODTIME = 0x00000008;
	public static final int SSH_FILEXFER_ATTR_EXTENDED = 0x80000000;
	
	static final int S_IFDIR = 0x4000;
	static final int S_IFLNK = 0xa000;

	public static RemoteFileAttributes getAttributes(SftpATTRS attrs) {
		return new RemoteFileAttributes(attrs);
	}

	public static RemoteFileAttributes getAttributes(String buf) {
		RemoteFileAttributes attr = new RemoteFileAttributes();

		if (buf.endsWith("No such file or directory")) { //$NON-NLS-1$
			return null;
		}
		String[] args = buf.split(" "); //$NON-NLS-1$
		if (args.length != 6) {
			return null;
		}

		attr.flags = SSH_FILEXFER_ATTR_PERMISSIONS | SSH_FILEXFER_ATTR_SIZE | SSH_FILEXFER_ATTR_UIDGID
				| SSH_FILEXFER_ATTR_ACMODTIME;

		attr.permissions = Integer.decode(args[0]);
		attr.size = Long.parseLong(args[1]);
		attr.uid = Integer.parseInt(args[2]);
		attr.gid = Integer.parseInt(args[3]);
		attr.mtime = Integer.parseInt(args[4]);
		attr.atime = Integer.parseInt(args[5]);
		return attr;
	}

	int flags = 0;
	long size;
	int uid;
	int gid;
	int permissions;
	int atime;
	int mtime;
	String linkTarget;
	String[] extended = null;

	public RemoteFileAttributes() {
	}

	public RemoteFileAttributes(SftpATTRS attrs) {
		flags = attrs.getFlags();
		permissions = attrs.getPermissions();
		size = attrs.getSize();
		uid = attrs.getUId();
		gid = attrs.getGId();
		mtime = attrs.getMTime();
		atime = attrs.getATime();
		extended = attrs.getExtended();
	}

	void dump(Buffer buf) {
		buf.putInt(flags);
		if ((flags & SSH_FILEXFER_ATTR_SIZE) != 0) {
			buf.putLong(size);
		}
		if ((flags & SSH_FILEXFER_ATTR_UIDGID) != 0) {
			buf.putInt(uid);
			buf.putInt(gid);
		}
		if ((flags & SSH_FILEXFER_ATTR_PERMISSIONS) != 0) {
			buf.putInt(permissions);
		}
		if ((flags & SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
			buf.putInt(atime);
		}
		if ((flags & SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
			buf.putInt(mtime);
		}
		if ((flags & SSH_FILEXFER_ATTR_EXTENDED) != 0) {
			int count = extended.length / 2;
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					buf.putString(extended[i * 2].getBytes());
					buf.putString(extended[i * 2 + 1].getBytes());
				}
			}
		}
	}

	public int getATime() {
		return atime;
	}

	public String getAtimeString() {
		SimpleDateFormat locale = new SimpleDateFormat();
		return (locale.format(new Date(atime)));
	}

	public String[] getExtended() {
		return extended;
	}

	public int getFlags() {
		return flags;
	}

	public int getGId() {
		return gid;
	}

	public String getLinkTarget() {
		return linkTarget;
	}

	public int getMTime() {
		return mtime;
	}

	public String getMtimeString() {
		Date date = new Date(((long) mtime) * 1000);
		return (date.toString());
	}

	public int getPermissions() {
		return permissions;
	}

	public String getPermissionsString() {
		StringBuffer buf = new StringBuffer(10);

		if (isDir()) {
			buf.append('d');
		} else if (isLink()) {
			buf.append('l');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IRUSR) != 0) {
			buf.append('r');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IWUSR) != 0) {
			buf.append('w');
		} else {
			buf.append('-');
		}

		if ((permissions & S_ISUID) != 0) {
			buf.append('s');
		} else if ((permissions & S_IXUSR) != 0) {
			buf.append('x');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IRGRP) != 0) {
			buf.append('r');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IWGRP) != 0) {
			buf.append('w');
		} else {
			buf.append('-');
		}

		if ((permissions & S_ISGID) != 0) {
			buf.append('s');
		} else if ((permissions & S_IXGRP) != 0) {
			buf.append('x');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IROTH) != 0) {
			buf.append('r');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IWOTH) != 0) {
			buf.append('w');
		} else {
			buf.append('-');
		}

		if ((permissions & S_IXOTH) != 0) {
			buf.append('x');
		} else {
			buf.append('-');
		}
		return (buf.toString());
	}

	public long getSize() {
		return size;
	}

	public int getUId() {
		return uid;
	}

	public boolean isDir() {
		return ((flags & SSH_FILEXFER_ATTR_PERMISSIONS) != 0 && ((permissions & S_IFDIR) == S_IFDIR));
	}

	public boolean isLink() {
		return ((flags & SSH_FILEXFER_ATTR_PERMISSIONS) != 0 && ((permissions & S_IFLNK) == S_IFLNK));
	}

	int length() {
		int len = 4;

		if ((flags & SSH_FILEXFER_ATTR_SIZE) != 0) {
			len += 8;
		}
		if ((flags & SSH_FILEXFER_ATTR_UIDGID) != 0) {
			len += 8;
		}
		if ((flags & SSH_FILEXFER_ATTR_PERMISSIONS) != 0) {
			len += 4;
		}
		if ((flags & SSH_FILEXFER_ATTR_ACMODTIME) != 0) {
			len += 8;
		}
		if ((flags & SSH_FILEXFER_ATTR_EXTENDED) != 0) {
			len += 4;
			int count = extended.length / 2;
			if (count > 0) {
				for (int i = 0; i < count; i++) {
					len += 4;
					len += extended[i * 2].length();
					len += 4;
					len += extended[i * 2 + 1].length();
				}
			}
		}
		return len;
	}

	public void setACMODTIME(int atime, int mtime) {
		flags |= SSH_FILEXFER_ATTR_ACMODTIME;
		this.atime = atime;
		this.mtime = mtime;
	}

	void setFLAGS(int flags) {
		this.flags = flags;
	}

	public void setLINKTARGET(String target) {
		this.linkTarget = target;
	}

	public void setPERMISSIONS(int permissions) {
		flags |= SSH_FILEXFER_ATTR_PERMISSIONS;
		permissions = (this.permissions & ~pmask) | (permissions & pmask);
		this.permissions = permissions;
	}

	public void setSIZE(long size) {
		flags |= SSH_FILEXFER_ATTR_SIZE;
		this.size = size;
	}

	public void setUIDGID(int uid, int gid) {
		flags |= SSH_FILEXFER_ATTR_UIDGID;
		this.uid = uid;
		this.gid = gid;
	}

	@Override
	public String toString() {
		return getPermissionsString() + " " + getUId() + " " + getGId() + " " + getSize() + " " + getMtimeString(); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$
	}
}
