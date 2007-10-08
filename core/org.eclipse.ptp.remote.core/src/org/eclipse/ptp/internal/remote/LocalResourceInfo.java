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

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.ptp.remote.AbstractRemoteResourceInfo;
import org.eclipse.ptp.remote.IRemoteResourceInfo;


public class LocalResourceInfo extends AbstractRemoteResourceInfo {
	private IFileInfo fileInfo;
	
	public LocalResourceInfo(IFileInfo fileInfo) {
		this.fileInfo = fileInfo;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#clone()
	 */
	@Override
	public Object clone() {
		return super.clone();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#compareTo(java.lang.Object)
	 */
	@Override
	public int compareTo(Object o) {
		return fileInfo.getName().compareTo(((IRemoteResourceInfo)o).getName());
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#exists()
	 */
	@Override
	public boolean exists() {
		return fileInfo.exists();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#getAttribute(int)
	 */
	@Override
	public boolean getAttribute(int attribute) {
		return fileInfo.getAttribute(attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#getLastModified()
	 */
	@Override
	public long getLastModified() {
		return fileInfo.getLastModified();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#getLength()
	 */
	@Override
	public long getLength() {
		return fileInfo.getLength();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#getName()
	 */
	@Override
	public String getName() {
		return fileInfo.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#getStringAttribute(int)
	 */
	@Override
	public String getStringAttribute(int attribute) {
		return fileInfo.getStringAttribute(attribute);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#isDirectory()
	 */
	@Override
	public boolean isDirectory() {
		return fileInfo.isDirectory();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#setAttribute(int, boolean)
	 */
	@Override
	public void setAttribute(int attribute, boolean value) {
		fileInfo.setAttribute(attribute, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.filesystem.provider.FileInfo#setLastModified(long)
	 */
	@Override
	public void setLastModified(long value) {
		fileInfo.setLastModified(value);
	}
}
