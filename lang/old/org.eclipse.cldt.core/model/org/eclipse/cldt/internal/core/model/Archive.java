/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.core.model;

 
import java.util.Map;

import org.eclipse.cldt.core.IBinaryParser.IBinaryArchive;
import org.eclipse.cldt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.IArchive;
import org.eclipse.cldt.core.model.IBinary;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ICProject;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class Archive extends Openable implements IArchive {

	IBinaryArchive binaryArchive;

	public Archive(ICElement parent, IFile file, IBinaryArchive ar) {
		this(parent, file.getLocation(), ar);
	}

	public Archive(ICElement parent, IPath path, IBinaryArchive ar) {
		super (parent, path, ICElement.C_ARCHIVE);
		binaryArchive = ar;
	}

	public IBinary[] getBinaries() throws CModelException {
		ICElement[] e = getChildren();
		IBinary[] b = new IBinary[e.length];
		System.arraycopy(e, 0, b, 0, e.length);
		return b;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#isReadOnly()
	 */
	public boolean isReadOnly() {
		return true;
	}

	public CElementInfo createElementInfo() {
		return new ArchiveInfo(this);
	}

	protected ArchiveInfo getArchiveInfo() throws CModelException {
		return (ArchiveInfo)getElementInfo();
	}

	protected boolean buildStructure(OpenableInfo info, IProgressMonitor pm, Map newElements, IResource underlyingResource)
		throws CModelException {
		return computeChildren(info, underlyingResource);
	}

	public boolean computeChildren(OpenableInfo info, IResource res) {
		IBinaryArchive ar = getBinaryArchive();
		if (ar != null) {
			IBinaryObject[] objects = ar.getObjects();
			for (int i = 0; i < objects.length; i++) {
				final IBinaryObject obj = objects[i];
				Binary binary = new Binary(this, ar.getPath().append(obj.getName()), obj);
				info.addChild(binary);
			}
		} else {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (IBinaryArchive.class.equals(adapter)) {
			return getBinaryArchive();
		}
		return super.getAdapter(adapter);
	}

	IBinaryArchive getBinaryArchive() {
		return binaryArchive;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.model.ICElement#exists()
	 */
	public boolean exists() {
		return getResource() != null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.model.CElement#closing(java.lang.Object)
	 */
	protected void closing(Object info) throws CModelException {
		ICProject cproject = getCProject();
		CProjectInfo pinfo = (CProjectInfo)CModelManager.getDefault().peekAtInfo(cproject);
		if (pinfo != null && pinfo.vLib != null) {
			pinfo.vLib.removeChild(this);
		}
		super.closing(info);
	}

}
