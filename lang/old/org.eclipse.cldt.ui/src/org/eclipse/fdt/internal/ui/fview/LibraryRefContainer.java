/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.fdt.internal.ui.fview;

import java.util.ArrayList;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.ILibraryReference;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.ui.CElementGrouping;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * VirtualGrouping
 */
public class LibraryRefContainer extends CElementGrouping {

	private Object[] EMPTY = new Object[0];
	private ICProject fCProject;

	/**
	 * 
	 */
	public LibraryRefContainer(ICProject cproject) {
		super(LIBRARY_REF_CONTAINER);
		fCProject = cproject;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter == IWorkbenchAdapter.class) {
			return this;
		}
		if (adapter == ICProject.class) {
			return fCProject;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		try {
			ILibraryReference[] references = fCProject.getLibraryReferences();
			ArrayList list = new ArrayList(references.length);
			for (int i = 0; i < references.length; i++) {
				IPath path = references[i].getPath();
				IFile file = references[i].getCModel().getWorkspace().getRoot().getFileForLocation(path);
				if (file == null || !file.isAccessible()) {
					list.add(references[i]);
				}
			}
			return list.toArray();
		} catch (CModelException e) {
		}
		return EMPTY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return FortranPluginImages.DESC_OBJS_LIBRARY;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return FortranViewMessages.getString("LibraryRefContainer.Libraries"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return getCProject();
	}

	public ICProject getCProject() {
		return fCProject;
	}

}
