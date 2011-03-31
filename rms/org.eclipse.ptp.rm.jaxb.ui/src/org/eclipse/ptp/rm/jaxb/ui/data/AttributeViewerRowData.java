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
package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.Collection;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.cell.CheckboxCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;

public class AttributeViewerRowData implements IJAXBUINonNLSConstants {

	/*
	 * Maps are not supported.
	 */
	private enum CellEditorType {
		TEXT, COMBO, SPINNER, CHECK;

		private static CellEditorType getType(Object object) {
			if (object instanceof Property) {
				Property p = (Property) object;
				Object value = p.getValue();
				String clzz = p.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				if (value != null) {
					return getType(value);
				}
				return TEXT;
			} else if (object instanceof Attribute) {
				Attribute a = (Attribute) object;
				if (a.getChoice() != null) {
					return COMBO;
				}
				String clzz = a.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				Object value = a.getValue();
				if (value != null) {
					return getType(value);
				}
				return TEXT;
			} else if (object instanceof Collection) {
				return COMBO;
			} else if (object instanceof Integer) {
				return SPINNER;
			} else if (object instanceof Boolean) {
				return CHECK;
			} else {
				return TEXT;
			}
		}

		private static CellEditorType getTypeFromClass(String clzz) {
			if (clzz.indexOf(NT) > 0) {
				return SPINNER;
			}
			if (clzz.indexOf(BOOL) >= 0) {
				return CHECK;
			}
			if (clzz.indexOf(IST) > 0) {
				return COMBO;
			}
			if (clzz.indexOf(ET) > 0) {
				return COMBO;
			}
			if (clzz.indexOf(ECTOR) > 0) {
				return COMBO;
			}
			return TEXT;
		}
	}

	private final Object data;
	private Object value;
	private CellEditor editor;
	private final CellEditorType type;
	private boolean discovered;

	public AttributeViewerRowData(Object data) {
		this.data = data;
		type = CellEditorType.getType(data);
		discovered = false;
	}

	public boolean canEdit() {
		if (data instanceof Property) {
			return !((Property) data).isReadOnly();
		} else if (data instanceof Attribute) {
			return !((Attribute) data).isReadOnly();
		}
		return false;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public CellEditor getCellEditor(ColumnViewer viewer) {
		if (editor == null) {
			Composite parent = (Composite) viewer.getControl();
			Object value = null;
			String initialValue = null;
			String choice = null;
			Integer min = null;
			Integer max = null;
			if (data instanceof Attribute) {
				Attribute a = (Attribute) data;
				value = a.getValue();
				if (!(value instanceof Collection) && !(value instanceof Map)) {
					initialValue = String.valueOf(value);
				}
				if (initialValue == null) {
					initialValue = a.getDefault();
				}
				choice = a.getChoice();
				min = a.getMin();
				max = a.getMax();
			} else {
				Property p = (Property) data;
				value = p.getValue();
				if (!(value instanceof Collection) && !(value instanceof Map)) {
					initialValue = String.valueOf(value);
				}
				if (initialValue == null) {
					initialValue = p.getDefault();
				}
			}
			if (type == CellEditorType.TEXT) {
				editor = new TextCellEditor(parent);
				if (initialValue != null) {
					editor.setValue(initialValue);
				}
			} else if (type == CellEditorType.CHECK) {
				editor = new CheckboxCellEditor(parent);
				if (initialValue != null) {
					editor.setValue(Boolean.valueOf(initialValue));
				}
			} else if (type == CellEditorType.SPINNER) {
				editor = new SpinnerCellEditor(parent, min, max);
				if (initialValue != null) {
					editor.setValue(Integer.valueOf(initialValue));
				}
			} else if (type == CellEditorType.COMBO) {
				String[] items = null;
				if (data instanceof Attribute) {
					if (choice != null) {
						items = choice.split(CM);
					}
				} else if (value instanceof Collection) {
					items = (String[]) ((Collection) value).toArray(new String[0]);
				} else {
					items = new String[0];
				}
				editor = new ComboBoxCellEditor(parent, items);
				if (initialValue != null) {
					editor.setValue(initialValue);
				}
			}
		}
		return editor;
	}

	public String getColumnDisplayValue(String columnName) {
		String value = null;
		if (data instanceof Property) {
			Property p = (Property) data;
			if (COLUMN_NAME.equals(columnName)) {
				value = p.getName();
			} else if (COLUMN_VALUE.equals(columnName)) {
				value = String.valueOf(value);
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				value = p.getDefault();
			}
		} else if (data instanceof Attribute) {
			Attribute ja = (Attribute) data;
			if (COLUMN_NAME.equals(columnName)) {
				value = ja.getName();
			} else if (COLUMN_DESC.equals(columnName)) {
				value = ja.getDescription();
			} else if (COLUMN_DEFAULT.equals(columnName)) {
				value = ja.getDefault();
			} else if (COLUMN_TYPE.equals(columnName)) {
				value = ja.getType();
			} else if (COLUMN_TOOLTIP.equals(columnName)) {
				value = ja.getTooltip();
				if (value != null) {
					value = WidgetBuilderUtils.fitToLineLength(60, value);
				}
			} else if (COLUMN_STATUS.equals(columnName)) {
				value = ja.getStatus();
			} else if (COLUMN_VALUE.equals(columnName)) {
				value = String.valueOf(value);
			}
		}

		if (value == null) {
			return ZEROSTR;
		}
		return value;
	}

	public Image getColumnImage(String columnName) {
		return null;
	}

	public Object getData() {
		return data;
	}

	public String getReplaced(String pattern) {
		String name = getColumnDisplayValue(COLUMN_NAME);
		String value = getColumnDisplayValue(COLUMN_VALUE);
		String result = new String(pattern);
		if (name != null) {
			result = result.replaceAll(NAME_TAG, name);
		}
		if (value != null) {
			result = result.replaceAll(VALUE_TAG, name);
		}
		return result;
	}

	public String getTooltip() {
		return getColumnDisplayValue(COLUMN_TOOLTIP);
	}

	public Object getValue() {
		return value;
	}

	public boolean isDiscovered() {
		return discovered;
	}

	public boolean isVisible() {
		if (data instanceof Property) {
			return ((Property) data).isVisible();
		} else if (data instanceof Attribute) {
			return ((Attribute) data).isVisible();
		}
		return false;
	}

	public void setDiscovered(boolean discovered) {
		this.discovered = discovered;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setValueFromString(String value) {
		if (type == CellEditorType.CHECK) {
			this.value = Boolean.valueOf(value);
		} else if (type == CellEditorType.CHECK) {
			this.value = Integer.valueOf(value);
		} else {
			this.value = value;
		}
	}

	public void setVisible(boolean visible) {
		if (data instanceof Property) {
			((Property) data).setVisible(visible);
		} else if (data instanceof Attribute) {
			((Attribute) data).setVisible(visible);
		}
	}
}
