/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.providers;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel;

/**
 * For Attribute Tree Viewer.
 * 
 * @author arossi
 * 
 */
public class TreeDataContentProvider implements ITreeContentProvider {
	public void dispose() {
		// Nothing required
	}

	/*
	 * Only Value nodes have children. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.
	 * Object)
	 */
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ValueTreeNodeUpdateModel) {
			return ((ValueTreeNodeUpdateModel) parentElement).getChildren().toArray();
		}
		return new Object[0];
	}

	/*
	 * ValueTreeNodeUpdateModel serves as the main data model for the viewer,
	 * with its children being InfoTreeNodeModel objects. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Collection<ValueTreeNodeUpdateModel> list = (Collection<ValueTreeNodeUpdateModel>) inputElement;
			return list.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	/*
	 * Only Value nodes have children. (non-Javadoc) (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.
	 * Object)
	 */
	public boolean hasChildren(Object element) {
		return (element instanceof ValueTreeNodeUpdateModel);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Do nothing
	}
}
