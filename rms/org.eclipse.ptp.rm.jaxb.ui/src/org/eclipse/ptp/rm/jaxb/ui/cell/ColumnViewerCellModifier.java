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
package org.eclipse.ptp.rm.jaxb.ui.cell;

import java.util.List;

import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.ColumnDescriptor;
import org.eclipse.ptp.rm.jaxb.ui.data.RowData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.widgets.Item;

public class ColumnViewerCellModifier implements ICellModifier, IJAXBUINonNLSConstants {
	private final ColumnViewer viewer;
	private final List<ColumnDescriptor> columnDescriptors;

	public ColumnViewerCellModifier(ColumnViewer viewer, List<ColumnDescriptor> columnDescriptors) {
		this.viewer = viewer;
		this.columnDescriptors = columnDescriptors;
	}

	public boolean canModify(Object element, String property) {
		RowData row = (RowData) element;
		return row.canModify();
	}

	public Object getValue(Object element, String property) {
		String columnName = property;
		RowData row = (RowData) element;
		ColumnDescriptor columnDescriptor = getColumnDescriptor(columnName);

		if (columnDescriptor.isText()) {
			String displayText = row.getColumnDisplayValue(columnName);
			return displayText != null ? displayText : ZEROSTR;
		} else if (columnDescriptor.isCombo()) {
			return columnDescriptor.getOptionIndex(row.getColumnDisplayValue(columnName));
		} else if (columnDescriptor.isButton()) {
			String b = row.getColumnDisplayValue(columnName);
			return Boolean.parseBoolean(b);
		}

		throw new RuntimeException(Messages.UnsupportedColumnDescriptor + columnDescriptor.getType());
	}

	public void modify(Object element, String property, Object value) {
		if (element instanceof Item) {
			element = ((Item) element).getData();
		}

		String columnName = property;
		RowData row = (RowData) element;
		ColumnDescriptor columnDescriptor = getColumnDescriptor(columnName);

		if (columnDescriptor.isText()) {
			if (value != null && ((String) value).trim().length() <= 0) {
				value = null;
			}
			row.setColumnValue(columnName, (String) value);
		} else if (columnDescriptor.isCombo()) {
			row.setColumnValue(columnName, columnDescriptor.getOption((Integer) value));
		} else if (columnDescriptor.isButton()) {
			row.setColumnValue(columnName, (String) value);
		}

		/* update cell gui item */
		viewer.update(element, null);
	}

	private ColumnDescriptor getColumnDescriptor(String columnName) {
		for (ColumnDescriptor columnDescriptor : columnDescriptors) {
			if (columnDescriptor.getColumnName().equals(columnName)) {
				return columnDescriptor;
			}
		}
		throw new IllegalArgumentException(Messages.InvalidColumnName + columnName);
	}
}
