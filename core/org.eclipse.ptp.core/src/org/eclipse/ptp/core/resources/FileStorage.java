/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core.resources;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.eclipse.core.resources.IStorage;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;

/**
 * @author Clement chu
 * 
 */
public class FileStorage extends PlatformObject implements IStorage {
	IPath path;
	InputStream in = null;

	public FileStorage(IPath path){
		this.path = path;
	}
	public FileStorage(InputStream in, IPath path){
		this.path = path;
		this.in = in;
	}        
	public InputStream getContents() throws CoreException {
		if (in == null) {	
			try {
				return new FileInputStream(path.toFile());
			} catch (FileNotFoundException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.PLUGIN_ID, IStatus.ERROR, e.toString(), e));
			}
		}
		return in;
	}

	/**
	 * @see IStorage#getFullPath
	 */
	public IPath getFullPath() {
		return this.path;
	}

	/**
	 * @see IStorage#getName
	 */
	public String getName() {
		return this.path.lastSegment();
	}

	/**
	 * @see IStorage#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}
	/**
	 * @see IStorage#isReadOnly()
	 */
	public String toString() {
		return path.toOSString();
	}

	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj instanceof IStorage) {
			IPath p= getFullPath();
			IPath objPath= ((IStorage)obj).getFullPath();
			if (p != null && objPath != null)
				return p.equals(objPath);
		}
		return super.equals(obj);
	}
}
