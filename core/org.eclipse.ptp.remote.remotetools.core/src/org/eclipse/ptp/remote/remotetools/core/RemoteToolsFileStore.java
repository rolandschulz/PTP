/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.remote.remotetools.core;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.remotetools.core.messages.Messages;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

public class RemoteToolsFileStore extends FileStore {
	private static Map<URI, RemoteToolsFileStore> instanceMap = new HashMap<URI, RemoteToolsFileStore>();

	/**
	 * Public factory method for obtaining RemoteToolsFileStore instances.
	 * @param uri URI to get a fileStore for
	 * @return an RemoteToolsFileStore instance for the URI.
	 */
	public static RemoteToolsFileStore getInstance(URI uri) {
		synchronized (instanceMap) {
			RemoteToolsFileStore store = (RemoteToolsFileStore)instanceMap.get(uri);
			if (store == null) {
				String name = uri.getAuthority();
				try {
					name = URLDecoder.decode(name, "UTF-8"); //$NON-NLS-1$
				} catch (UnsupportedEncodingException e) {
					// Should not happen
				}
				String path = uri.getPath();
				store = new RemoteToolsFileStore(name, path);
				instanceMap.put(uri, store);
			}
			return store;
		}
	}
	
	private final String fConnectionName;
	private final IPath fRemotePath;

	private IRemoteItem remoteItem = null;
	private boolean stale = true;
	
	public RemoteToolsFileStore(String connName, String path) {
		fConnectionName = connName;
		fRemotePath = new Path(path);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IRemoteItem[] items;
		try {
			items = getExecutionManager(monitor).getRemoteFileTools().listItems(fRemotePath.toString());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL, 
					e.getMessage(), null));
		}

		String[] names = new String[items.length];

		for (int i = 0; i < items.length; i++) {
			IPath path = new Path(items[i].getPath());
			names[i] = path.lastSegment();
		}

		return names;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IRemoteItem item = getRemoteItem(monitor);
		
		if (item.exists()) {
			try {
				stale = true;
				getExecutionManager(monitor).getRemoteFileTools().removeFile(fRemotePath.toString());
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						Messages.RemoteToolsFileStore_0, e));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IRemoteItem item = getRemoteItem(monitor);
		
		FileInfo info = new FileInfo(getName());

		if (!item.exists()) {
			info.setExists(false);
			return info;
		}	
		
		info.setExists(true);
		info.setLastModified(item.getModificationTime());
		info.setDirectory(item.isDirectory());
		info.setAttribute(EFS.ATTRIBUTE_READ_ONLY, !item.isWritable());
		info.setAttribute(EFS.ATTRIBUTE_EXECUTABLE, item.isExecutable());
		info.setLength(item.getSize());

		return info;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileStore#getChild(java.lang.String)
	 */
	@Override
	public IFileStore getChild(String name) {
		URI uri = RemoteToolsFileSystem.getURIFor(fConnectionName, fRemotePath.append(name).toString());
		return RemoteToolsFileStore.getInstance(uri);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#getName()
	 */
	@Override
	public String getName() {
		if (fRemotePath.isRoot()) {
			return fRemotePath.toString();
		}
		return fRemotePath.lastSegment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#getParent()
	 * 
	 * FIXME: should take a progress monitor as argument
	 * FIXME: should throw a core exception
	 */
	@Override
	public IFileStore getParent() {
		if (fRemotePath.isRoot()) {
			return null;
		}
		String parentPath = fRemotePath.toString();
		if (fRemotePath.segmentCount() > 0) {
			parentPath = fRemotePath.removeLastSegments(1).toString();
		}
		return RemoteToolsFileStore.getInstance(RemoteToolsFileSystem.getURIFor(fConnectionName, parentPath));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IFileStore mkdir(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IRemoteItem item = getRemoteItem(monitor);
		
		if (!item.exists()) {
			if ((options & EFS.SHALLOW) == EFS.SHALLOW) {
				IFileStore parent = getParent();
				if (parent != null && !parent.fetchInfo(EFS.NONE, monitor).exists()) {
					throw new CoreException(new Status(IStatus.ERROR,
							RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
							EFS.ERROR_WRITE,
							Messages.RemoteToolsFileStore_1, null));
				}
			}
			
			try {
				stale = true;
				getExecutionManager(monitor).getRemoteFileTools().createDirectory(fRemotePath.toString());
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						Messages.RemoteToolsFileStore_2, e));
			}
		} else if (!item.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE,
					Messages.RemoteToolsFileStore_13, null));
		}
		
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IRemoteItem item = getRemoteItem(monitor);
		
		if (!item.exists()) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_READ,
					Messages.RemoteToolsFileStore_14, null));
		}
		
		if (item.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE, 
					Messages.RemoteToolsFileStore_3, null));
		}
		
		try {
			stale = true;
			return getExecutionManager(monitor).getRemoteFileTools().getInputStream(item.getPath(), monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL,
					Messages.RemoteToolsFileStore_4, e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		
		IRemoteItem item = getRemoteItem(monitor);

		if (!item.exists()) {
			try {
				stale = true;
				getExecutionManager(monitor).getRemoteFileTools().createFile(item.getPath());
			} catch(Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						e.getMessage(), e));
			}
			
			item = getRemoteItem(monitor);
		}
		
		if (item.isDirectory()) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_WRONG_TYPE,
					Messages.RemoteToolsFileStore_3, null));
		}
		
		try {
			// Convert from EFS option constants to IFileService option constants
			if ((options & EFS.APPEND) != 0) {
				options = IRemoteFileTools.APPEND;
			} else {
				options = IRemoteFileTools.NONE;
			}
			stale = true;
			return getExecutionManager(monitor).getRemoteFileTools().getOutputStream(item.getPath(), options, monitor);
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL,
					Messages.RemoteToolsFileStore_6, e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#putInfo(org.eclipse.ptp.remote.core.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IFileInfo info, int options, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

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
				item.commitAttributes();
			} catch(Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						e.getMessage(), e));
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.AbstractRemoteResource#toURI()
	 */
	@Override
	public URI toURI() {
		return RemoteToolsFileSystem.getURIFor(fConnectionName, fRemotePath.toString());
	}

	/**
	 * Get the remote tools execution manager associated with the connection. Will attempt to
	 * initialize and open the connection if necessary.
	 * 
	 * @param monitor progress monitor
	 * @return remote tools execution manager
	 * @throws CoreException
	 */
	private IRemoteExecutionManager getExecutionManager(IProgressMonitor monitor) throws CoreException {
		final RemoteToolsServices services = RemoteToolsServices.getInstance();
		if (!services.isInitialized()) {
			services.initialize();
			if (!services.isInitialized()) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						Messages.RemoteToolsFileStore_5, null));
			}
		}
		final RemoteToolsConnectionManager connMgr = (RemoteToolsConnectionManager)services.getConnectionManager();
		if (connMgr == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL,
					Messages.RemoteToolsFileStore_7, null));
		}
		final RemoteToolsConnection conn = (RemoteToolsConnection)connMgr.getConnection(fConnectionName);
		if (conn == null) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL,
					NLS.bind(Messages.RemoteToolsFileStore_8, fConnectionName), null));
		}
		if (!conn.isOpen()) {
			try {
				conn.open(monitor);
			} catch (RemoteConnectionException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						e.getLocalizedMessage(), e));
			}
            if (monitor.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						EFS.ERROR_INTERNAL,
						Messages.RemoteToolsFileStore_12, null));
            }
    		if (!conn.isOpen()) {
    			throw new CoreException(new Status(IStatus.ERROR,
    					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
    					EFS.ERROR_INTERNAL,
    					NLS.bind(Messages.RemoteToolsFileStore_10, fConnectionName), null));
    		}
		}
		try {
			return conn.createExecutionManager();
		} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
			throw new CoreException(new Status(IStatus.ERROR,
					RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
					EFS.ERROR_INTERNAL,
					e.getLocalizedMessage(), e));
		}
	}

	/**
	 * Gets the remote item associated with this file store. Will open the connection
	 * if necessary.
	 * 
	 * @param monitor progress monitor
	 * @return remote item
	 * @throws CoreException
	 */
	private IRemoteItem getRemoteItem(IProgressMonitor monitor) throws CoreException {
		final IRemoteExecutionManager mgr = getExecutionManager(monitor);
		if (remoteItem == null) {
			try {
				IRemoteFileTools tools = mgr.getRemoteFileTools();
				remoteItem = tools.getItem(fRemotePath.toString());
			} catch (org.eclipse.ptp.remotetools.exception.RemoteConnectionException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						e.getLocalizedMessage()));
			} catch (RemoteOperationException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						e.getLocalizedMessage()));
			} catch (CancelException e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						e.getLocalizedMessage()));
			}
		}
		
		if (stale) {
			try {
				remoteItem.refreshAttributes();
			} catch (Exception e) {
				throw new CoreException(new Status(IStatus.ERROR,
						RemoteToolsAdapterCorePlugin.getDefault().getBundle().getSymbolicName(),
						e.getMessage(), e));
			}
		}

		return remoteItem;
	}
}
