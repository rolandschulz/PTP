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

import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerData;

public class TableDataContentProvider implements IStructuredContentProvider {

	private boolean selected = true;

	public void dispose() {
	}

	public Object[] getElements(Object inputElement) {
		if (selected) {
			return ((AttributeViewerData) inputElement).getSelectedRows().toArray();
		} else {
			return ((AttributeViewerData) inputElement).getAllRows().toArray();
		}
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void setSelectedOnly(boolean selected) {
		this.selected = selected;
	}
}
