/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;

/**
 * Cell editor for the (editable) value nodes of a Tree Viewer.
 * 
 * @author arossi
 * 
 */
public class ValueTreeNodeUpdateModel extends CellEditorUpdateModel {

	private final List<InfoTreeNodeModel> children;

	/**
	 * Cell editor model for Tree node with underlying attribute data.
	 * 
	 * @param name
	 *            of the model, which will correspond to the name of an attribute the value is to be saved to
	 * @param linkUpdateTo
	 *            if a change in this property or attribute value overwrites other property or attribute values
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param editor
	 *            the cell editor for the value cell
	 * @param items
	 *            if this is a combo editor, the selection items
	 * @param itemsFrom
	 *            if this is a combo editor, the property or attribute value to get the items from
	 * @param translateBooleanAs
	 *            if this is a checkbox, use these string values for T/F
	 * @param readOnly
	 *            whether it is editable
	 * @param inValueCol
	 *            whether to display the field value in the Value column of the viewer. (<code>false</code> means that its value
	 *            displays in a column whose name matches the id.
	 */
	public ValueTreeNodeUpdateModel(String name, IUpdateHandler handler, CellEditor editor, String[] items, String itemsFrom,
			String translateBooleanAs, boolean readOnly, boolean inValueCol) {
		super(name, handler, editor, items, translateBooleanAs, readOnly, JAXBControlUIConstants.ZEROSTR,
				JAXBControlUIConstants.ZEROSTR, JAXBControlUIConstants.ZEROSTR, itemsFrom);
		children = new ArrayList<InfoTreeNodeModel>();
		generateChildren(inValueCol);
	}

	/**
	 * Cell editor model for Tree node with underlying Attribute data.
	 * 
	 * @param name
	 *            of the model, which will correspond to the name of an attribute the value is to be saved to
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param editor
	 *            the cell editor for the value cell
	 * @param items
	 *            if this is a combo editor, the selection items
	 * @param itemsFrom
	 *            if this is a combo editor, the property or attribute value to get the items from
	 * @param translateBooleanAs
	 *            if this is a checkbox, use these string values for T/F
	 * @param readOnly
	 *            whether it is editable
	 * @param inValueCol
	 *            whether to display the field value in the Value column of the viewer. (<code>false</code> means that its value
	 *            displays in a column whose name matches the id.
	 * @param data
	 *            the Attribute object
	 */
	public ValueTreeNodeUpdateModel(String name, IUpdateHandler handler, CellEditor editor, String[] items, String itemsFrom,
			String translateBooleanAs, boolean readOnly, boolean inValueCol, AttributeType data) {
		super(name, handler, editor, items, translateBooleanAs, readOnly, data.getTooltip(), data.getDescription(), data
				.getStatus(), itemsFrom);
		children = new ArrayList<InfoTreeNodeModel>();
		generateChildren(inValueCol);
	}

	/**
	 * @return the info nodes attached to this Property or Attribute
	 */
	public List<InfoTreeNodeModel> getChildren() {
		return children;
	}

	/**
	 * @return defaultValue (used by info children)
	 */
	public String getDefault() {
		return defaultValue;
	}

	/*
	 * The parent value node displays only the name and the editable value. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IColumnViewerLabelSupport#getDisplayValue( java.lang.String)
	 */
	public String getDisplayValue(String columnName) {
		String displayValue = null;
		if (JAXBControlUIConstants.COLUMN_NAME.equals(columnName)) {
			displayValue = name;
		} else if (isChecked() && JAXBControlUIConstants.COLUMN_VALUE.equals(columnName)) {
			displayValue = getValueAsString();
		}
		if (displayValue == null) {
			return JAXBControlUIConstants.ZEROSTR;
		}
		return displayValue;
	}

	/**
	 * @return attribute status field value, if any (used by info children)
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * Creates the info nodes for this entry.
	 * 
	 * @param inValueCol
	 *            whether to display the field value in the Value column of the viewer. (<code>false</code> means that its value
	 *            displays in a column whose name matches the id.
	 */
	private void generateChildren(boolean inValueCol) {
		children.add(new InfoTreeNodeModel(this, JAXBControlUIConstants.COLUMN_DEFAULT, inValueCol));
		children.add(new InfoTreeNodeModel(this, JAXBControlUIConstants.COLUMN_TYPE, inValueCol));
		children.add(new InfoTreeNodeModel(this, JAXBControlUIConstants.COLUMN_STATUS, inValueCol));
		children.add(new InfoTreeNodeModel(this, JAXBControlUIConstants.COLUMN_DESC, inValueCol));
	}
}
