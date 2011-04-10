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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;

public class ValueTreeNodeUpdateModel extends CellEditorUpdateModel {

	private final List<InfoTreeNodeModel> children;

	public ValueTreeNodeUpdateModel(String name, ValueUpdateHandler handler, CellEditor editor, String[] items, boolean readOnly,
			boolean inValueCol) {
		super(name, handler, editor, items, readOnly, ZEROSTR, ZEROSTR, ZEROSTR);
		children = new ArrayList<InfoTreeNodeModel>();
		generateChildren(inValueCol);
	}

	public ValueTreeNodeUpdateModel(String name, ValueUpdateHandler handler, CellEditor editor, String[] items, boolean readOnly,
			boolean inValueCol, Attribute data) {
		super(name, handler, editor, items, readOnly, data.getTooltip(), data.getDescription(), data.getStatus());
		children = new ArrayList<InfoTreeNodeModel>();
		generateChildren(inValueCol);
	}

	public List<InfoTreeNodeModel> getChildren() {
		return children;
	}

	public String getDefault() {
		return defaultValue;
	}

	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (COLUMN_NAME.equals(columnName)) {
			displayValue = name;
		} else if (selected && COLUMN_VALUE.equals(columnName)) {
			displayValue = getValueAsString();
		}
		if (displayValue == null) {
			return ZEROSTR;
		}
		return displayValue;
	}

	public String getStatus() {
		return status;
	}

	private void generateChildren(boolean inValueCol) {
		children.add(new InfoTreeNodeModel(this, COLUMN_DEFAULT, inValueCol));
		children.add(new InfoTreeNodeModel(this, COLUMN_TYPE, inValueCol));
		children.add(new InfoTreeNodeModel(this, COLUMN_STATUS, inValueCol));
		children.add(new InfoTreeNodeModel(this, COLUMN_DESC, inValueCol));
	}
}
