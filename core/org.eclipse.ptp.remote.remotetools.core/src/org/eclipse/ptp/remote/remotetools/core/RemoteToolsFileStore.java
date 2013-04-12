/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   IBM Corporation - Initial API and implementation
 *   Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.remote.remotetools.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.RemoteServices;
import org.eclipse.ptp.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

public class RemoteToolsFileStore extends FileStore {
	private static Map<String, RemoteToolsFileStore> instanceMap = new HashMap<String, RemoteToolsFileStore>();

	/**
	 * Public factory method for obtaining RemoteToolsFileStore instances.
	 * 
	 * @param uri
	 *            URI to get a fileStore for
	 * @return an RemoteToolsFileStore instance for the URI.
	 */
	public static RemoteToolsFileStore getInstance(URI uri) {
		synchronized (instanceMap) {
			RemoteToolsFileStore store = instanceMap.get(uri.toString());
			if (store == null) {
				String name = RemoteToolsFileSystem.getConnectionNameFor(uri);
				if (name != null) {
					String path = uri.getPath();
					store = new RemoteToolsFileStore(name, path);
					instanceMap.put(uri.toString(), store);
				}
			}
			return store;
		}
	}

	private final String fConnectionName;
	private final IPath fRemotePath;

	private IRemoteItem fRemoteItem = null;

	public RemoteToolsFileStore(String connName, String path) {
		fConnectionName = connName;
		fRemotePath = new Path(path);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childInfos(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo[] childInfos(int options, IProgressMonitor monitor) throws CoreException {
		IRemoteItem[] items;
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		try {
			items = getExecutionManager(progress.newChild(50)).getRemoteFileTools().listItems(fRemotePath.toString(), monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, e.getMessage(), null));
		}
		IFileInfo[] result = new FileInfo[items.length];
		for (int i = 0; i < result.length; i++) {
			if (items[i].isSymLink()) {
				try {
					items[i].refreshAttributes(monitor);
				} catch (Exception e) {
					throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
							.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
				}
			}
			result[i] = convertRemoteItemToFileInfo(items[i], getNameFromPath(new Path(items[i].getPath())));
		}

		return result;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("CHILDNAMES: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem[] items;
		try {
			items = getExecutionManager(monitor).getRemoteFileTools().listItems(fRemotePath.toString(), monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, e.getMessage(), null));
		}

		String[] names = new String[items.length];

		for (int i = 0; i < items.length; i++) {
			IPath path = new Path(items[i].getPath());
			names[i] = path.lastSegment(); // should this be
											// getNameFromPath(path)?
		}

		return names;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		IRemoteItem item = getRemoteItem(monitor);

		//		System.out.println("DELETE: " + fRemotePath.toString() + ", exists: " + item.exists()); //$NON-NLS-1$ //$NON-NLS-2$

		if (item.exists()) {
			try {
				cacheRemoteItem(null);
				if (item.isDirectory()) {
					getExecutionManager(monitor).getRemoteFileTools().removeDirectory(fRemotePath.toString(), monitor);
				} else {
					getExecutionManager(monitor).getRemoteFileTools().removeFile(fRemotePath.toString(), monitor);
				}
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, NLS.bind(Messages.RemoteToolsFileStore_0, fRemotePath.toString()), e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("FETCHINFO: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem item = getRemoteItem(monitor);

		return convertRemoteItemToFileInfo(item, getName());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		//		System.out.println("GETCHILD: " + name); //$NON-NLS-1$
		URI uri = RemoteToolsFileSystem.getURIFor(fConnectionName, fRemotePath.append(name).toString());
		return RemoteToolsFileStore.getInstance(uri);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getName()
	 */
	@Override
	public String getName() {
		return getNameFromPath(fRemotePath);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#getParent()
	 */
	@Override
	public IFileStore getParent() {
		//		System.out.println("GETPARENT: " + fRemotePath.toString()); //$NON-NLS-1$
		if (fRemotePath.isRoot()) {
			return null;
		}
		String parentPath = fRemotePath.toString();
		if (fRemotePath.segmentCount() > 0) {
			parentPath = fRemotePath.removeLastSegments(1).toString();
		}
		return RemoteToolsFileStore.getInstance(RemoteToolsFileSystem.getURIFor(fConnectionName, parentPath));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#mkdir(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("MKDIR: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem item = getRemoteItem(monitor);

		if (!item.exists()) {
			if ((options & EFS.SHALLOW) == EFS.SHALLOW) {
				IFileStore parent = getParent();
				if (parent != null && !parent.fetchInfo(EFS.NONE, monitor).exists()) {
					throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
							.getSymbolicName(), EFS.ERROR_WRITE, NLS.bind(Messages.RemoteToolsFileStore_1, fRemotePath.toString()), null));
				}
			}

			try {
				getExecutionManager(monitor).getRemoteFileTools().createDirectory(fRemotePath.toString(), monitor);
				cacheRemoteItem(null);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, NLS.bind(Messages.RemoteToolsFileStore_2, fRemotePath.toString()), e));
			}
		} else if (!item.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_WRONG_TYPE, NLS.bind(Messages.RemoteToolsFileStore_13, fRemotePath.toString()), null));
		}

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openInputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("OPENINPUTSTREAM: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem item = getRemoteItem(monitor);

		if (!item.exists()) {
			//			System.out.println("OPENINPUTSTREAM: " + Messages.RemoteToolsFileStore_14); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_READ, NLS.bind(Messages.RemoteToolsFileStore_14, fRemotePath.toString()), null));
		}

		if (item.isDirectory()) {
			//			System.out.println("OPENINPUTSTREAM: " + Messages.RemoteToolsFileStore_3); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_WRONG_TYPE, NLS.bind(Messages.RemoteToolsFileStore_3, fRemotePath.toString()), null));
		}

		try {
			return getExecutionManager(monitor).getRemoteFileTools().getInputStream(item.getPath(), monitor);
		} catch (Exception e) {
			//			System.out.println("OPENINPUTSTREAM: " + e.getLocalizedMessage()); //$NON-NLS-1$
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, NLS.bind(Messages.RemoteToolsFileStore_4, fRemotePath.toString()), e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#openOutputStream(int,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("OPENOUTPUTSTREAM: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem item = getRemoteItem(monitor);

		if (item.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_WRONG_TYPE, Messages.RemoteToolsFileStore_3, null));
		}

		try {
			// Convert from EFS option constants to IFileService option
			// constants
			if ((options & EFS.APPEND) != 0) {
				options = IRemoteFileTools.APPEND;
			} else {
				options = IRemoteFileTools.NONE;
			}
			cacheRemoteItem(null);
			return getExecutionManager(monitor).getRemoteFileTools().getOutputStream(item.getPath(), options, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, Messages.RemoteToolsFileStore_6, e));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.core.filesystem.provider.FileStore#putInfo(org.eclipse.core
	 * .filesystem.IFileInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		//		System.out.println("PUTINFO: " + fRemotePath.toString()); //$NON-NLS-1$

		IRemoteItem item = getRemoteItem(monitor);

		boolean modified = false;
		if ((options & EFS.SET_ATTRIBUTES) != 0) {
			boolean writeable = !info.getAttribute(EFS.ATTRIBUTE_READ_ONLY);
			item.setWriteable(writeable);
			boolean executable = info.getAttribute(EFS.ATTRIBUTE_EXECUTABLE);
			item.setExecutable(executable);
			modified = true;
		}
		if ((options & EFS.SET_LAST_MODIFIED) != 0) {
			long modtime = info.getLastModified();
			item.setModificationTime(modtime);
			modified = true;
		}
		if (modified) {
			try {
				item.commitAttributes(monitor);
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, e.getMessage(), e));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.filesystem.provider.FileStore#toURI()
	 */
	@Override
	public URI toURI() {
		return RemoteToolsFileSystem.getURIFor(fConnectionName, fRemotePath.toString());
	}

	private void cacheRemoteItem(IRemoteItem item) {
		fRemoteItem = item;
	}

	/**
	 * Copy attributes from an IRemoteItem object into an IFileInfo object.
	 * 
	 * @param item
	 *            the IRemoteItem to convert
	 * @param name
	 *            the name of the file object
	 * @return an IFileInfo object containing the IRemoteItem attributes
	 */
	private IFileInfo convertRemoteItemToFileInfo(IRemoteItem item, String name) {
		FileInfo info = new FileInfo(name);

		if (!item.exists()) {
			info.setExists(false);
			return info;
		}

		info.setExists(true);
		info.setLastModified(item.getModificationTime());
		info.setDirectory(item.isDirectory());
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !item.isWritable());
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, item.isExecutable());
		info.setAttribute(EFS.ATTRIBUTE_SYMLINK, item.isSymLink());
		if (item.isSymLink()) {
			info.setStringAttribute(EFS.ATTRIBUTE_LINK_TARGET, item.getLinkTarget());
		}
		info.setLength(item.getSize());
		return info;
	}

	/**
	 * Get the remote tools execution manager associated with the connection.
	 * Will attempt to initialize and open the connection if necessary.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return remote tools execution manager
	 * @throws CoreException
	 */
	private IRemoteExecutionManager getExecutionManager(IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 100);
		final RemoteToolsServices services = (RemoteToolsServices) RemoteServices.getRemoteServices(
				RemoteToolsServices.REMOTE_TOOLS_ID, progress.newChild(20));
		if (services == null) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, Messages.RemoteToolsFileStore_5, null));
		}
		final RemoteToolsConnectionManager connMgr = (RemoteToolsConnectionManager) services.getConnectionManager();
		if (connMgr == null) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, Messages.RemoteToolsFileStore_7, null));
		}
		final RemoteToolsConnection conn = (RemoteToolsConnection) connMgr.getConnection(fConnectionName);
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, NLS.bind(Messages.RemoteToolsFileStore_8, fConnectionName), null));
		}
		if (!conn.isOpen()) {
			try {
				conn.open(progress.newChild(80));
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
			}
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, Messages.RemoteToolsFileStore_12, null));
			}
			if (!conn.isOpen()) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, NLS.bind(Messages.RemoteToolsFileStore_10, fConnectionName), null));
			}
		}
		try {
			return conn.createExecutionManager();
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
		}
	}

	/**
	 * Utility routing to get the file name from an absolute path.
	 * 
	 * @param path
	 *            path to extract file name from
	 * @return last segment of path, or the full path if it is root
	 */
	private String getNameFromPath(IPath path) {
		if (path.isRoot()) {
			return path.toString();
		}
		return path.lastSegment();
	}

	/**
	 * Gets the remote item associated with this file store. Will open the
	 * connection if necessary.
	 * 
	 * @param monitor
	 *            progress monitor
	 * @return remote item
	 * @throws CoreException
	 */
	private IRemoteItem getRemoteItem(IProgressMonitor monitor) throws CoreException {
		final IRemoteExecutionManager mgr = getExecutionManager(monitor);
		if (fRemoteItem == null) {
			try {
				IRemoteFileTools tools = mgr.getRemoteFileTools();
				cacheRemoteItem(tools.getItem(fRemotePath.toString()));
			} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
			} catch (RemoteOperationException e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
			} catch (CancelException e) {
				throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
						.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
			}
		}

		/*
		 * Always need to refresh attributes in case file has been modified
		 * since the last call.
		 */
		try {
			fRemoteItem.refreshAttributes(monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR, RemoteToolsAdapterCorePlugin.getDefault().getBundle()
					.getSymbolicName(), EFS.ERROR_INTERNAL, e.getLocalizedMessage(), e));
		}

		return fRemoteItem;
	}
}
