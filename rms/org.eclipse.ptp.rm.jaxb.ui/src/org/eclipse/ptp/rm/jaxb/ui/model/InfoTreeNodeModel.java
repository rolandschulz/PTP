package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class InfoTreeNodeModel implements IColumnViewerLabelSupport, IJAXBUINonNLSConstants {
	private final String id;
	private final String displayCol;
	private final ValueTreeNodeUpdateModel parent;

	public InfoTreeNodeModel(ValueTreeNodeUpdateModel parent, String id, boolean inValueCol) {
		this.parent = parent;
		this.id = id;
		if (inValueCol) {
			this.displayCol = COLUMN_VALUE;
		} else {
			this.displayCol = id;
		}
	}

	public Color getBackground(Object element, int columnIndex) {
		return parent.getBackground(element, columnIndex);
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	public String getDescription() {
		return parent.getDescription();
	}

	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (COLUMN_NAME.equals(columnName)) {
			displayValue = id;
		} else if (displayCol.equals(columnName)) {
			if (parent.isSelected()) {
				if (COLUMN_DESC.equals(id)) {
					displayValue = parent.getDescription();
				} else if (COLUMN_DEFAULT.equals(id)) {
					displayValue = parent.getDefault();
				} else if (COLUMN_TYPE.equals(id)) {
					displayValue = parent.getType();
				} else if (COLUMN_VALUE.equals(id)) {
					displayValue = parent.getValueAsString();
				} else if (COLUMN_STATUS.equals(id)) {
					displayValue = parent.getStatus();
				}
			}
		}
		if (displayValue == null) {
			return ZEROSTR;
		}
		return displayValue;
	}

	public Font getFont(Object element, int columnIndex) {
		return parent.getFont(element, columnIndex);
	}

	public Color getForeground(Object element, int columnIndex) {
		return parent.getForeground(element, columnIndex);
	}

	public ValueTreeNodeUpdateModel getParent() {
		return parent;
	}

	public String getTooltip() {
		String ttip = null;
		if (COLUMN_NAME.equals(id)) {
			ttip = parent.getTooltip();
		} else if (COLUMN_DESC.equals(id)) {
			ttip = parent.getDescription();
		}
		return ttip;
	}
}
