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
package org.eclipse.ptp.internal.remote;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.AbstractRemoteResource;
import org.eclipse.ptp.remote.IRemoteResource;
import org.eclipse.ptp.remote.IRemoteResourceInfo;


public class LocalResource extends AbstractRemoteResource {

	private IFileStore filestore;
	
	public LocalResource(IFileStore filestore) {
		this.filestore = filestore;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#childNames(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public String[] childNames(int options, IProgressMonitor monitor)
			throws CoreException {
		return filestore.childNames(options, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#delete(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void delete(int options, IProgressMonitor monitor)
			throws CoreException {
		filestore.delete(options, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#fetchInfo(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResourceInfo fetchInfo(int options, IProgressMonitor monitor)
			throws CoreException {
		return new LocalResourceInfo(filestore.fetchInfo(options, monitor));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getChild(java.lang.String)
	 */
	@Override
	public IRemoteResource getChild(String name, IProgressMonitor monitor) {
		try {
			return new LocalResource(filestore.getChild(name));
		} finally {
			monitor.done();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getName()
	 */
	@Override
	public String getName() {
		return filestore.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#getParent()
	 */
	@Override
	public IRemoteResource getParent() {
		return new LocalResource(filestore.getParent());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#mkdir(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public IRemoteResource mkdir(int options, IProgressMonitor monitor)
			throws CoreException {
		return new LocalResource(filestore.mkdir(options, monitor));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openInputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public InputStream openInputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return filestore.openInputStream(options, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#openOutputStream(int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public OutputStream openOutputStream(int options, IProgressMonitor monitor)
			throws CoreException {
		return filestore.openOutputStream(options, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#putInfo(org.eclipse.ptp.remote.IRemoteResourceInfo, int, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	public void putInfo(IRemoteResourceInfo info, int options,
			IProgressMonitor monitor) throws CoreException {
		filestore.putInfo(info, options, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.AbstractRemoteResource#toURI()
	 */
	@Override
	public URI toURI() {
		return filestore.toURI();
	}
}
