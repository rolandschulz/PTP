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


import org.eclipse.fdt.core.model.CModelException;
import org.eclipse.fdt.core.model.ICProject;
import org.eclipse.fdt.core.model.IIncludeReference;
import org.eclipse.fdt.internal.ui.FortranPluginImages;
import org.eclipse.fdt.ui.CElementGrouping;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * IncludeRefContainer
 */
public class IncludeRefContainer extends CElementGrouping {

	ICProject fCProject;

	/**
	 * 
	 */
	public IncludeRefContainer(ICProject cproject) {
		super(INCLUDE_REF_CONTAINER);
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
			IIncludeReference[] references = fCProject.getIncludeReferences();
			IncludeReferenceProxy[] proxies = new IncludeReferenceProxy[references.length];
			for (int i = 0; i < proxies.length; ++i) {
				proxies[i] = new IncludeReferenceProxy(this, references[i]);
			}
			return proxies;
		} catch (CModelException e) {
		}
		return NO_CHILDREN;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object object) {
		return FortranPluginImages.DESC_OBJS_INCLUDES_CONTAINER;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		return FortranViewMessages.getString("IncludeRefContainer.Includes");  //$NON-NLS-1$
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

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj instanceof IncludeRefContainer) {
			IncludeRefContainer other = (IncludeRefContainer)obj;
			return fCProject.equals(other.getCProject());
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		if (fCProject != null) {
			return fCProject.hashCode();
		}
		return super.hashCode();
	}

}
