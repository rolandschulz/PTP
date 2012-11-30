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
package org.eclipse.ptp.launch.internal.rulesengine;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.OverwritePolicies;

/*
 * TODO: NEEDS TO BE DOCUMENTED
 * Represents an upload rule.
 * It describes how to copy a list of local files or local directories into a single remote directory.
 * A local file is copied into the remote directory.
 * A local directory has its content copied recursively into the remote directory.
 */
public class UploadRule implements ISynchronizationRule {
	private int overwritePolicy = OverwritePolicies.UNKNOWN;

	private boolean asReadOnly = false;

	private boolean asExecutable = false;

	private boolean preserveTimeStamp = false;

	private boolean downloadBack = false;

	private boolean defaultRemoteDirectory = false;

	private String remoteDirectory = null;

	/*
	 * List of local paths, represented as strings. They may be files or
	 * directories.
	 */
	private List<String> localFileList = new ArrayList<String>();

	/*
	 * Default constructor.
	 */
	public UploadRule() {

	}

	/*
	 * Creates de rule from a serialized string.
	 */
	public UploadRule(String data) {
		/*
		 * Split the string into lines.
		 */
		String list[] = data.split("\n"); //$NON-NLS-1$
		/*
		 * Check if the first token is the proper identifier. If not, the string
		 * does not represent a rule that can be parsed by this class.
		 */
		{
			if (list.length < 1) {
				throwError(Messages.UploadRule_0 + this.getClass().getName());
			}
			String s = list[0];
			if (!s.equalsIgnoreCase(SerializationKeys.TYPE_UPLOAD)) {
				throwError(Messages.UploadRule_0 + this.getClass().getName());
			}
		}
		for (int i = 1; i < list.length; i++) {
			String s = list[i];
			/*
			 * Split key from data.
			 */
			int p = s.indexOf(' ');
			if (p == -1) {
				logError(Messages.UploadRule_1 + s);
				continue;
			}
			String key = s.substring(0, p);
			String value = s.substring(p + 1);
			parseEntry(key, value);
		}
	}

	public UploadRule(UploadRule rule) {
		this.overwritePolicy = rule.overwritePolicy;
		this.asReadOnly = rule.asReadOnly;
		this.asExecutable = rule.asExecutable;
		this.preserveTimeStamp = rule.preserveTimeStamp;
		this.downloadBack = rule.downloadBack;
		this.defaultRemoteDirectory = rule.defaultRemoteDirectory;
		if (rule.remoteDirectory != null) {
			this.remoteDirectory = new String(rule.remoteDirectory);
		} else {
			this.remoteDirectory = null;
		}
		this.localFileList = new ArrayList<String>(rule.localFileList);
	}

	private void throwError(String string) {
		throw new RuntimeException(string);
	}

	private void parseEntry(String key, String value) {
		if (key.equalsIgnoreCase(SerializationKeys.KEY_REMOTE_PATH)) {
			remoteDirectory = value;
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_OVERWRITE_POLICY)) {
			if (value.equalsIgnoreCase(SerializationKeys.KEY_OVERWRITE_POLICY_ALWAYS)) {
				overwritePolicy = OverwritePolicies.ALWAYS;
			} else if (value.equalsIgnoreCase(SerializationKeys.KEY_OVERWRITE_POLICY_ASK)) {
				overwritePolicy = OverwritePolicies.ASK;
			} else if (value.equalsIgnoreCase(SerializationKeys.KEY_OVERWRITE_POLICY_NEWER)) {
				overwritePolicy = OverwritePolicies.NEWER;
			} else if (value.equalsIgnoreCase(SerializationKeys.KEY_OVERWRITE_POLICY_SKIP)) {
				overwritePolicy = OverwritePolicies.SKIP;
			} else {
				logError(Messages.UploadRule_2 + value);
			}
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_FLAGS)) {
			String flags[] = value.split(" "); //$NON-NLS-1$
			for (int i = 0; i < flags.length; i++) {
				String flag = flags[i];
				if (flag.equalsIgnoreCase(SerializationKeys.KEY_FLAGS_DEFAULT_REMOTE_DIRECTORY)) {
					defaultRemoteDirectory = true;
				} else if (flag.equalsIgnoreCase(SerializationKeys.KEY_FLAGS_TIMESTAMP)) {
					preserveTimeStamp = true;
				} else if (flag.equalsIgnoreCase(SerializationKeys.KEY_FLAGS_DOWNLOAD_BACK)) {
					downloadBack = true;
				} else {
					logError(Messages.UploadRule_3 + flag);
				}
			}
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_LOCAL_PATH)) {
			localFileList.add(value);
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS)) {
			String flags[] = value.split(" "); //$NON-NLS-1$
			for (int i = 0; i < flags.length; i++) {
				String flag = flags[i];
				if (flag.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS_EXECUTABLE)) {
					asExecutable = true;
				} else if (flag.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS_READONLY)) {
					asReadOnly = true;
				} else {
					logError(Messages.UploadRule_4 + flag);
				}
			}
		} else {
			logError(Messages.UploadRule_5 + key);
		}
	}

	private void logError(String string) {
		// TODO NEEDS TO BE IMPLEMENTED
	}

	/*
	 * The rule as a serialized string.
	 */
	@Override
	public String toString() {
		List<String> l = new ArrayList<String>();
		if (remoteDirectory != null) {
			l.add(SerializationKeys.KEY_REMOTE_PATH + " " + remoteDirectory.trim()); //$NON-NLS-1$
		}
		if (overwritePolicy == OverwritePolicies.ALWAYS) {
			l.add(SerializationKeys.KEY_OVERWRITE_POLICY + " " + SerializationKeys.KEY_OVERWRITE_POLICY_ALWAYS); //$NON-NLS-1$
		} else if (overwritePolicy == OverwritePolicies.ASK) {
			l.add(SerializationKeys.KEY_OVERWRITE_POLICY + " " + SerializationKeys.KEY_OVERWRITE_POLICY_ASK); //$NON-NLS-1$
		} else if (overwritePolicy == OverwritePolicies.NEWER) {
			l.add(SerializationKeys.KEY_OVERWRITE_POLICY + " " + SerializationKeys.KEY_OVERWRITE_POLICY_NEWER); //$NON-NLS-1$
		} else if (overwritePolicy == OverwritePolicies.SKIP) {
			l.add(SerializationKeys.KEY_OVERWRITE_POLICY + " " + SerializationKeys.KEY_OVERWRITE_POLICY_SKIP); //$NON-NLS-1$
		}
		if (asExecutable || asReadOnly) {
			String s = SerializationKeys.KEY_PERMISSIONS;
			if (asExecutable) {
				s += " " + SerializationKeys.KEY_PERMISSIONS_EXECUTABLE; //$NON-NLS-1$
			}
			if (asReadOnly) {
				s += " " + SerializationKeys.KEY_PERMISSIONS_READONLY; //$NON-NLS-1$
			}
			l.add(s);
		}
		if (preserveTimeStamp || defaultRemoteDirectory || downloadBack) {
			String s = SerializationKeys.KEY_FLAGS;
			if (defaultRemoteDirectory) {
				s += " " + SerializationKeys.KEY_FLAGS_DEFAULT_REMOTE_DIRECTORY; //$NON-NLS-1$
			}
			if (preserveTimeStamp) {
				s += " " + SerializationKeys.KEY_FLAGS_TIMESTAMP; //$NON-NLS-1$
			}
			if (downloadBack) {
				s += " " + SerializationKeys.KEY_FLAGS_DOWNLOAD_BACK; //$NON-NLS-1$
			}
			l.add(s);
		}
		for (Iterator<String> iter = localFileList.iterator(); iter.hasNext();) {
			String localpath = iter.next();
			if (localpath == null)
				continue;
			if (localpath.trim().length() == 0)
				continue;

			l.add(SerializationKeys.KEY_LOCAL_PATH + " " + localpath.trim()); //$NON-NLS-1$
		}
		String result = new String(SerializationKeys.TYPE_UPLOAD);
		for (Iterator<String> iter = l.iterator(); iter.hasNext();) {
			String element = iter.next();
			result += "\n" + element; //$NON-NLS-1$
		}
		return result;
	}

	public boolean isAsExecutable() {
		return asExecutable;
	}

	public void setAsExecutable(boolean asExecutable) {
		this.asExecutable = asExecutable;
	}

	public boolean isAsReadOnly() {
		return asReadOnly;
	}

	public void setAsReadOnly(boolean asReadOnly) {
		this.asReadOnly = asReadOnly;
	}

	public boolean isDefaultRemoteDirectory() {
		return defaultRemoteDirectory;
	}

	public void setDefaultRemoteDirectory(boolean defaultRemoteDirectory) {
		this.defaultRemoteDirectory = defaultRemoteDirectory;
	}

	public int getOverwritePolicy() {
		return overwritePolicy;
	}

	public void setOverwritePolicy(int overwritePolicy) {
		this.overwritePolicy = overwritePolicy;
	}

	public boolean isPreserveTimeStamp() {
		return preserveTimeStamp;
	}

	public void setPreserveTimeStamp(boolean preserveTimeStamp) {
		this.preserveTimeStamp = preserveTimeStamp;
	}

	public String getRemoteDirectory() {
		if (remoteDirectory != null) {
			return remoteDirectory.trim();
		} else {
			return null;
		}
	}

	public void setRemoteDirectory(String remoteDirectory) {
		if (remoteDirectory != null) {
			this.remoteDirectory = remoteDirectory.trim();
		} else {
			this.remoteDirectory = null;
		}
	}

	public void setDownloadBack(boolean downloadBack) {
		this.downloadBack = downloadBack;
	}

	public boolean isDownloadBack() {
		return downloadBack;
	}

	public int getRemoteFileCount() {
		return localFileList.size();
	}

	public String[] getLocalFilesAsStringArray() {
		String result[] = new String[localFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = localFileList.get(i);
		}
		return result;
	}

	public IPath[] getLocalFilesAsPathArray() {
		IPath result[] = new IPath[localFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Path(localFileList.get(i));
		}
		return result;
	}

	public File[] getLocalFilesAsFileArray() {
		File result[] = new File[localFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new File(localFileList.get(i));
		}
		return result;
	}

	public class LocalFileIteratorAsString implements Iterator<Object> {
		Iterator<String> iteratorref = localFileList.iterator();

		public boolean hasNext() {
			return iteratorref.hasNext();
		}

		public Object next() {
			return iteratorref.next();
		}

		public void remove() {
			iteratorref.remove();
		}
	}

	public class LocalFileIteratorAsPath extends LocalFileIteratorAsString {
		@Override
		public Object next() {
			return new Path(iteratorref.next());
		}
	}

	public class LocalFileIteratorAsFile extends LocalFileIteratorAsString {
		@Override
		public Object next() {
			return new File(iteratorref.next());
		}
	}

	public Iterator<?> localIteratorAsString() {
		return new LocalFileIteratorAsString();
	}

	public Iterator<?> localIteratorAsPath() {
		return new LocalFileIteratorAsPath();
	}

	public Iterator<?> localIteratorAsFile() {
		return new LocalFileIteratorAsFile();
	}

	public void addLocalFile(String string) {
		if (string != null) {
			localFileList.add(string);
		}
	}

	public void addLocalFile(IPath path) {
		if (path != null) {
			localFileList.add(path.toOSString());
		}
	}

	public void addLocalFile(File file) {
		if (file != null) {
			localFileList.add(file.getPath());
		}
	}

	public static void main(String[] args) {
		UploadRule r = new UploadRule();
		System.out.println(r);
		r.setAsExecutable(true);
		System.out.println(r);
		r.setAsReadOnly(true);
		System.out.println(r);
		r.setRemoteDirectory("/tmp/a"); //$NON-NLS-1$
		System.out.println(r);
		r.setOverwritePolicy(OverwritePolicies.ASK);
		System.out.println(r);

		String s = "upload\nremote-directory /tmp/a\npermissions executable readonly\nflags default-remote-directory"; //$NON-NLS-1$
		r = new UploadRule(s);
		System.out.println(r);

	}

	public void removeLocalFile(String entry) {
		for (Iterator<String> iter = localFileList.iterator(); iter.hasNext();) {
			String element = iter.next();
			if (element.equals(entry)) {
				iter.remove();
			}
		}
	}

	public void removeLocalFile(IPath entry) {
		removeLocalFile(entry.toOSString());
	}

	public void removeLocalFile(File entry) {
		removeLocalFile(entry.getPath());
	}

	public void setLocalFiles(String[] items) {
		clearLocalFiles();
		for (int i = 0; i < items.length; i++) {
			String string = items[i];
			if (string != null) {
				addLocalFile(string);
			}
		}
	}

	public void setLocalFiles(IPath[] items) {
		clearLocalFiles();
		for (int i = 0; i < items.length; i++) {
			IPath path = items[i];
			if (path != null) {
				addLocalFile(path);
			}
		}
	}

	public void setLocalFiles(File[] items) {
		clearLocalFiles();
		for (int i = 0; i < items.length; i++) {
			File file = items[i];
			if (file != null) {
				addLocalFile(file);
			}
		}
	}

	public void clearLocalFiles() {
		localFileList.clear();
	}

	public boolean isActive() {
		return true;
	}

	public boolean isDownloadRule() {
		return false;
	}

	public boolean isUploadRule() {
		return true;
	}

	// FIXME: Exceptions not thrown
	public void validate() throws CoreException {
		if (overwritePolicy == OverwritePolicies.UNKNOWN) {
			// RemoteLauncherPlugin.throwCoreException(Messages.UploadRule_Validate_MissingOverwritePolicy,
			// IRemoteLaunchErrors.INVALID_RULE);
		}
		if (defaultRemoteDirectory == false) {
			if (remoteDirectory == null) {
				// RemoteLauncherPlugin.throwCoreException(Messages.UploadRule_Validate_MissingRemotedirectory,
				// IRemoteLaunchErrors.INVALID_RULE);
			}
			// IPath remotePath = LinuxPath.fromString(remoteDirectory);
			// if (! remotePath.isAbsolute()) {
			// RemoteLauncherPlugin.throwCoreException("Remote directory must be an absolute path.",
			// IRemoteLaunchErrors.INVALID_RULE);
			// }
		}
	}
}
