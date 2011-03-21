/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.rm.smoa.core.SMOACoreActivator;

import com.smoa.comp.sdk.SMOAStaging;
import com.smoa.comp.sdk.exceptions.DeleteOnTerminationException;
import com.smoa.comp.sdk.exceptions.FileNotFoundException;
import com.smoa.comp.sdk.exceptions.NotAuthorizedException;
import com.smoa.comp.sdk.exceptions.StagingException;
import com.smoa.comp.sdk.jsdl.JSDL;
import com.smoa.comp.sdk.jsdl.JSDLDataStaging;
import com.smoa.comp.sdk.types.FileInfo;
import com.smoa.comp.stubs.staging.FileNotFoundFault;
import com.smoa.comp.stubs.staging.NotAuthorizedFault;
import com.smoa.comp.stubs.staging.StagingFault;

/**
 * Represents a potential file or directory. {@see
 * http://help.eclipse.org/helios
 * /index.jsp?topic=/org.eclipse.platform.doc.isv/reference
 * /api/org/eclipse/core/filesystem/package-summary.html}
 */
public class SMOAFileStore extends FileStore {

	private final SMOAStaging staging;
	private final SMOAConnection connection;

	/** Absolute path to file */
	private final String path;

	/** If file exists */
	boolean exists = true;

	private FileInfo info;

	/** Parent file store (i.e. "../") */
	private final SMOAFileStore parent;

	// Maps used to co-operate with {@link SMOAFileStagingHandler}
	static public Map<String, SMOAFileStore> fileStoresWaitingForStaging = new ConcurrentHashMap<String, SMOAFileStore>();
	static public Map<String, InputStream> fileStoresWaitingForInputStream = new ConcurrentHashMap<String, InputStream>();
	static public Map<String, OutputStream> fileStoresWaitingForOutputStream = new ConcurrentHashMap<String, OutputStream>();

	// File info used in case of an error
	static final FileInfo errorFileInfo = new FileInfo(0, 0, null, null, 0,
			null, null, null);

	private static int counter = 0;

	private static Object ctr_lock = new Object();

	private static String chmodAttributesFromFileInfo(IFileInfo fileInfo) {
		int rights = 0;

		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE)) {
			rights |= 01;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_WRITE)) {
			rights |= 02;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OTHER_READ)) {
			rights |= 04;
		}

		if (fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE)) {
			rights |= 010;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_WRITE)) {
			rights |= 020;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_GROUP_READ)) {
			rights |= 040;
		}

		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE)) {
			rights |= 0100;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OWNER_WRITE)) {
			rights |= 0200;
		}
		if (fileInfo.getAttribute(EFS.ATTRIBUTE_OWNER_READ)) {
			rights |= 0400;
		}

		return Integer.toString(rights, 8);
	}

	/**
	 * Constructs new SMOAFileStore, does not fetch the info
	 * 
	 * @param path
	 *            - may be relative
	 * @param parent
	 *            - parent directory store
	 */
	public SMOAFileStore(String path, SMOAStaging staging,
			SMOAConnection connection, SMOAFileStore parent) {
		super();

		while (path.endsWith("/") && path.length() > 1) { //$NON-NLS-1$
			path = path.substring(0, path.length() - 1);
		}

		while (path.startsWith("./")) { //$NON-NLS-1$
			path = path.substring(2);
		}

		if (path.equals(".") || path.equals("")) { //$NON-NLS-1$ //$NON-NLS-2$
			path = connection.getHomeDir();
		}

		if (!path.startsWith("/")) { //$NON-NLS-1$
			path = connection.getHomeDir() + "/" + path; //$NON-NLS-1$
		}

		this.path = path;
		this.connection = connection;
		this.staging = staging;
		this.parent = parent;
	}

	/**
	 * Returns last fetched IFileInfo, or null if no info has been fetched yet.
	 */
	public IFileInfo cachedInfo() throws CoreException {

		org.eclipse.core.filesystem.provider.FileInfo fileInfo;
		fileInfo = new org.eclipse.core.filesystem.provider.FileInfo(path);

		fileInfo.setExists(exists);

		if (!exists) {
			return fileInfo;
		}

		if (info == null) {
			return null;
		}
		
		fileInfo.setDirectory((info.getMode() & FileInfo.S_IFDIR) != 0);

		fileInfo.setLastModified(info.getModificationTime().getTime());

		fileInfo.setLength(info.getSize());

		fileInfo.setAttribute(EFS.ATTRIBUTE_EXECUTABLE,
				(info.getMode() & 0111) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_HIDDEN, getName().matches("^.")); //$NON-NLS-1$
		fileInfo.setAttribute(EFS.ATTRIBUTE_READ_ONLY,
				(info.getMode() & 0444) == 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_SYMLINK,
				(info.getMode() & FileInfo.S_IFLNK) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_EXECUTE,
				(info.getMode() & 0100) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_READ,
				(info.getMode() & 0400) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OWNER_WRITE,
				(info.getMode() & 0200) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_EXECUTE,
				(info.getMode() & 010) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_READ,
				(info.getMode() & 040) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_GROUP_WRITE,
				(info.getMode() & 020) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_EXECUTE,
				(info.getMode() & 01) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_READ,
				(info.getMode() & 04) != 0);
		fileInfo.setAttribute(EFS.ATTRIBUTE_OTHER_WRITE,
				(info.getMode() & 02) != 0);

		if ((info.getMode() & FileInfo.S_IFLNK) != 0) {
			fileInfo.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, path);
		}

		return fileInfo;
	}

	/**
	 * Returns IFileInfos for all children in this directory. Uses one call for
	 * all files instead of a call for every file. If fails, falls back to
	 * default implementation.
	 */
	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor)
			throws CoreException {

		final List<String> fileNames = new Vector<String>();
		final String[] childNames = childNames(options, monitor);

		if (childNames.length == 0) {
			return new IFileInfo[0];
		}

		for (final String childName : childNames) {
			fileNames.add(path + "/" + childName); //$NON-NLS-1$
		}

		FileInfo[] infos;
		try {
			infos = staging.statFile(fileNames, null);
		} catch (final FileNotFoundFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final NotAuthorizedFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final StagingFault e) {
			return super.childInfos(options, monitor);
		}

		final SMOAFileStore[] stores = new SMOAFileStore[fileNames.size()];
		final IFileInfo[] smoaInfos = new IFileInfo[fileNames.size()];
		for (int i = 0; i < infos.length; i++) {
			stores[i] = fromChildInfo(infos[i], fileNames.get(i));
			smoaInfos[i] = stores[i].cachedInfo();
		}

		return smoaInfos;
	}

	/**
	 * Returns the directory listing (like <tt>ls -1</tt>)
	 */
	@Override
	public String[] childNames(int arg0, IProgressMonitor arg1)
			throws CoreException {

		fetchInfo(arg0, arg1);

		if (!exists) {
			throw new CoreException(new Status(IStatus.WARNING,
					SMOACoreActivator.PLUGIN_ID,
					Messages.SMOAFileStore_RequestedListingUnexistingDirOrFile));
		}

		if ((info.getMode() & FileInfo.S_IFDIR) == 0) {
			return new String[0];
		}

		JSDL jsdl;

		try {
			jsdl = staging.listDirectory(path, null);
		} catch (final FileNotFoundFault e) {
			throw new RuntimeException(e);
		} catch (final NotAuthorizedFault e) {
			throw new RuntimeException(e);
		}

		final Vector<String> names = new Vector<String>();
		for (final JSDLDataStaging ds : jsdl.getDataStaging()) {
			if (ds.getFileName().equals(".") || ds.getFileName().equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
				continue;
			}
			names.add(ds.getFileName());
		}
		return names.toArray(new String[names.size()]);
	}

	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			staging.delete(path, null);
		} catch (final FileNotFoundFault e) {
		} catch (final NotAuthorizedFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final StagingFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}
	}

	/**
	 * Fetches info for a single file
	 */
	@Override
	public IFileInfo fetchInfo(int arg0, IProgressMonitor arg1)
			throws CoreException {
		try {
			final List<String> l = new Vector<String>();
			l.add(path);
			info = staging.statFile(l, null)[0];
			exists = true;
		} catch (final FileNotFoundFault e) {
			exists = false;
		} catch (final NotAuthorizedFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final StagingFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}

		return cachedInfo();

	}

	/**
	 * If one has a FileInfo of a child, one may create FileStore with it
	 */
	protected SMOAFileStore fromChildInfo(FileInfo info, String path) {
		final SMOAFileStore store = new SMOAFileStore(path, staging, connection, this);
		store.info = info;
		store.exists = true;
		return store;
	}

	/**
	 * Returns a file from this directory
	 * 
	 * @param name
	 *            - name, relative to this directory
	 */
	@Override
	public SMOAFileStore getChild(String name) {

		if (name == null || name.isEmpty()) {
			return null;
		}

		if (name.equals(".")) { //$NON-NLS-1$
			return this;
		}

		if (name.startsWith("/")) { //$NON-NLS-1$
			if (name.startsWith(path) && !name.equals(path)) {
				return new SMOAFileStore(name, staging, getConnection(), this);
			}

			return new SMOAFileStore(name, staging, getConnection(), null);
		}

		if (name.equals("..") || name.contains("/")) { //$NON-NLS-1$ //$NON-NLS-2$
			return new SMOAFileStore(path + "/" + name, staging, //$NON-NLS-1$
					getConnection(), null);
		}

		return new SMOAFileStore(path + "/" + name, staging, //$NON-NLS-1$
				getConnection(), this);
	}

	public SMOAConnection getConnection() {
		return connection;
	}

	/**
	 * Returns file name
	 */
	@Override
	public String getName() {
		if (path.endsWith("/") && path.length() > 1) { //$NON-NLS-1$
			final String p = path.substring(0, path.length() - 1);
			return p.substring(p.lastIndexOf('/') + 1);
		}
		return path.substring(path.lastIndexOf('/') + 1);
	}

	/**
	 * Returns (creating if needed) parent store
	 */
	@Override
	public SMOAFileStore getParent() {
		if (parent != null) {
			return parent;
		}

		if (path.indexOf('/') == -1) {
			return null;
		}

		if (path.equals("/")) { //$NON-NLS-1$
			return null;
		}

		final String parentPath = path.substring(0, path.lastIndexOf('/'));
		return new SMOAFileStore(parentPath, staging, getConnection(), null);
	}

	public String getPath() {
		return path;
	}

	@Override
	public SMOAFileStore mkdir(int options, IProgressMonitor monitor)
			throws CoreException {

		if (this.fetchInfo().isDirectory()) {
			return this;
		}

		if (exists) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID,
					Messages.SMOAFileStore_MkdirOverAnExistingFile));
		}

		final IFileStore par = getParent();
		if (!par.fetchInfo().isDirectory()) {
			par.mkdir(options, monitor);
		}

		try {
			staging.mkdir(path, null, null);
		} catch (final FileNotFoundFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final NotAuthorizedFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		} catch (final StagingFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID, e.getLocalizedMessage(), e));
		}

		fetchInfo();
		return this;
	}

	/**
	 * Co-working with {@link SMOAStagingHandler} gets the {@link InputStream}
	 * from the file
	 */
	@Override
	public InputStream openInputStream(int arg0, IProgressMonitor arg1)
			throws CoreException {
		return openInputStream(arg0, arg1, 0);
	}

	/**
	 * Co-working with {@link SMOAStagingHandler} gets the {@link InputStream}
	 * from the file, from specified offset (in bytes) on
	 */
	public InputStream openInputStream(int arg0, IProgressMonitor arg1,
			Integer offset) throws CoreException {

		int lcounter;
		synchronized (ctr_lock) {
			lcounter = counter;
			counter++;
		}

		final JSDL jsdl = new JSDL("openInputStream_" + path); //$NON-NLS-1$
		final JSDLDataStaging dataStaging = new JSDLDataStaging(path, null, path
				+ " " + lcounter); //$NON-NLS-1$

		dataStaging.setOffset(offset);

		jsdl.getDataStaging().add(dataStaging);
		Throwable t;

		try {
			staging.stageOutFiles(jsdl);

			final InputStream is = fileStoresWaitingForInputStream.remove(path
					+ " " + lcounter); //$NON-NLS-1$

			// FIXME:
			if (is == null) {
				throw new CoreException(new Status(IStatus.ERROR,
						SMOACoreActivator.PLUGIN_ID,
						Messages.SMOAFileStore_InputStreamForFileNotReceived));
			}

			return is;

		} catch (final DeleteOnTerminationException e) {
			t = e;
		} catch (final FileNotFoundException e) {
			t = e;
		} catch (final NotAuthorizedException e) {
			t = e;
		} catch (final StagingException e) {
			t = e;
		} catch (final IOException e) {
			t = e;
		}
		throw new CoreException(new Status(IStatus.ERROR,
				SMOACoreActivator.PLUGIN_ID, t.getLocalizedMessage(), t));
	}

	/**
	 * Opens {@link OutputStream} for the file. Overwrites if file exists.
	 * 
	 * If the file doesn't exist, the file is created.
	 * 
	 * If the parent directory doesn't exist, it's also created.
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {

		// Ensure that the parent dir exists
		getParent().mkdir(options, monitor);

		int lcounter;
		synchronized (ctr_lock) {
			lcounter = counter;
			counter++;
		}

		final JSDL jsdl = new JSDL("openOutputStream_" + path); //$NON-NLS-1$
		jsdl.getDataStaging().add(
				new JSDLDataStaging(path, path + " " + lcounter, //$NON-NLS-1$
						null));
		final Throwable[] t = new Throwable[1];
		t[0] = null;

		fileStoresWaitingForStaging.put(path + " " + lcounter, this); //$NON-NLS-1$

		synchronized (this) {
			new Thread() {
				@Override
				public void run() {
					setName("stageIn_" + path); //$NON-NLS-1$
					try {
						staging.stageInFiles(jsdl);
					} catch (final FileNotFoundException e) {
						t[0] = e;
					} catch (final NotAuthorizedException e) {
						t[0] = e;
					} catch (final StagingException e) {
						t[0] = e;
					} catch (final IOException e) {
						t[0] = e;
					}
				}
			}.start();

			try {
				this.wait();
			} catch (final InterruptedException e) {
			}
		}

		fileStoresWaitingForStaging.remove(path + " " + lcounter); //$NON-NLS-1$

		final OutputStream os = fileStoresWaitingForOutputStream.remove(path
				+ " " + lcounter); //$NON-NLS-1$

		if (t[0] == null) {
			return os;
		}

		throw new CoreException(new Status(IStatus.ERROR,
				SMOACoreActivator.PLUGIN_ID, t[0].getLocalizedMessage(), t[0]));
	}

	/**
	 * Changes file properties - in SMOA case, only chmod is supported
	 */
	@Override
	public void putInfo(IFileInfo arg0, int arg1, IProgressMonitor arg2)
			throws CoreException {
		try {
			staging.chmod(path, chmodAttributesFromFileInfo(arg0), null);
		} catch (final FileNotFoundFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID,
					Messages.SMOAFileStore_ChmodFailed
							+ e.getLocalizedMessage(), e));
		} catch (final NotAuthorizedFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID,
					Messages.SMOAFileStore_ChmodFailed
							+ e.getLocalizedMessage(), e));
		} catch (final StagingFault e) {
			throw new CoreException(new Status(IStatus.ERROR,
					SMOACoreActivator.PLUGIN_ID,
					Messages.SMOAFileStore_ChmodFailed
							+ e.getLocalizedMessage(), e));
		}
	}

	@Override
	public URI toURI() {
		try {
			return new URI(path);
		} catch (final URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

}