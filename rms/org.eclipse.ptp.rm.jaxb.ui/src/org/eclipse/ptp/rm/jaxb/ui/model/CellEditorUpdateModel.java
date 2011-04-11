/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.model;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ptp.rm.jaxb.core.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;

/**
 * Specialized base class for the Viewer cell editor models.
 * 
 * @author arossi
 * 
 */
public abstract class CellEditorUpdateModel extends AbstractUpdateModel implements ICellEditorUpdateModel {

	protected boolean checked;
	protected boolean readOnly;
	protected String tooltip;
	protected String description;
	protected String status;
	protected Color[] foreground;
	protected Color[] background;
	protected Font[] font;
	protected CellEditor editor;
	protected ViewerUpdateModel viewer;

	protected String stringValue;
	protected int index;
	protected String[] items;
	protected boolean booleanValue;
	protected int integerValue;

	/**
	 * @param name
	 *            of the model, which will correspond to the name of a Property
	 *            or Attribute the value is to be saved to
	 * @param handler
	 *            the handler for notifying other widgets to refresh their
	 *            values
	 * @param editor
	 *            the cell editor for the value cell
	 * @param items
	 *            if this is a combo editor, the selection items
	 * @param readOnly
	 *            if this is a text box, whether it is editable
	 * @param tooltip
	 *            to display
	 * @param description
	 *            to display
	 * @param status
	 *            the attribute's status value, if any
	 */
	protected CellEditorUpdateModel(String name, ValueUpdateHandler handler, CellEditor editor, String[] items, boolean readOnly,
			String tooltip, String description, String status) {
		super(name, handler);
		this.editor = editor;
		this.readOnly = readOnly;
		this.items = items;
		this.tooltip = tooltip;
		this.description = description;
		this.status = status;
	}

	/*
	 * The combo cell editor does not allow write-ins. So effectively the only
	 * widget which can be "read-only" is a text box. We must allow edit on the
	 * others for them to appear. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#canEdit()
	 */
	public boolean canEdit() {
		return checked && !((editor instanceof TextCellEditor) && readOnly);
	}

	/*
	 * Return the background color for the column, if set. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang
	 * .Object, int)
	 */
	public Color getBackground(Object element, int columnIndex) {
		if (background == null) {
			return null;
		}
		return background[columnIndex];
	}

	/*
	 * The cell editor for this row or node. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#getCellEditor()
	 */
	public CellEditor getCellEditor() {
		return editor;
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	/*
	 * Returns cell editor. @see #getCellEditor() (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#getControl()
	 */
	@Override
	public Object getControl() {
		return editor;
	}

	/*
	 * Returns the description string. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.IColumnViewerLabelSupport#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * Return the font for the column, if set. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object,
	 * int)
	 */
	public Font getFont(Object element, int columnIndex) {
		if (font == null) {
			return null;
		}
		return font[columnIndex];
	}

	/*
	 * Return the foreground color for the column, if set. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object,
	 * int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		if (foreground == null) {
			return null;
		}
		return foreground[columnIndex];
	}

	/*
	 * For generating a templated string using all the names and values in the
	 * viewer rows. Skips non-checked items. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#getReplacedValue(java
	 * .lang.String)
	 */
	public String getReplacedValue(String pattern) {
		if (!checked) {
			return ZEROSTR;
		}
		String value = getValueAsString();
		if (ZEROSTR.equals(value)) {
			return ZEROSTR;
		}
		String result = pattern.replaceAll(NAME_TAG, name);
		result = result.replaceAll(VALUE_TAG, value);
		return result;
	}

	/*
	 * Returns the tooltip string. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#getTooltip()
	 */
	public String getTooltip() {
		return tooltip;
	}

	/**
	 * @return the type of the value (Boolean, Integer or String)
	 */
	public String getType() {
		if (editor instanceof CheckboxCellEditor) {
			return Boolean.TYPE.getCanonicalName();
		} else if (editor instanceof SpinnerCellEditor) {
			return Integer.TYPE.getCanonicalName();
		}
		return STRING;
	}

	/*
	 * The editor needs the selection index for a combo box; otherwise the value
	 * is of the same type as the stored value. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#getValueForEditor()
	 */
	public Object getValueForEditor() {
		if (editor instanceof TextCellEditor) {
			return stringValue;
		} else if (editor instanceof CheckboxCellEditor) {
			return booleanValue;
		} else if (editor instanceof SpinnerCellEditor) {
			return integerValue;
		} else if (editor instanceof ComboBoxCellEditor) {
			return index;
		}
		return null;
	}

	/*
	 * The most recent update on the map value (@see #setValueFromEditor)
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return mapValue;
	}

	/*
	 * Also sets whether this model is currently on a checked viewer item.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.model.AbstractUpdateModel#initialize(org.eclipse
	 * .ptp.rm.jaxb.core.variables.LCVariableMap)
	 */
	@Override
	public void initialize(LCVariableMap lcMap) {
		this.lcMap = lcMap;
		if (name != null) {
			checked = lcMap.isChecked(name);
		}
		super.initialize(lcMap);
	}

	/*
	 * Whether the model item is checked in the viewer. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#isChecked()
	 */
	public boolean isChecked() {
		return checked;
	}

	/*
	 * Casts or converts the map value to the proper type to set it to the
	 * correct internal field. For combo box editors both the string value of
	 * the selection and the index are maintained.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.get(name);
		if (editor instanceof TextCellEditor) {
			stringValue = ZEROSTR;
			if (mapValue != null) {
				stringValue = (String) mapValue;
			}
		} else if (editor instanceof CheckboxCellEditor) {
			booleanValue = false;
			if (mapValue != null) {
				if (mapValue instanceof String) {
					booleanValue = Boolean.parseBoolean((String) mapValue);
				} else {
					booleanValue = (Boolean) mapValue;
				}
			}
		} else if (editor instanceof SpinnerCellEditor) {
			SpinnerCellEditor e = (SpinnerCellEditor) editor;
			integerValue = e.getMin();
			if (mapValue != null) {
				if (mapValue instanceof String) {
					integerValue = Integer.parseInt((String) mapValue);
				} else {
					integerValue = (Integer) mapValue;
				}
			}
		} else if (editor instanceof ComboBoxCellEditor) {
			if (mapValue == null) {
				index = UNDEFINED;
				stringValue = ZEROSTR;
			} else {
				stringValue = (String) mapValue;
				for (int i = 0; i < items.length; i++) {
					if (items[i].equals(stringValue)) {
						index = i;
						break;
					}
				}
				if (index == items.length) {
					index = UNDEFINED;
					stringValue = ZEROSTR;
				}
			}
		}
		refreshing = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setBackground(org.eclipse
	 * .swt.graphics.Color[])
	 */
	public void setBackground(Color[] background) {
		this.background = background;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setChecked(boolean)
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setFont(org.eclipse
	 * .swt.graphics.Font[])
	 */
	public void setFont(Font[] font) {
		this.font = font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setForeground(org.eclipse
	 * .swt.graphics.Color[])
	 */
	public void setForeground(Color[] foreground) {
		this.foreground = foreground;
	}

	/*
	 * Casts the value to its appropriate field. For combo boxes, also sets the
	 * selection string from the index. Calls storeValue on the viewer to update
	 * the viewer template string; this in turn calls #getReplacedValue on all
	 * its cell models. Finishes with call to store value on the current cell
	 * value.(non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setValueFromEditor(
	 * java.lang.Object)
	 */
	public void setValueFromEditor(Object value) {
		if (editor instanceof TextCellEditor) {
			stringValue = ZEROSTR;
			if (value != null) {
				stringValue = (String) value;
			}
			mapValue = stringValue;
		} else if (editor instanceof CheckboxCellEditor) {
			if (value == null) {
				booleanValue = false;
			} else {
				booleanValue = (Boolean) value;
			}
			mapValue = booleanValue;
		} else if (editor instanceof SpinnerCellEditor) {
			SpinnerCellEditor e = (SpinnerCellEditor) editor;
			if (value == null) {
				integerValue = e.getMin();
			} else {
				integerValue = (Integer) value;
			}
			mapValue = integerValue;
		} else if (editor instanceof ComboBoxCellEditor) {
			if (value == null || ((Integer) value) == UNDEFINED) {
				index = UNDEFINED;
				stringValue = ZEROSTR;
			} else {
				index = (Integer) value;
				stringValue = items[index];
			}
			mapValue = stringValue;
		}
		viewer.storeValue();
		storeValue();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel#setViewer(org.eclipse
	 * .ptp.rm.jaxb.ui.model.ViewerUpdateModel)
	 */
	public void setViewer(ViewerUpdateModel viewer) {
		this.viewer = viewer;
	}

	/**
	 * @return appropriate string value for current editor value.
	 */
	protected String getValueAsString() {
		if (editor instanceof TextCellEditor || editor instanceof ComboBoxCellEditor) {
			return stringValue;
		} else if (editor instanceof CheckboxCellEditor) {
			return String.valueOf(booleanValue);
		} else if (editor instanceof SpinnerCellEditor) {
			return String.valueOf(integerValue);
		}
		return ZEROSTR;
	}
}
