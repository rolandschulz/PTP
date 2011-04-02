package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.swt.graphics.Image;

public class AttributeViewerNodeData extends AttributeViewerCellData {

	private final List<AttributeViewerChildNodeData> children;

	public AttributeViewerNodeData(Object data, boolean inValueCol, List<ColumnData> columnData) {
		super(data, columnData);
		children = new ArrayList<AttributeViewerChildNodeData>();
		generateChildren(inValueCol);
	}

	public synchronized CellEditor getCellEditor(TreeViewer viewer, ColumnData d) {
		if (editor == null) {
			createEditor(viewer.getTree(), d);
		}
		return editor;
	}

	public List<AttributeViewerChildNodeData> getChildren() {
		return children;
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (data instanceof Property) {
			Property p = (Property) data;
			if (COLUMN_NAME.equals(columnName)) {
				displayValue = p.getName();
			} else if (COLUMN_VALUE.equals(columnName)) {
				displayValue = getActualValueAsString();
			}
		} else if (data instanceof Attribute) {
			Attribute ja = (Attribute) data;
			if (COLUMN_NAME.equals(columnName)) {
				displayValue = ja.getName();
			} else if (COLUMN_VALUE.equals(columnName)) {
				displayValue = getActualValueAsString();
			}
		}
		if (displayValue == null) {
			return ZEROSTR;
		}
		return displayValue;
	}

	private void generateChildren(boolean inValueCol) {
		children.add(new AttributeViewerChildNodeData(this, COLUMN_DEFAULT, inValueCol));
		if (data instanceof Attribute) {
			children.add(new AttributeViewerChildNodeData(this, COLUMN_TYPE, inValueCol));
			children.add(new AttributeViewerChildNodeData(this, COLUMN_STATUS, inValueCol));
			children.add(new AttributeViewerChildNodeData(this, COLUMN_DESC, inValueCol));
		}
	}
}
