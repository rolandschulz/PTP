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

import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableFontProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class ViewerDataLabelProvider implements ITableLabelProvider, ITableColorProvider, ITableFontProvider,
		IJAXBUINonNLSConstants {
	private final List<ColumnData> columnData;

	public ViewerDataLabelProvider(List<ColumnData> columnData) {
		this.columnData = columnData;
	}

	public void addListener(ILabelProviderListener listener) {
	}

	public void dispose() {
	}

	public Color getBackground(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getBackground(element, columnIndex);
		}
		return null;
	}

	public Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getColumnImage(getColumnName(columnIndex));
		}
		return null;
	}

	public String getColumnText(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getDisplayValue(getColumnName(columnIndex));
		}
		return ZEROSTR;
	}

	public Font getFont(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getFont(element, columnIndex);
		}
		return null;
	}

	public Color getForeground(Object element, int columnIndex) {
		Color color = null;
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			color = support.getForeground(element, columnIndex);
		}
		return color;
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
	}

	private String getColumnName(int columnIndex) {
		if (columnIndex >= columnData.size()) {
			throw new ArrayIndexOutOfBoundsException(Messages.ViewerLabelProviderColumnError + columnIndex);
		}
		return columnData.get(columnIndex).getName();
	}
}
