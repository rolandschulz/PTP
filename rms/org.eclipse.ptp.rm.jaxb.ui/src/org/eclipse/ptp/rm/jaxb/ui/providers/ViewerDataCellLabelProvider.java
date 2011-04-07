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

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

public class ViewerDataCellLabelProvider extends CellLabelProvider implements IJAXBUINonNLSConstants {
	private final List<ColumnData> columnData;

	public ViewerDataCellLabelProvider(List<ColumnData> columnData) {
		this.columnData = columnData;
	}

	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 1000;
	}

	@Override
	public String getToolTipText(Object element) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getTooltip();
		}
		return super.getToolTipText(element);
	}

	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 5000;
	}

	@Override
	public void update(ViewerCell cell) {
		int index = cell.getColumnIndex();
		Object element = cell.getElement();
		Color color = getBackground(element, index);
		if (color != null) {
			cell.setBackground(color);
		}
		color = getForeground(element, index);
		if (color != null) {
			cell.setBackground(color);
		}
		Font font = getFont(element, index);
		if (font != null) {
			cell.setFont(font);
		}
		Image img = getColumnImage(element, index);
		if (img != null) {
			cell.setImage(img);
		}
		cell.setText(getColumnText(element, index));
	}

	private Color getBackground(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getBackground(element, columnIndex);
		}
		return null;
	}

	private Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getColumnImage(getColumnName(columnIndex));
		}
		return null;
	}

	private String getColumnName(int columnIndex) {
		if (columnIndex >= columnData.size()) {
			throw new ArrayIndexOutOfBoundsException(Messages.ViewerLabelProviderColumnError + columnIndex);
		}
		return columnData.get(columnIndex).getName();
	}

	private String getColumnText(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getDisplayValue(getColumnName(columnIndex));
		}
		return ZEROSTR;
	}

	private Font getFont(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getFont(element, columnIndex);
		}
		return null;
	}

	private Color getForeground(Object element, int columnIndex) {
		Color color = null;
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			color = support.getForeground(element, columnIndex);
		}
		return color;
	}
}
