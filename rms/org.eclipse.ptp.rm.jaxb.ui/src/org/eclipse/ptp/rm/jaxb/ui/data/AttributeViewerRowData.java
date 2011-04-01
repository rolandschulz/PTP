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
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBCheckboxCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBComboCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBSpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBTextCellEditor;
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
	private String initialValue = null;
	private String choice = null;
	private Integer min = null;
	private Integer max = null;
	private String foreground;
	private String background;
	private CellEditor editor;
	private boolean discovered;

	public AttributeViewerRowData(Object data) {
		this.data = data;
		discovered = false;
		initialize();
	}

	public boolean canEdit() {
		if (data instanceof Property) {
			Property p = (Property) data;
			return !p.isReadOnly() && p.isSelected();
		} else if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			return !a.isReadOnly() && a.isSelected();
		}
		return false;
	}

	public synchronized CellEditor getCellEditor(TableViewer viewer) {
		if (editor == null) {
			createEditor(viewer.getTable());
		}
		return editor;
	}

	public synchronized CellEditor getCellEditor(TreeViewer viewer) {
		if (editor == null) {
			createEditor(viewer.getTree());
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
				if (this.value != null && !(this.value instanceof Collection) && !(this.value instanceof Map)) {
					value = String.valueOf(this.value);
				}
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
				if (this.value != null && !(this.value instanceof Collection) && !(this.value instanceof Map)) {
					value = String.valueOf(this.value);
				}
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

	public boolean isSelected() {
		if (data instanceof Property) {
			return ((Property) data).isSelected();
		} else if (data instanceof Attribute) {
			return ((Attribute) data).isSelected();
		}
		return false;
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

	public void setSelected(boolean selected) {
		if (data instanceof Property) {
			((Property) data).setSelected(selected);
		} else if (data instanceof Attribute) {
			((Attribute) data).setSelected(selected);
		}
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public void setValueFromString(String value) {
		if (editor instanceof JAXBCheckboxCellEditor) {
			this.value = Boolean.valueOf(value);
		} else if (editor instanceof JAXBSpinnerCellEditor) {
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private CellEditor createEditor(Composite parent) {
		CellEditorType type = CellEditorType.getType(data);
		if (type == CellEditorType.TEXT) {
			JAXBTextCellEditor e = new JAXBTextCellEditor(parent);
			editor = e;
			if (initialValue != null) {
				e.setValue(initialValue);
			}
			if (foreground != null) {
				e.getText().setForeground(WidgetBuilderUtils.getColor(foreground));
			}
			if (background != null) {
				e.getText().setBackground(WidgetBuilderUtils.getColor(background));
			}
		} else if (type == CellEditorType.CHECK) {
			JAXBCheckboxCellEditor e = new JAXBCheckboxCellEditor(parent);
			editor = e;
			if (initialValue != null) {
				e.setValue(Boolean.valueOf(String.valueOf(initialValue)));
			}
			if (foreground != null) {
				e.getCheckbox().setForeground(WidgetBuilderUtils.getColor(foreground));
			}
			if (background != null) {
				e.getCheckbox().setBackground(WidgetBuilderUtils.getColor(background));
			}
		} else if (type == CellEditorType.SPINNER) {
			JAXBSpinnerCellEditor e = new JAXBSpinnerCellEditor(parent, min, max);
			editor = e;
			if (initialValue != null) {
				e.setValue(Integer.valueOf(String.valueOf(initialValue)));
			}
			if (foreground != null) {
				e.getSpinner().setForeground(WidgetBuilderUtils.getColor(foreground));
			}
			if (background != null) {
				e.getSpinner().setBackground(WidgetBuilderUtils.getColor(background));
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
			JAXBComboCellEditor e = new JAXBComboCellEditor(parent, items);
			editor = e;
			if (initialValue != null) {
				e.setValue(initialValue);
			}
			if (foreground != null) {
				e.getComboBox().setForeground(WidgetBuilderUtils.getColor(foreground));
			}
			if (background != null) {
				e.getComboBox().setBackground(WidgetBuilderUtils.getColor(background));
			}
		}
		return editor;
	}

	private void initialize() {
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
	}
}
