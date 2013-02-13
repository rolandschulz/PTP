/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.cell;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;

/**
 * Editing support for the checkbox attribute viewers.
 * 
 * @author arossi
 * 
 */
public class AttributeViewerEditingSupport extends EditingSupport {

	private final ColumnViewer viewer;

	/**
	 * @param viewer
	 * @see org.eclipse.jface.viewers.CheckboxTableViewer
	 * @see org.eclipse.jface.viewers.CheckboxTreeViewer
	 */
	public AttributeViewerEditingSupport(ColumnViewer viewer) {
		super(viewer);
		this.viewer = viewer;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#canEdit(java.lang.Object)
	 */
	@Override
	protected boolean canEdit(Object element) {
		if (element instanceof ICellEditorUpdateModel) {
			return ((ICellEditorUpdateModel) element).canEdit();
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.EditingSupport#getCellEditor(java.lang.Object)
	 */
	@Override
	protected CellEditor getCellEditor(Object element) {
		if (element instanceof ICellEditorUpdateModel) {
			return ((ICellEditorUpdateModel) element).getCellEditor();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#getValue(java.lang.Object)
	 */
	@Override
	protected Object getValue(Object element) {
		if (element instanceof ICellEditorUpdateModel) {
			return ((ICellEditorUpdateModel) element).getValueForEditor();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.EditingSupport#setValue(java.lang.Object,
	 * java.lang.Object)
	 */
	@Override
	protected void setValue(Object element, Object value) {
		if (element instanceof ICellEditorUpdateModel) {
			ICellEditorUpdateModel model = (ICellEditorUpdateModel) element;
			model.setValueFromEditor(value);
			viewer.refresh();
		}
	}
}
