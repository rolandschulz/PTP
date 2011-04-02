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
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerCellData;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerNodeData;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;

public class AttributeViewerEditingSupport extends EditingSupport {

	private final ColumnViewer viewer;
	private final ColumnData data;

	public AttributeViewerEditingSupport(ColumnViewer viewer, ColumnData data) {
		super(viewer);
		this.viewer = viewer;
		this.data = data;
	}

	@Override
	protected boolean canEdit(Object element) {
		if (element instanceof AttributeViewerCellData) {
			return ((AttributeViewerCellData) element).canEdit();
		}
		return false;
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof AttributeViewerRowData) {
			return ((AttributeViewerRowData) element).getCellEditor((TableViewer) viewer, data);
		} else if (element instanceof AttributeViewerNodeData) {
			return ((AttributeViewerNodeData) element).getCellEditor((TreeViewer) viewer, data);
		}
		return null;
	}

	@Override
	protected Object getValue(Object element) {
		if (element instanceof AttributeViewerCellData) {
			return ((AttributeViewerCellData) element).getValueForEditor();
		}
		return null;
	}

	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof AttributeViewerCellData) {
			((AttributeViewerCellData) element).setValueFromEditor(value);
			WidgetActionUtils.refreshViewer(viewer);
		}
	}
}
