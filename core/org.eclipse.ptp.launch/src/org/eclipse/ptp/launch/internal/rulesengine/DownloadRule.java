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
 * TODO: NEEDS TO BE DOCUMENTED!
 * 
 * Represents a download rule.
 * It describes how to copy a list of local files or local directories into a single remote directory.
 * A local file is copied into the remote directory.
 * A local directory has its content copied recursively into the remote directory.
 */
/**
 * @since 4.1
 */
public class DownloadRule implements ISynchronizationRule {
	private int overwritePolicy = OverwritePolicies.UNKNOWN;

	private boolean asReadOnly = false;
	private boolean asExecutable = false;
	private boolean preserveTimeStamp = false;

	/** Local destiny directory where the files are downloaded into. */
	private String localDirectory = null;

	/**
	 * List of remote source paths, represented as strings. They may be files or
	 * directories.
	 */
	private List<String> remoteFileList = new ArrayList<String>();

	/*
	 * Default constructor.
	 */
	public DownloadRule() {

	}

	/*
	 * Creates the rule from a serialized string.
	 */
	public DownloadRule(String data) {
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
				throwError(Messages.DownloadRule_0 + this.getClass().getName());
			}
			String s = list[0];
			if (!s.equalsIgnoreCase(SerializationKeys.TYPE_DOWNLOAD)) {
				throwError(Messages.DownloadRule_0 + this.getClass().getName());
			}
		}
		for (int i = 1; i < list.length; i++) {
			String s = list[i];
			/*
			 * Split key from data.
			 */
			int p = s.indexOf(' ');
			if (p == -1) {
				logError(Messages.DownloadRule_1 + s);
				continue;
			}
			String key = s.substring(0, p);
			String value = s.substring(p + 1);
			parseEntry(key, value);
		}
	}

	public DownloadRule(DownloadRule rule) {
		this.overwritePolicy = rule.overwritePolicy;
		this.asReadOnly = rule.asReadOnly;
		this.asExecutable = rule.asExecutable;
		this.preserveTimeStamp = rule.preserveTimeStamp;
		if (rule.localDirectory != null) {
			this.localDirectory = new String(rule.localDirectory);
		} else {
			this.localDirectory = null;
		}
		this.remoteFileList = new ArrayList<String>(rule.remoteFileList);
	}

	private void throwError(String string) {
		throw new RuntimeException(string);
	}

	private void parseEntry(String key, String value) {
		if (key.equalsIgnoreCase(SerializationKeys.KEY_LOCAL_PATH)) {
			localDirectory = value;
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
				logError(Messages.DownloadRule_2 + value);
			}
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_FLAGS)) {
			String flags[] = value.split(" "); //$NON-NLS-1$
			for (int i = 0; i < flags.length; i++) {
				String flag = flags[i];
				if (flag.equalsIgnoreCase(SerializationKeys.KEY_FLAGS_TIMESTAMP)) {
					preserveTimeStamp = true;
				} else {
					logError(Messages.DownloadRule_3 + flag);
				}
			}
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_REMOTE_PATH)) {
			remoteFileList.add(value);
		} else if (key.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS)) {
			String flags[] = value.split(" "); //$NON-NLS-1$
			for (int i = 0; i < flags.length; i++) {
				String flag = flags[i];
				if (flag.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS_EXECUTABLE)) {
					asExecutable = true;
				} else if (flag.equalsIgnoreCase(SerializationKeys.KEY_PERMISSIONS_READONLY)) {
					asReadOnly = true;
				} else {
					logError(Messages.DownloadRule_4 + flag);
				}
			}
		} else {
			logError(Messages.DownloadRule_5 + key);
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
		if (localDirectory != null) {
			l.add(SerializationKeys.KEY_LOCAL_PATH + " " + localDirectory.trim()); //$NON-NLS-1$
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
		if (preserveTimeStamp) {
			String s = SerializationKeys.KEY_FLAGS;
			if (preserveTimeStamp) {
				s += " " + SerializationKeys.KEY_FLAGS_TIMESTAMP; //$NON-NLS-1$
			}
			l.add(s);
		}
		for (Iterator<String> iter = remoteFileList.iterator(); iter.hasNext();) {
			String remotePath = iter.next();
			if (remotePath == null)
				continue;
			if (remotePath.trim().length() == 0)
				continue;

			l.add(SerializationKeys.KEY_REMOTE_PATH + " " + remotePath.trim()); //$NON-NLS-1$
		}
		String result = new String(SerializationKeys.TYPE_DOWNLOAD);
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

	public String getLocalDirectory() {
		if (localDirectory != null) {
			return localDirectory.trim();
		} else {
			return null;
		}
	}

	public void setLocalDirectory(String localDirectory) {
		if (localDirectory != null) {
			this.localDirectory = localDirectory.trim();
		} else {
			this.localDirectory = null;
		}
	}

	public int getRemoteFileCount() {
		return remoteFileList.size();
	}

	public String[] getRemoteFilesAsStringArray() {
		String result[] = new String[remoteFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = remoteFileList.get(i);
		}
		return result;
	}

	public IPath[] getRemoteFilesAsPathArray() {
		IPath result[] = new IPath[remoteFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new Path(remoteFileList.get(i));
		}
		return result;
	}

	public File[] getRemoteFilesAsFileArray() {
		File result[] = new File[remoteFileList.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = new File(remoteFileList.get(i));
		}
		return result;
	}

	public class RemoteFileIteratorAsString implements Iterator<Object> {
		Iterator<String> iteratorref = remoteFileList.iterator();

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

	public class RemoteFileIteratorAsPath extends RemoteFileIteratorAsString {
		@Override
		public Object next() {
			return new Path(iteratorref.next());
		}
	}

	public Iterator<?> remoteIteratorAsString() {
		return new RemoteFileIteratorAsString();
	}

	public Iterator<?> remoteIteratorAsPath() {
		return new RemoteFileIteratorAsPath();
	}

	public void addRemoteFile(String string) {
		if (string != null) {
			remoteFileList.add(string);
		}
	}

	public void addRemoteFile(IPath path) {
		if (path != null) {
			remoteFileList.add(path.toOSString());
		}
	}

	public static void main(String[] args) {
		DownloadRule r = new DownloadRule();
		System.out.println(r);
		r.setAsExecutable(true);
		System.out.println(r);
		r.setAsReadOnly(true);
		System.out.println(r);
		r.setLocalDirectory("/tmp/a"); //$NON-NLS-1$
		System.out.println(r);
		r.setOverwritePolicy(OverwritePolicies.ASK);
		System.out.println(r);
	}

	public void removeRemoteFile(String entry) {
		for (Iterator<String> iter = remoteFileList.iterator(); iter.hasNext();) {
			String element = iter.next();
			if (element.equals(entry)) {
				iter.remove();
			}
		}
	}

	public void removeRemoteFile(IPath entry) {
		removeRemoteFile(entry.toOSString());
	}

	public void setRemoteFiles(String[] items) {
		clearRemoteFiles();
		for (int i = 0; i < items.length; i++) {
			String string = items[i];
			if (string != null) {
				addRemoteFile(string.trim());
			}
		}
	}

	public void setRemoteFiles(IPath[] items) {
		clearRemoteFiles();
		for (int i = 0; i < items.length; i++) {
			IPath path = items[i];
			if (path != null) {
				addRemoteFile(path);
			}
		}
	}

	public void clearRemoteFiles() {
		remoteFileList.clear();
	}

	public boolean isActive() {
		return true;
	}

	public boolean isDownloadRule() {
		return true;
	}

	public boolean isUploadRule() {
		return false;
	}

	// FIXME: Throw core exception
	public void validate() throws CoreException {
		if (overwritePolicy == OverwritePolicies.UNKNOWN) {
			// RemoteLauncherPlugin.throwCoreException(Messages.DownloadRule_Validation_MissingOverwritePolicy,
			// IRemoteLaunchErrors.INVALID_RULE);
		}
		if (localDirectory == null) {
			// RemoteLauncherPlugin.throwCoreException(Messages.DownloadRule_Validation_MissingRemoteDirectory,
			// IRemoteLaunchErrors.INVALID_RULE);
		}
	}

}
