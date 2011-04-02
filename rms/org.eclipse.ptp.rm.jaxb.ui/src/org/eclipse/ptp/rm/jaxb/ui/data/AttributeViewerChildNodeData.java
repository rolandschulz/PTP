package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IAttributeViewerColumnLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.graphics.Image;

public class AttributeViewerChildNodeData implements IAttributeViewerColumnLabelSupport, IJAXBUINonNLSConstants {

	private final AttributeViewerNodeData parent;
	private final String id;
	private final String displayCol;

	public AttributeViewerChildNodeData(AttributeViewerNodeData parent, String id, boolean inValueCol) {
		this.parent = parent;
		this.id = id;
		if (inValueCol) {
			this.displayCol = COLUMN_VALUE;
		} else {
			this.displayCol = id;
		}
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	public String getDisplayValue(String columnName) {
		if (COLUMN_NAME.equals(columnName)) {
			return id;
		} else if (displayCol.equals(columnName)) {
			Object data = parent.getData();
			if (data instanceof Property) {
				Property p = (Property) data;
				if (COLUMN_DEFAULT.equals(id)) {
					return p.getDefault();
				}
			} else if (data instanceof Attribute) {
				Attribute ja = (Attribute) data;
				if (COLUMN_DESC.equals(id)) {
					return ja.getDescription();
				} else if (COLUMN_DEFAULT.equals(id)) {
					return ja.getDefault();
				} else if (COLUMN_TYPE.equals(id)) {
					return ja.getType();
				} else if (COLUMN_STATUS.equals(id)) {
					return ja.getStatus();
				}
			}
		}
		return ZEROSTR;
	}

	public AttributeViewerNodeData getParent() {
		return parent;
	}
}
