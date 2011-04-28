/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - modifications
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.providers;

import java.util.List;

import org.eclipse.jface.viewers.CellLabelProvider;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Cell label provider for the viewers allows for tooltip text display.
 * 
 * @author arossi
 * 
 */
public class ViewerDataCellLabelProvider extends CellLabelProvider implements IJAXBUINonNLSConstants {
	private final List<ColumnDataType> columnData;

	/**
	 * @param columnData
	 *            info on name, colors and font for a given column.
	 */
	public ViewerDataCellLabelProvider(List<ColumnDataType> columnData) {
		this.columnData = columnData;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#getToolTipDisplayDelayTime
	 * (java.lang.Object)
	 */
	@Override
	public int getToolTipDisplayDelayTime(Object object) {
		return 500;
	}

	/*
	 * Gets the tooltip related to underlying model type. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.ValueTreeNodeUpdateModel
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.InfoTreeNodeModel
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#getToolTipText(java.lang.
	 * Object)
	 */
	@Override
	public String getToolTipText(Object element) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getTooltip();
		}
		return super.getToolTipText(element);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#getToolTipTimeDisplayed(java
	 * .lang.Object)
	 */
	@Override
	public int getToolTipTimeDisplayed(Object object) {
		return 3000;
	}

	/*
	 * Provides cell text, colors and font based on its column index.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.CellLabelProvider#update(org.eclipse.jface.
	 * viewers.ViewerCell)
	 */
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

	/**
	 * Delegates to the
	 * {@link IColumnViewerLabelSupport#getBackground(Object, int)} method.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column background color, if any
	 */
	private Color getBackground(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getBackground(element, columnIndex);
		}
		return null;
	}

	/**
	 * Delegates to the {@link IColumnViewerLabelSupport#getColumnImage(String)}
	 * method.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column image, if any
	 */
	private Image getColumnImage(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getColumnImage(getColumnName(columnIndex));
		}
		return null;
	}

	/**
	 * Accesses name from the ColumnData element at given index.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column name
	 */
	private String getColumnName(int columnIndex) {
		if (columnIndex >= columnData.size()) {
			throw new ArrayIndexOutOfBoundsException(Messages.ViewerLabelProviderColumnError + columnIndex);
		}
		return columnData.get(columnIndex).getName();
	}

	/**
	 * Delegates to the
	 * {@link IColumnViewerLabelSupport#getDisplayValue(String)} method.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column text
	 */
	private String getColumnText(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getDisplayValue(getColumnName(columnIndex));
		}
		return ZEROSTR;
	}

	/**
	 * Delegates to the {@link IColumnViewerLabelSupport#getFont(Object, int)}
	 * method.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column font, if any
	 */
	private Font getFont(Object element, int columnIndex) {
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			return support.getFont(element, columnIndex);
		}
		return null;
	}

	/**
	 * Delegates to the
	 * {@link IColumnViewerLabelSupport#getForeground(Object, int)} method.
	 * 
	 * @param element
	 *            model object
	 * @param columnIndex
	 * @return column foreground color, if any
	 */
	private Color getForeground(Object element, int columnIndex) {
		Color color = null;
		if (element instanceof IColumnViewerLabelSupport) {
			IColumnViewerLabelSupport support = (IColumnViewerLabelSupport) element;
			color = support.getForeground(element, columnIndex);
		}
		return color;
	}
}
