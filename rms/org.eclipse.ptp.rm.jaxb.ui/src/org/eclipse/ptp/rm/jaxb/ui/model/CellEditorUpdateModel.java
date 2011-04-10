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

public abstract class CellEditorUpdateModel extends AbstractUpdateModel implements ICellEditorUpdateModel {

	protected boolean selected;
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
	 * others for them to appear.
	 */
	public boolean canEdit() {
		return selected && !((editor instanceof TextCellEditor) && readOnly);
	}

	public Color getBackground(Object element, int columnIndex) {
		if (background == null) {
			return null;
		}
		return background[columnIndex];
	}

	public CellEditor getCellEditor() {
		return editor;
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	@Override
	public Object getControl() {
		return editor;
	}

	public String getDescription() {
		return description;
	}

	public Font[] getFont() {
		return font;
	}

	public Font getFont(Object element, int columnIndex) {
		if (font == null) {
			return null;
		}
		return font[columnIndex];
	}

	public Color getForeground(Object element, int columnIndex) {
		if (foreground == null) {
			return null;
		}
		return foreground[columnIndex];
	}

	/**
	 * For generating a templated string using all the names and values in the
	 * viewer rows. Skips non-selected items.
	 */
	public String getReplacedValue(String pattern) {
		if (!selected) {
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

	public String getTooltip() {
		return tooltip;
	}

	public String getType() {
		if (editor instanceof CheckboxCellEditor) {
			return Boolean.TYPE.getCanonicalName();
		} else if (editor instanceof SpinnerCellEditor) {
			return Integer.TYPE.getCanonicalName();
		}
		return STRING;
	}

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
	 * In the case of the editor, we take the most recent update on the map
	 * value (@see #setValueFromEditor) (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.IUpdateModel#getValueFromControl()
	 */
	public Object getValueFromControl() {
		return mapValue;
	}

	@Override
	public void initialize(LCVariableMap lcMap) {
		this.lcMap = lcMap;
		if (name != null) {
			selected = lcMap.isSelected(name);
		}
		super.initialize(lcMap);
	}

	public boolean isSelected() {
		return selected;
	}

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

	public void setBackground(Color[] background) {
		this.background = background;
	}

	public void setFont(Font[] font) {
		this.font = font;
	}

	public void setForeground(Color[] foreground) {
		this.foreground = foreground;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

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

	public void setViewer(ViewerUpdateModel viewer) {
		this.viewer = viewer;
	}

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
