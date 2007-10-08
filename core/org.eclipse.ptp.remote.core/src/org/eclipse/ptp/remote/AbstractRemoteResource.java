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
package org.eclipse.ptp.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.filesystem.provider.FileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;


public abstract class AbstractRemoteResource implements IRemoteResource {
	/**
	 * Singleton buffer created to avoid buffer creations in the
	 * transferStreams method.  Used as an optimization, based on the assumption
	 * that multiple writes won't happen in a given instance of FileStore.
	 */
	private static final byte[] buffer = new byte[8192];

	/**
	 * A file info array of size zero that can be used as a return value for methods
	 * that return IFileInfo[] to avoid creating garbage objects.
	 */
	protected static final IFileInfo[] EMPTY_FILE_INFO_ARRAY = new IFileInfo[0];

	/**
	 * A string array of size zero that can be used as a return value for methods
	 * that return String[] to avoid creating garbage objects.
	 */
	protected static final String[] EMPTY_STRING_ARRAY = new String[0];

	/**
	 * Transfers the contents of an input stream to an output stream, using a large
	 * buffer.
	 * 
	 * @param source The input stream to transfer
	 * @param destination The destination stream of the transfer
	 * @param path A path representing the data being transferred for use in error
	 * messages.
	 * @param monitor A progress monitor.  The monitor is assumed to have
	 * already done beginWork with one unit of work allocated per buffer load
	 * of contents to be transferred.
	 * @throws CoreException
	 */
	private static final void transferStreams(InputStream source, OutputStream destination, 
			String path, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		try {
			/*
			 * Note: although synchronizing on the buffer is thread-safe,
			 * it may result in slower performance in the future if we want 
			 * to allow concurrent writes.
			 */
			synchronized (buffer) {
				while (true) {
					int bytesRead = -1;
					try {
						bytesRead = source.read(buffer);
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR,
								PTPRemotePlugin.getDefault().getBundle().getSymbolicName(),
								"read error"));
					}
					if (bytesRead == -1)
						break;
					try {
						destination.write(buffer, 0, bytesRead);
					} catch (IOException e) {
						throw new CoreException(new Status(IStatus.ERROR,
								PTPRemotePlugin.getDefault().getBundle().getSymbolicName(),
								"write error"));
					}
					monitor.worked(1);
				}
			}
		} finally {
			try {
				source.close();
				destination.close();
			} catch (IOException e) {
			}
		}
	}

	/**
	 * The default implementation of {@link IFileStore#childInfos(int, IProgressMonitor)}.
	 * Subclasses should override this method where a more efficient implementation
	 * is possible.  This default implementation calls {@link #fetchInfo()} on each
	 * child, which will result in a file system call for each child.
	 */
	public IRemoteResourceInfo[] childResourceInfos(int options, IProgressMonitor monitor) throws CoreException {
		IRemoteResource[] childResources = childResources(options, monitor);
		IRemoteResourceInfo[] childInfos = new IRemoteResourceInfo[childResources.length];
		for (int i = 0; i < childResources.length; i++) {
			childInfos[i] = childResources[i].fetchInfo();
		}
		return childInfos;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract String[] childNames(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * The default implementation of {@link IFileStore#childStores(int, IProgressMonitor)}.
	 * Subclasses may override.
	 */
	public IRemoteResource[] childResources(int options, IProgressMonitor monitor) throws CoreException {
		String[] children = childNames(options, monitor);
		IRemoteResource[] wrapped = new IRemoteResource[children.length];
		for (int i = 0; i < wrapped.length; i++)
			wrapped[i] = getChild(children[i], monitor);
		return wrapped;
	}

	/**
	 * The default implementation of {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.
	 * This implementation performs a copy by using other primitive methods. 
	 * Subclasses may override this method.
	 */
	public void copy(IRemoteResource destination, int options, IProgressMonitor monitor) throws CoreException {
		final IRemoteResourceInfo sourceInfo = fetchInfo(EFS.NONE, null);
		if (sourceInfo.isDirectory())
			copyDirectory(sourceInfo, destination, options, monitor);
		else
			copyFile(sourceInfo, destination, options, monitor);
	}

	/**
	 * Recursively copies a directory as specified by 
	 * {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.
	 * 
	 * @param sourceInfo The current file information for the source of the move
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link EFS#OVERWRITE} or {@link EFS#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The <code>OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	protected void copyDirectory(IRemoteResourceInfo sourceInfo, IRemoteResource destination, 
			int options, IProgressMonitor monitor) throws CoreException {
		try {
			IRemoteResource[] children = null;
			int opWork = 1;
			if ((options & EFS.SHALLOW) == 0) {
				children = childResources(EFS.NONE, null);
				opWork += children.length;
			}
			monitor.beginTask("", opWork); //$NON-NLS-1$
			monitor.subTask("copying"); //$NON-NLS-1$
			// create directory
			destination.mkdir(EFS.NONE, new SubProgressMonitor(monitor, 1));

			if (children == null)
				return;
			// copy children
			for (int i = 0; i < children.length; i++)
				children[i].copy(destination.getChild(children[i].getName(), new SubProgressMonitor(monitor, 1)), 
						options, new SubProgressMonitor(monitor, 1));
		} finally {
			monitor.done();
		}
	}

	/**
	 * Copies a file as specified by 
	 * {@link IFileStore#copy(IFileStore, int, IProgressMonitor)}.

	 * @param sourceInfo The current file information for the source of the move
	 * @param destination The destination of the copy.
	 * @param options bit-wise or of option flag constants (
	 * {@link EFS#OVERWRITE} or {@link EFS#SHALLOW}).
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if this method fails. Reasons include:
	 * <ul>
	 * <li> This store does not exist.</li>
	 * <li> The <code>OVERWRITE</code> flag is not specified and a file of the
	 * same name already exists at the copy destination.</li>
	 * </ul>
	 */
	protected void copyFile(IRemoteResourceInfo sourceInfo, IRemoteResource destination, int options, 
			IProgressMonitor monitor) throws CoreException {
		try {
			if ((options & EFS.OVERWRITE) == 0 && destination.fetchInfo().exists()) {
				throw new CoreException(new Status(IStatus.ERROR,
						PTPRemotePlugin.getDefault().getBundle().getSymbolicName(),
						"resource exists"));
			}
			long length = sourceInfo.getLength();
			int totalWork;
			if (length == -1)
				totalWork = IProgressMonitor.UNKNOWN;
			else
				totalWork = 1 + (int) (length / buffer.length);
			String sourcePath = toString();
			monitor.beginTask("copying", totalWork);  //$NON-NLS-1$
			InputStream in = null;
			OutputStream out = null;
			try {
				in = openInputStream(EFS.NONE, new SubProgressMonitor(monitor, 1));
				out = destination.openOutputStream(EFS.NONE, new SubProgressMonitor(monitor, 1));
				transferStreams(in, out, sourcePath, monitor);
				transferAttributes(sourceInfo, destination);
			} catch (CoreException e) {
				try {
					in.close();
					out.close();
				} catch (IOException e1) {
				}
				//if we failed to write, try to cleanup the half written file
				if (!destination.fetchInfo(0, null).exists()) {
					destination.delete(EFS.NONE, null);
				}
				throw e;
			}
		} finally {
			monitor.done();
		}
	}

	/**
	 * The default implementation of {@link IFileStore#delete(int, IProgressMonitor)}.
	 * This implementation always throws an exception indicating that deletion
	 * is not supported by this file system.  This method should be overridden
	 * for all file systems on which deletion is supported.
	 * 
	 * @param options bit-wise or of option flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 */
	public abstract void delete(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * This implementation of {@link Object#equals(Object)} defines
	 * equality based on the file store's URI.  Subclasses should override
	 * this method to return <code>true</code> if and only if the two file stores
	 * represent the same resource in the backing file system.  Issues to watch
	 * out for include whether the file system is case-sensitive, and whether trailing
	 * slashes are considered significant. Subclasses that override this method
	 * should also override {@link #hashCode()}.
	 * 
	 * @param obj The object to compare with the receiver for equality
	 * @return <code>true</code> if this object is equal to the provided object,
	 * and <code>false</code> otherwise.
	 * @since org.eclipse.core.filesystem 1.1
	 */
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!(obj instanceof FileStore))
			return false;
		return toURI().equals(((FileStore) obj).toURI());
	}

	/**
	 * The default implementation of {@link IFileStore#fetchInfo()}.
	 * This implementation forwards to {@link IFileStore#fetchInfo(int, IProgressMonitor)}.
	 * Subclasses may override this method.
	 */
	public IRemoteResourceInfo fetchInfo() throws CoreException {
		return fetchInfo(EFS.NONE, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract IRemoteResourceInfo fetchInfo(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getChild(java.lang.String)
	 */
	public abstract IRemoteResource getChild(String name, IProgressMonitor monitor);

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getName()
	 */
	public abstract String getName();

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#getParent()
	 */
	public abstract IRemoteResource getParent();

	/**
	 * This implementation of {@link Object#hashCode()} uses a definition
	 * of equality based on equality of the file store's URI.  Subclasses that
	 * override {@link #equals(Object)} should also override this method
	 * to ensure the contract of {@link Object#hashCode()} is honored.
	 * 
	 * @return A hash code value for this file store
	 * @since org.eclipse.core.filesystem 1.1
	 */
	public int hashCode() {
		return toURI().hashCode();
	}

	/**
	 * The default implementation of {@link IFileStore#isParentOf(IFileStore)}.
	 * This implementation performs parent calculation using other primitive methods. 
	 * Subclasses may override this method.
	 * 
	 * @param other The store to test for parentage.
	 * @return <code>true</code> if this store is a parent of the provided
	 * store, and <code>false</code> otherwise.
	 */
	public boolean isParentOf(IRemoteResource other) {
		while (true) {
			other = other.getParent();
			if (other == null)
				return false;
			if (this.equals(other))
				return true;
		}
	}

	/**
	 * The default implementation of {@link IFileStore#mkdir(int, IProgressMonitor)}.
	 * This implementation always throws an exception indicating that this file system 
	 * is read only. This method should be overridden for all writable file systems.
	 * 
	 * @param options bit-wise or of option flag constants
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 */
	public abstract IRemoteResource mkdir(int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * The default implementation of {@link IFileStore#move(IFileStore, int, IProgressMonitor)}.
	 * This implementation performs a move by using other primitive methods. 
	 * Subclasses may override this method.
	 */
	public void move(IRemoteResource destination, int options, IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("move", 100);
		copy(destination, options & EFS.OVERWRITE, new SubProgressMonitor(monitor, 70));
		delete(EFS.NONE, new SubProgressMonitor(monitor, 30));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#openInputStream(int, IProgressMonitor)
	 */
	public abstract InputStream openInputStream(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract OutputStream openOutputStream(int options, IProgressMonitor monitor) throws CoreException;

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteResource#putInfo(org.eclipse.ptp.remote.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public abstract void putInfo(IRemoteResourceInfo info, int options, IProgressMonitor monitor) throws CoreException;

	/**
	 * Default implementation of {@link IFileStore#toString()}. This default implementation
	 * returns a string equal to the one returned by #toURI().toString(). Subclasses
	 * may override to provide a more specific string representation of this store.
	 * 
	 * @return A string representation of this store.
	 */
	public String toString() {
		return toURI().toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.IFileStore#toURI()
	 */
	public abstract URI toURI();

	private void transferAttributes(IRemoteResourceInfo sourceInfo, IRemoteResource destination) throws CoreException {
		int options = EFS.SET_ATTRIBUTES | EFS.SET_LAST_MODIFIED;
		destination.putInfo(sourceInfo, options, null);
	}
}
