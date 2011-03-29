package org.eclipse.ptp.rm.jaxb.ui.cell;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.EditingSupport;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;

public class AttributeViewerEditingSupport extends EditingSupport {

	private ColumnViewer viewer;

	public AttributeViewerEditingSupport(ColumnViewer viewer) {
		super(viewer);
	}

	@Override
	protected boolean canEdit(Object element) {
		return ((AttributeViewerRowData) element).canEdit();
	}

	@Override
	protected CellEditor getCellEditor(Object element) {
		return ((AttributeViewerRowData) element).getCellEditor(viewer);
	}

	@Override
	protected Object getValue(Object element) {
		return ((AttributeViewerRowData) element).getValue();
	}

	@Override
	protected void setValue(Object element, Object value) {
		((AttributeViewerRowData) element).setValue(value);
		viewer.refresh();
	}
}
