/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;

public class TableRowUpdateModel extends CellEditorUpdateModel {

	public TableRowUpdateModel(String name, ValueUpdateHandler handler, CellEditor editor, String[] items, boolean readOnly) {
		super(name, handler, editor, items, readOnly, ZEROSTR, ZEROSTR, ZEROSTR);
	}

	public TableRowUpdateModel(String name, ValueUpdateHandler handler, CellEditor editor, String[] items, boolean readOnly,
			Attribute data) {
		super(name, handler, editor, items, readOnly, data.getTooltip(), data.getDescription(), data.getStatus());
	}

	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (COLUMN_NAME.equals(columnName)) {
			displayValue = name;
		} else if (selected) {
			if (COLUMN_DESC.equals(columnName)) {
				displayValue = description;
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				displayValue = defaultValue;
			} else if (COLUMN_TYPE.equals(columnName)) {
				displayValue = getType();
			} else if (COLUMN_VALUE.equals(columnName)) {
				displayValue = getValueAsString();
			} else if (COLUMN_STATUS.equals(columnName)) {
				displayValue = status;
			}
		}
		if (displayValue == null) {
			return ZEROSTR;
		}
		return displayValue;
	}
}
