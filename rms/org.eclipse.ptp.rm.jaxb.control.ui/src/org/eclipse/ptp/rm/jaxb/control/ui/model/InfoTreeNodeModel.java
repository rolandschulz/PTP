/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.model;

import org.eclipse.ptp.rm.jaxb.control.ui.IColumnViewerLabelSupport;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * The only model without a widget or editor. Used to display information nodes
 * in a Tree Viewer.
 * 
 * @author arossi
 * 
 */
public class InfoTreeNodeModel implements IColumnViewerLabelSupport {
	private final String id;
	private final String displayCol;
	private final ValueTreeNodeUpdateModel parent;

	/**
	 * @param parent
	 *            the main (editable) node entry for the attribute
	 * @param id
	 *            what field of the data object this node represents
	 * @param inValueCol
	 *            whether to display the field value in the Value column of the
	 *            viewer. (<code>false</code> means that its value displays in a
	 *            column whose name matches the id.
	 */
	public InfoTreeNodeModel(ValueTreeNodeUpdateModel parent, String id, boolean inValueCol) {
		this.parent = parent;
		this.id = id;
		if (inValueCol) {
			this.displayCol = JAXBControlUIConstants.COLUMN_VALUE;
		} else {
			this.displayCol = id;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang
	 * .Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		return parent.getBackground(element, columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport#getColumnImage(java
	 * .lang.String)
	 */
	public Image getColumnImage(String columnName) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport#getDescription()
	 */
	public String getDescription() {
		return parent.getDescription();
	}

	/*
	 * Display name of this field (=id) in the name column, and its value either
	 * in the value column or in the column which matches its id. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport#getDisplayValue(
	 * java.lang.String)
	 */
	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (JAXBControlUIConstants.COLUMN_NAME.equals(columnName)) {
			displayValue = id;
		} else if (displayCol.equals(columnName)) {
			if (JAXBControlUIConstants.COLUMN_DESC.equals(id)) {
				displayValue = parent.getDescription();
			} else if (JAXBControlUIConstants.COLUMN_DEFAULT.equals(id)) {
				displayValue = parent.getDefault();
			} else if (JAXBControlUIConstants.COLUMN_TYPE.equals(id)) {
				displayValue = parent.getType();
			} else if (JAXBControlUIConstants.COLUMN_VALUE.equals(id)) {
				displayValue = parent.getValueAsString();
			} else if (JAXBControlUIConstants.COLUMN_STATUS.equals(id)) {
				displayValue = parent.getStatus();
			}
		}
		if (displayValue == null) {
			return JAXBControlUIConstants.ZEROSTR;
		}
		return displayValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object,
	 * int)
	 */
	public Font getFont(Object element, int columnIndex) {
		return parent.getFont(element, columnIndex);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableColorProvider#getForeground(java.lang
	 * .Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		return parent.getForeground(element, columnIndex);
	}

	/**
	 * @return the main (editable) node entry for the Property or Attribute
	 */
	public ValueTreeNodeUpdateModel getParent() {
		return parent;
	}

	/*
	 * Display the tooltip only on the name node; if this is the description
	 * node, display that. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport#getTooltip()
	 */
	public String getTooltip() {
		String ttip = null;
		if (JAXBControlUIConstants.COLUMN_NAME.equals(id)) {
			ttip = parent.getTooltip();
		} else if (JAXBControlUIConstants.COLUMN_DESC.equals(id)) {
			ttip = parent.getDescription();
		}
		return ttip;
	}
}
