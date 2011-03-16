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
package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.ColumnDescriptor;
import org.eclipse.ptp.rm.jaxb.ui.data.RowData;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.graphics.Image;

public class TreeDataLabelProvider implements IBaseLabelProvider, IJAXBUINonNLSConstants {
	private final List<ColumnDescriptor> columnDescriptors;

	public TreeDataLabelProvider(List<ColumnDescriptor> columnDescriptors) {
		this.columnDescriptors = columnDescriptors;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Image getColumnImage(Object element, int columnIndex) {
		RowData row = (RowData) element;
		return row.getColumnImage(getColumnName(columnIndex));
	}

	public String getColumnText(Object element, int columnIndex) {
		RowData row = (RowData) element;
		String displayText = row.getColumnDisplayValue(getColumnName(columnIndex));
		return displayText != null ? displayText : ZEROSTR;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	private String getColumnName(int columnIndex) {
		if (columnIndex >= columnDescriptors.size()) {
			throw new ArrayIndexOutOfBoundsException(Messages.TreeDataLabelProviderColumnError + columnIndex);
		}

		return columnDescriptors.get(columnIndex).getColumnName();
	}
}
