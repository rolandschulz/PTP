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

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.swt.graphics.Image;

public class AttributeViewerRowData extends AttributeViewerCellData {

	public AttributeViewerRowData(Object data, List<ColumnData> columnData) {
		super(data, columnData);
	}

	public synchronized CellEditor getCellEditor(TableViewer viewer, ColumnDescriptor d) {
		if (editor == null) {
			createEditor(viewer.getTable(), d);
		}
		return editor;
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
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				displayValue = p.getDefault();
			} else if (COLUMN_VALUE.equals(columnName)) {
				displayValue = getActualValueAsString();
			}
		} else if (data instanceof Attribute) {
			Attribute ja = (Attribute) data;
			if (COLUMN_NAME.equals(columnName)) {
				displayValue = ja.getName();
			} else if (COLUMN_DESC.equals(columnName)) {
				displayValue = ja.getDescription();
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				displayValue = ja.getDefault();
			} else if (COLUMN_TYPE.equals(columnName)) {
				displayValue = ja.getType();
			} else if (COLUMN_STATUS.equals(columnName)) {
				displayValue = ja.getStatus();
			} else if (COLUMN_VALUE.equals(columnName)) {
				displayValue = getActualValueAsString();
			}
		}
		if (displayValue == null) {
			return ZEROSTR;
		}
		return displayValue;
	}
}
