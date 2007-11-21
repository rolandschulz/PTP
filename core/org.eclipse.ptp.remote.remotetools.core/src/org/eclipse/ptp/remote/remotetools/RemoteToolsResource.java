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
package org.eclipse.ptp.remote.remotetools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.core.filesystem.provider.FileInfo;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.remote.AbstractRemoteResource;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.ptp.remote.IRemoteResourceInfo;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteItem;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;


public class RemoteToolsResource extends AbstractRemoteResource {

	private IRemoteItem remoteItem;
	private URI remoteURI;
	private RemoteToolsFileManager fileMgr;
	private boolean isDirectory;
	
	public RemoteToolsResource(RemoteToolsConnection conn, RemoteToolsFileManager mgr, IRemoteItem remoteItem, boolean isDirectory) {
		this.fileMgr = mgr;
		this.remoteItem = remoteItem;
		this.isDirectory = isDirectory;
		try {
			this.remoteURI = new URI("remotetools", conn.getHostname(), remoteItem.getPath(), null);
		} catch (URISyntaxException e) {
			throw new RuntimeException(e);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		IRemoteItem[] items;
		try {
			items = fileMgr.listItems(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					e.getMessage()));
		}
		
		String[] names = new String[items.length];
		                
		for (int i = 0; i < items.length; i++)
		{
			IPath path = new Path(items[i].getPath());
			names[i] = path.lastSegment();
		}
		
		return names;	
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			fileMgr.delete(remoteItem.getPath());
		}
		catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not delete file", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResourceInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		FileInfo info = new FileInfo(new Path(remoteItem.getPath()).lastSegment());
		if (remoteItem == null || !remoteItem.exists()) {
			info.setExists(false);
			return new RemoteToolsResourceInfo(info);
		}
		
		info.setExists(true);
		info.setLastModified(remoteItem.getModificationTime());
		info.setDirectory(isDirectory);
		info.setAttribute(IRemoteResource.ATTRIBUTE_READ_ONLY, !remoteItem.isWritable());
		
		if (!isDirectory) {
			IRemoteFile file = (IRemoteFile)remoteItem;
			info.setAttribute(IRemoteResource.ATTRIBUTE_EXECUTABLE, file.isExecutable());
			info.setLength(file.getSize());
		}

		return new RemoteToolsResourceInfo(info);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getChild(java.lang.String)
	 * 
	 * FIXME: Should throw CoreException
	 */
	@Override
	public IRemoteResource getChild(String name, IProgressMonitor monitor) {
		IPath childPath = new Path(remoteItem.getPath()).append(name);
		
		IRemoteResource resource = fileMgr.lookup(childPath);
		if (resource != null) {
			return resource;
		}
		
		try {
			return fileMgr.getResource(childPath, monitor);
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getName()
	 */
	@Override
	public String getName() {
		return new Path(remoteItem.getPath()).lastSegment();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getParent()
	 * 
	 * FIXME: should take a progress monitor as argument
	 * FIXME: should throw a core exception
	 */
	@Override
	public IRemoteResource getParent() {
		IPath parent;
		try {
			parent = new Path(fileMgr.getParent(remoteItem.getPath()));
		} catch (RemoteConnectionException e1) {
			return null;
		}
		
		IRemoteResource resource = fileMgr.lookup(parent);
		if (resource != null) {
			return resource;
		}
		try {
			return fileMgr.getResource(parent, new NullProgressMonitor());
		} catch (IOException e) {
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResource mkdir(int options, IProgressMonitor monitor)
			throws CoreException {
		try {
			fileMgr.mkdir(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(), 
					"The directory could not be created", e));
		}
			
		return this;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		if (isDirectory) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Is a directory"));
		}
		try {
			return fileMgr.openInputStream(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not get input stream", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {
			
		if (isDirectory) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Is a directory"));
		}
		try {
			return fileMgr.openOutputStream(remoteItem.getPath());
		} catch (Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					"Could not open output stream", e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#putInfo(org.eclipse.ptp.remote.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IRemoteResourceInfo info, int options,
			IProgressMonitor monitor) throws CoreException {
		try {
			boolean modified = false;
			if ((options & IRemoteResource.SET_ATTRIBUTES) != 0) {
				//We cannot currently write isExecutable(), isHidden()
				remoteItem.setWriteable(!info.getAttribute(IRemoteResource.ATTRIBUTE_READ_ONLY));
				modified = true;
			} 
			if ((options & IRemoteResource.SET_LAST_MODIFIED) != 0) {
				remoteItem.setModificationTime(info.getLastModified());
				modified = true;
			}
			if (modified) {
				remoteItem.commitAttributes();
			}
		} catch(Exception e) {
			throw new CoreException(new Status(IStatus.ERROR,
					Activator.getDefault().getBundle().getSymbolicName(),
					e.getMessage(), e));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#toURI()
	 */
	@Override
	public URI toURI() {
		return remoteURI;
	}
}
