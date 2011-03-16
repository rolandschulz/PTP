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

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class TreeDataLabelProvider implements IBaseLabelProvider {
	private final List<TreeColumnDescriptor> columnDescriptors;

	public TreeDataLabelProvider(List<TreeColumnDescriptor> columnDescriptors) {
		this.columnDescriptors = columnDescriptors;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		TreeRowData row = (TreeRowData) element;
		return row.getColumnImage(getColumnName(columnIndex));
	}

	public String getColumnText(Object element, int columnIndex) {
		TreeRowData row = (TreeRowData) element;

		String displayText = row.getColumnDisplayValue(getColumnName(columnIndex));
		return displayText != null ? displayText : "";
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	private String getColumnName(int columnIndex) {
		if (columnIndex >= columnDescriptors.size()) {
			throw new IllegalArgumentException("Only " + columnDescriptors.size() + " are supplied to " + getClass().getName()
					+ ". But LabelProvider is querying column: " + (columnIndex + 1));
		}

		return columnDescriptors.get(columnIndex).getColumnName();
	}
}
