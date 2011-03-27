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
package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetActionUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;

public class RowData implements IJAXBUINonNLSConstants {

	private final Object data;
	private final Control valueControl;

	public RowData(Object data, Control valueControl) {
		this.data = data;
		this.valueControl = valueControl;
	}

	public boolean canModify() {
		return true;
	}

	public String getColumnDisplayValue(String columnName) {

		Object value = null;

		if (data instanceof Property) {
			Property p = (Property) data;
			if (COLUMN_NAME.equals(columnName)) {
				value = p.getName();
			} else if (COLUMN_VALUE.equals(columnName)) {
				value = WidgetActionUtils.getValueString(valueControl);
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				value = p.getDefault();
			}
		} else if (data instanceof Attribute) {
			Attribute ja = (Attribute) data;
			if (COLUMN_NAME.equals(columnName)) {
				value = ja.getName();
			} else if (COLUMN_DESC.equals(columnName)) {
				value = ja.getDescription();
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				value = ja.getDefault();
			} else if (COLUMN_TYPE.equals(columnName)) {
				value = ja.getType();
			} else if (COLUMN_TOOLTIP.equals(columnName)) {
				value = ja.getTooltip();
			} else if (COLUMN_STATUS.equals(columnName)) {
				value = ja.getStatus();
			} else if (COLUMN_VALUE.equals(columnName)) {
				value = WidgetActionUtils.getValueString(valueControl);
			}
		}

		if (value == null) {
			return ZEROSTR;
		}
		return value.toString();

	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	public Object getData() {
		return data;
	}

	public String getReplaced(String pattern) {
		String name = getColumnDisplayValue(COLUMN_NAME);
		String value = getColumnDisplayValue(COLUMN_VALUE);
		String result = new String(pattern);
		if (name != null) {
			result = result.replaceAll(NAME_TAG, name);
		}
		if (value != null) {
			result = result.replaceAll(VALUE_TAG, name);
		}
		return result;
	}

	public void setColumnValue(String columnName, String displayValue) {
		if (COLUMN_VALUE.equals(columnName)) {
			WidgetActionUtils.setValue(valueControl, displayValue);
		}
	}
}
