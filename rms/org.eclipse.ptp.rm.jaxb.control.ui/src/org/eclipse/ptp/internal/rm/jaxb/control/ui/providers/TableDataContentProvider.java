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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.TableRowUpdateModel;

/**
 * For Attribute Table Viewer.
 * 
 * @author arossi
 * 
 */
public class TableDataContentProvider implements IStructuredContentProvider {

	public void dispose() {
	}

	/*
	 * TableRowUpdateModel serves as the data model for the viewer.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.IStructuredContentProvider#getElements(java
	 * .lang.Object)
	 */
	@SuppressWarnings("unchecked")
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof Collection<?>) {
			Collection<TableRowUpdateModel> list = (Collection<TableRowUpdateModel>) inputElement;
			return list.toArray();
		}
		return new Object[0];
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}
}
