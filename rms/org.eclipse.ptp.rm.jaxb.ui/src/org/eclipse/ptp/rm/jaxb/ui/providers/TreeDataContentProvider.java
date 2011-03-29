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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerData;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;

public class TreeDataContentProvider implements ITreeContentProvider {
	private boolean selected = true;

	public void dispose() {
	}

	public Object[] getChildren(Object parentElement) {
		List<Object> children = new ArrayList<Object>();
		AttributeViewerRowData rowData = (AttributeViewerRowData) parentElement;
		Object data = rowData.getData();
		if (data instanceof Property) {
			Property p = (Property) data;
			children.add(p.getName());
			children.add(p.getDefault());
			children.add(p.getValue());
		} else if (data instanceof Attribute) {
			Attribute ja = (Attribute) data;
			children.add(ja.getName());
			children.add(ja.getDefault());
			children.add(ja.getValue());
			children.add(ja.getType());
			children.add(ja.getDescription());
			children.add(ja.getTooltip());
			children.add(ja.getStatus());
		}
		return children.toArray();
	}

	public Object[] getElements(Object inputElement) {
		if (selected) {
			return ((AttributeViewerData) inputElement).getSelectedRows().toArray();
		} else {
			return ((AttributeViewerData) inputElement).getAllRows().toArray();
		}
	}

	public Object getParent(Object element) {
		return null;
	}

	public boolean hasChildren(Object element) {
		AttributeViewerRowData rowData = (AttributeViewerRowData) element;
		Object data = rowData.getData();
		return (data instanceof Property) || (data instanceof Attribute);
	}

	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	public void setSelectedOnly(boolean selected) {
		this.selected = selected;
	}
}
