/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 *  M Venkataramana - original code: http://eclipse.dzone.com/users/venkat_r_m
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.Collection;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.ValueTreeNodeUpdateModel;

public class TreeDataContentProvider implements ITreeContentProvider {
	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ValueTreeNodeUpdateModel) {
			return ((ValueTreeNodeUpdateModel) parentElement).getChildren().toArray();
		}
		return new Object[0];
	}

	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Collection<ICellEditorUpdateModel> list = (Collection<ICellEditorUpdateModel>) inputElement;
			return list.toArray();
		}
		return new Object[0];
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		return (element instanceof ValueTreeNodeUpdateModel);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
