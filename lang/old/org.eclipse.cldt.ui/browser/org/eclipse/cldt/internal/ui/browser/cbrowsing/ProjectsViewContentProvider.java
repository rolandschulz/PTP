/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.internal.ui.browser.cbrowsing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cldt.core.model.CModelException;
import org.eclipse.cldt.core.model.ICElement;
import org.eclipse.cldt.core.model.ICModel;
import org.eclipse.cldt.core.model.ICProject;
import org.eclipse.cldt.core.model.ISourceRoot;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

class ProjectsViewContentProvider extends CBrowsingContentProvider {

	ProjectsViewContentProvider(CBrowsingPart browsingPart) {
		super(browsingPart);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(Object element) {
		if (element == null || (element instanceof ICElement && !((ICElement)element).exists())) {
			return false;
		}

		try {
			startReadInDisplayThread();
			
			if (element instanceof ICModel) {
				ICModel cModel = (ICModel)element;
				return cModel.hasChildren();
			}
			
			if (element instanceof ICProject) {
				ICProject cProject = (ICProject)element;
				if (cProject.exists() && cProject.isOpen())
					return cProject.hasChildren();
				return false;
			}
			
			if (element instanceof ISourceRoot) {
				return false;
			}

			return false;
//		} catch (CModelException e) {
//			return false;
		} finally {
			finishedReadInDisplayThread();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object element) {
		if (element == null || (element instanceof ICElement && !((ICElement)element).exists())) {
			return INVALID_INPUT;
		}
		
		try {
			startReadInDisplayThread();
			
			if (element instanceof ICModel) {
				ICModel cModel = (ICModel)element;
				return cModel.getCProjects();
			}
			
			if (element instanceof ICProject) 
				return getSourceRoots((ICProject)element);

			if (element instanceof ISourceRoot) 
				return NO_CHILDREN;

			return INVALID_INPUT;
		} catch (CModelException e) {
			return ERROR_CANCELLED;
		} finally {
			finishedReadInDisplayThread();
		}
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element == null || (element instanceof ICElement && !((ICElement)element).exists())) {
			return null;
		}

		try {
			startReadInDisplayThread();
			
			if (element instanceof ICModel) {
				return null;
			}
			
			if (element instanceof ICProject) {
				ICProject cProject = (ICProject)element;
				return cProject.getCModel();
			}
			
			if (element instanceof ISourceRoot) {
				ISourceRoot cSourceRoot = (ISourceRoot)element;
				return cSourceRoot.getCProject();
			}

			return null;
//		} catch (CModelException e) {
//			return false;
		} finally {
			finishedReadInDisplayThread();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	protected Object[] getSourceRoots(ICProject project) throws CModelException {
		if (!project.getProject().isOpen())
			return NO_CHILDREN;
			
		ISourceRoot[] roots= project.getSourceRoots();
		List list= new ArrayList(roots.length);
		// filter out package fragments that correspond to projects and
		// replace them with the package fragments directly
		for (int i= 0; i < roots.length; i++) {
			ISourceRoot root= roots[i];
			if (!isProjectSourceRoot(root))
				list.add(root);
		}
		return list.toArray();
	}
	
	protected boolean isProjectSourceRoot(ISourceRoot root) {
		IResource resource= root.getResource();
		return (resource instanceof IProject);
	}
}
