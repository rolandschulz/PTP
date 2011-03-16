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
package org.eclipse.ptp.rm.jaxb.tests.viewers;

import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.TreeColumn;

/**
 * @See TreeViewerUtil.
 */
public class TreeViewerUtil {
	public static void setupTreeForEditing(TreeViewer treeViewer, List<TreeColumnDescriptor> columnDescriptors) {
		/* create table column items */
		for (TreeColumnDescriptor columnDescriptor : columnDescriptors) {
			TreeColumn columnItem = new TreeColumn(treeViewer.getTree(), SWT.LEFT);
			columnItem.setText(columnDescriptor.getColumnName());
			if (columnDescriptor.isWidthSpecified()) {
				columnItem.setWidth(columnDescriptor.getWidth());
			}
		}

		/* set column properties */
		String[] columnProperties = new String[columnDescriptors.size()];
		for (int i = 0; i < columnDescriptors.size(); i++) {
			TreeColumnDescriptor columnDescriptor = columnDescriptors.get(i);
			columnProperties[i] = columnDescriptor.getColumnName();
		}
		treeViewer.setColumnProperties(columnProperties);

		/* set column editors */
		CellEditor[] cellEditors = new CellEditor[columnDescriptors.size()];
		for (int i = 0; i < columnDescriptors.size(); i++) {
			TreeColumnDescriptor columnDescriptor = columnDescriptors.get(i);
			CellEditor cellEditor = null;
			if (columnDescriptor.isText()) {
				cellEditor = new TextCellEditor(treeViewer.getTree());
			} else if (columnDescriptor.isCombo()) {
				cellEditor = new ComboBoxCellEditor(treeViewer.getTree(), columnDescriptor.getOptions());
				/* TODO: Support Button */
			} else {
				throw new IllegalArgumentException("Column type: " + columnDescriptor.getType() + " is not supported");
			}

			cellEditors[i] = cellEditor;
		}
		treeViewer.setCellEditors(cellEditors);

		/* set cell modifier */
		treeViewer.setCellModifier(new TreeCellModifier(treeViewer, columnDescriptors));

		/* set controller */
		treeViewer.setContentProvider(new TreeDataContentProvider()); /* controller */
		treeViewer.setLabelProvider(new TreeDataLabelProvider(columnDescriptors)); /* controller */
	}
}

class TreeCellModifier implements ICellModifier {
	private final TreeViewer tableViewer;
	private final List<TreeColumnDescriptor> columnDescriptors;

	public TreeCellModifier(TreeViewer tableViewer, List<TreeColumnDescriptor> columnDescriptors) {
		this.tableViewer = tableViewer;
		this.columnDescriptors = columnDescriptors;
	}

	public boolean canModify(Object element, String property) {
		TreeRowData row = (TreeRowData) element;
		return row.canModify();
	}

	public Object getValue(Object element, String property) {
		String columnName = property;
		TreeRowData row = (TreeRowData) element;
		TreeColumnDescriptor columnDescriptor = getTreeColumnDescriptor(columnName);

		if (columnDescriptor.isText()) {
			String displayText = row.getColumnDisplayValue(columnName);
			return displayText != null ? displayText : "";
		} else if (columnDescriptor.isCombo()) {
			return columnDescriptor.getOptionIndex(row.getColumnDisplayValue(columnName));
			/* TODO: Support Button */
		}

		throw new RuntimeException("Unsupported columnDescriptor: " + columnDescriptor.getType());
	}

	public void modify(Object element, String property, Object value) {
		if (element instanceof Item) {
			element = ((Item) element).getData();
		}

		String columnName = property;
		TreeRowData row = (TreeRowData) element;
		TreeColumnDescriptor columnDescriptor = getTreeColumnDescriptor(columnName);

		if (columnDescriptor.isText()) {
			if (value != null && ((String) value).trim().length() <= 0) {
				value = null;
			}

			row.setColumnValue(columnName, (String) value);
		} else if (columnDescriptor.isCombo()) {
			row.setColumnValue(columnName, columnDescriptor.getOption((Integer) value));
			/* TODO: Support Button */
		}

		/* update cell gui item */
		tableViewer.update(element, null);
	}

	private TreeColumnDescriptor getTreeColumnDescriptor(String columnName) {
		for (TreeColumnDescriptor columnDescriptor : columnDescriptors) {
			if (columnDescriptor.getColumnName().equals(columnName)) {
				return columnDescriptor;
			}
		}
		throw new IllegalArgumentException("Column descriptor: " + columnName + " does not exist");
	}
}