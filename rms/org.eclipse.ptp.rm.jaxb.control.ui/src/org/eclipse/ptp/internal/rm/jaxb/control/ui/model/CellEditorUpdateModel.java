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

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.WidgetActionUtils;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.AbstractUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
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

	private static String replaceAll(String sequence, String tag, String with) {
		int i = 0;
		int j = 0;
		int ln = sequence.length();
		StringBuffer buffer = new StringBuffer();
		while (i < ln) {
			j = sequence.indexOf(tag, i);
			if (j < 0) {
				j = ln;
				break;
			}
			buffer.append(sequence.substring(i, j)).append(with);
			i = j + tag.length();
		}
		buffer.append(sequence.substring(i, j));
		return buffer.toString();
	}

	protected boolean readOnly;
	protected boolean checked;
	protected String tooltip;
	protected String description;
	protected String status;
	protected String itemsFrom;
	protected Color[] foreground;
	protected Color[] background;
	protected Font[] font;
	protected CellEditor editor;

	protected IUpdateModel viewer;
	protected String stringValue;
	protected int index;
	protected String[] items;
	protected boolean booleanValue;
	protected int integerValue;

	/**
	 * @param name
	 *            of the model, which will correspond to the name of an attribute the value is to be saved to
	 * @param handler
	 *            the handler for notifying other widgets to refresh their values
	 * @param editor
	 *            the cell editor for the value cell
	 * @param items
	 *            if this is a combo editor, the selection items
	 * @param translateBooleanAs
	 *            if this is a checkbox, use these string values for T/F
	 * @param readOnly
	 *            if this is a text box, whether it is editable
	 * @param tooltip
	 *            to display
	 * @param description
	 *            to display
	 * @param status
	 *            the attribute's status value, if any
	 * @param itemsFrom
	 *            for combo box
	 */
	protected CellEditorUpdateModel(String name, IUpdateHandler handler, CellEditor editor, String[] items,
			String translateBooleanAs, boolean readOnly, String tooltip, String description, String status, String itemsFrom) {
		super(name, handler);
		this.editor = editor;
		this.readOnly = readOnly;
		this.items = items;
		this.itemsFrom = itemsFrom;
		this.tooltip = tooltip;
		this.description = description;
		this.status = status;
		setBooleanToString(translateBooleanAs);
	}

	/*
	 * The combo cell editor does not allow write-ins. So effectively the only widget which can be "read-only" is a text box. We
	 * must allow edit on the others for them to appear. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#canEdit()
	 */
	public boolean canEdit() {
		return isChecked() && !((editor instanceof TextCellEditor) && readOnly);
	}

	/*
	 * Return the background color for the column, if set. (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableColorProvider#getBackground(java.lang .Object, int)
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
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#getCellEditor()
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
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.model.AbstractUpdateModel#getControl()
	 */
	@Override
	public Object getControl() {
		return editor;
	}

	/*
	 * Returns the description string. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IColumnViewerLabelSupport#getDescription()
	 */
	public String getDescription() {
		return description;
	}

	/*
	 * Return the font for the column, if set. (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
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
	 * @see org.eclipse.jface.viewers.ITableFontProvider#getFont(java.lang.Object, int)
	 */
	public Color getForeground(Object element, int columnIndex) {
		if (foreground == null) {
			return null;
		}
		return foreground[columnIndex];
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel#getParent()
	 */
	public Viewer getParent() {
		return (Viewer) viewer.getControl();
	}

	/*
	 * For generating a templated string using all the names and values in the viewer rows. Skips non-checked items. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#getReplacedValue(java .lang.String)
	 */
	public String getReplacedValue(String pattern) {
		if (!isChecked()) {
			return JAXBControlUIConstants.ZEROSTR;
		}
		String value = getValueAsString();
		if (JAXBControlUIConstants.ZEROSTR.equals(value)) {
			return JAXBControlUIConstants.ZEROSTR;
		}
		String result = replaceAll(pattern, JAXBControlUIConstants.NAME_TAG, name);
		return replaceAll(result, JAXBControlUIConstants.VALUE_TAG, value);
	}

	/*
	 * Returns the tooltip string. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#getTooltip()
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
		return JAXBControlUIConstants.STRING;
	}

	/*
	 * The editor needs the selection index for a combo box; otherwise the value is of the same type as the stored value.
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#getValueForEditor()
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
	 * The most recent update on the map value (@see #setValueFromEditor) (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return mapValue;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.rm.jaxb.control.ui.model.AbstractUpdateModel#initialize(org.eclipse.ptp.rm.jaxb.core.IVariableMap,
	 * org.eclipse.ptp.rm.jaxb.core.IVariableMap)
	 */
	@Override
	public void initialize(ILaunchConfiguration configuration, IVariableMap rmMap, IVariableMap lcMap) {
		this.lcMap = lcMap;
		if (editor instanceof ComboBoxCellEditor) {
			if (itemsFrom != null) {
				items = WidgetActionUtils.getItemsFrom(rmMap, itemsFrom);
				if (items.length == 0) {
					items = WidgetActionUtils.getItemsFrom(lcMap, itemsFrom);
				}
				items = WidgetBuilderUtils.normalizeComboItems(items);
				((ComboBoxCellEditor) editor).setItems(items);
			}
		}
		super.initialize(configuration, rmMap, lcMap);
	}

	/*
	 * Whether the model item is checked in the viewer. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#isChecked()
	 */
	public boolean isChecked() {
		return checked;
	}

	/*
	 * Casts or converts the map value to the proper type to set it to the correct internal field. For combo box editors both the
	 * string value of the selection and the index are maintained.(non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.IUpdateModel#refreshValueFromMap()
	 */
	public void refreshValueFromMap() {
		refreshing = true;
		mapValue = lcMap.getValue(name);
		if (editor instanceof TextCellEditor) {
			stringValue = JAXBControlUIConstants.ZEROSTR;
			if (mapValue != null) {
				stringValue = (String) mapValue;
			}
		} else if (editor instanceof CheckboxCellEditor) {
			booleanValue = false;
			if (JAXBUIConstants.ZEROSTR.equals(mapValue)) {
				mapValue = null;
			}
			if (mapValue != null) {
				booleanValue = maybeGetBooleanFromString(mapValue);
				Object o = getBooleanValue(booleanValue);
				if (o instanceof String) {
					stringValue = (String) o;
				}
			}
		} else if (editor instanceof SpinnerCellEditor) {
			SpinnerCellEditor e = (SpinnerCellEditor) editor;
			integerValue = e.getMin();
			if (JAXBUIConstants.ZEROSTR.equals(mapValue)) {
				mapValue = null;
			}
			if (mapValue != null) {
				if (mapValue instanceof String) {
					integerValue = Integer.parseInt((String) mapValue);
				} else {
					integerValue = (Integer) mapValue;
				}
			}
		} else if (editor instanceof ComboBoxCellEditor) {
			if (mapValue == null) {
				index = JAXBControlUIConstants.UNDEFINED;
				stringValue = JAXBControlUIConstants.ZEROSTR;
			} else {
				stringValue = (String) mapValue;
				for (int i = 0; i < items.length; i++) {
					if (items[i].equals(stringValue)) {
						index = i;
						break;
					}
				}
				if (index == items.length) {
					index = JAXBControlUIConstants.UNDEFINED;
					stringValue = JAXBControlUIConstants.ZEROSTR;
				}
			}
		}
		refreshing = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#setBackground(org.eclipse .swt.graphics.Color[])
	 */
	public void setBackground(Color[] background) {
		this.background = background;
	}

	/**
	 * @param checked
	 *            whether item in viewer is checked
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#setFont(org.eclipse .swt.graphics.Font[])
	 */
	public void setFont(Font[] font) {
		this.font = font;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#setForeground(org.eclipse .swt.graphics.Color[])
	 */
	public void setForeground(Color[] foreground) {
		this.foreground = foreground;
	}

	/*
	 * Casts the value to its appropriate field. For combo boxes, also sets the selection string from the index. Calls store value
	 * on the current cell value. Finishes with calls to storeValue on the viewer to update the viewer template string; this in turn
	 * calls #getReplacedValue on all its cell models. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#setValueFromEditor( java.lang.Object)
	 */
	public void setValueFromEditor(Object value) {
		if (editor instanceof TextCellEditor) {
			stringValue = JAXBControlUIConstants.ZEROSTR;
			if (value != null) {
				stringValue = (String) value;
			}
			mapValue = stringValue;
		} else if (editor instanceof CheckboxCellEditor) {
			if (value == null) {
				booleanValue = false;
			} else {
				booleanValue = (Boolean) value;
				Object o = getBooleanValue((Boolean) value);
				if (o instanceof String) {
					stringValue = (String) o;
					mapValue = stringValue;
				} else {
					mapValue = booleanValue;
				}
			}
		} else if (editor instanceof SpinnerCellEditor) {
			SpinnerCellEditor e = (SpinnerCellEditor) editor;
			if (value == null) {
				integerValue = e.getMin();
			} else {
				integerValue = (Integer) value;
			}
			mapValue = integerValue;
		} else if (editor instanceof ComboBoxCellEditor) {
			if (value == null || ((Integer) value) == JAXBControlUIConstants.UNDEFINED) {
				index = JAXBControlUIConstants.UNDEFINED;
				stringValue = JAXBControlUIConstants.ZEROSTR;
			} else {
				index = (Integer) value;
				stringValue = items[index];
			}
			mapValue = stringValue;
		}
		try {
			Object v = storeValue();
			viewer.storeValue();
			handleUpdate(v);
		} catch (Exception ignored) {
			// Ignore
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.ui.ICellEditorUpdateModel#setViewer(org.eclipse
	 * .ptp.rm.jaxb.ui.model.ViewerUpdateModel)
	 */
	public void setViewer(IUpdateModel viewer) {
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
		return JAXBControlUIConstants.ZEROSTR;
	}
}
