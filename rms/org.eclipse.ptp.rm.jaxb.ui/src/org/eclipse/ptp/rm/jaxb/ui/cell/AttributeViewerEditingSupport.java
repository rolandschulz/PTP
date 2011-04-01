/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.cell;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;
import org.eclipse.ptp.rm.jaxb.ui.data.ColumnDescriptor;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;

public class AttributeViewerEditingSupport extends EditingSupport {

	private final ColumnViewer viewer;
	private final ColumnDescriptor descriptor;

	public AttributeViewerEditingSupport(ColumnViewer viewer, ColumnDescriptor descriptor) {
		super(viewer);
		this.viewer = viewer;
		this.descriptor = descriptor;
	}

	@Override
	protected boolean canEdit(Object element) {
		return ((AttributeViewerRowData) element).canEdit();
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (viewer instanceof TableViewer) {
			return ((AttributeViewerRowData) element).getCellEditor((TableViewer) viewer, descriptor);
		} else {
			return ((AttributeViewerRowData) element).getCellEditor((TreeViewer) viewer, descriptor);
		}
	}

	@Override
	protected Object getValue(Object element) {
		Object o = ((AttributeViewerRowData) element).getValue();
		return o;
	}

	@Override
	protected void setValue(Object element, Object value) {
		((AttributeViewerRowData) element).setValue(value);
		WidgetActionUtils.refreshViewer(viewer);
	}
}
