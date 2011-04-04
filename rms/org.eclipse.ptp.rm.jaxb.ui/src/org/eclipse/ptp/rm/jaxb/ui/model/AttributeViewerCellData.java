package org.eclipse.ptp.rm.jaxb.ui.model;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IAttributeViewerColumnLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.IWidgetListener;
import org.eclipse.ptp.rm.jaxb.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Composite;

public abstract class AttributeViewerCellData implements IAttributeViewerColumnLabelSupport, IJAXBUINonNLSConstants {
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

	protected final Object data;
	protected String name;
	protected String choice;
	protected Integer min;
	protected Integer max;
	protected CellEditor editor;
	protected IWidgetListener listener;
	protected boolean selected;
	protected boolean readOnly;

	protected String stringValue;
	protected int index;
	protected String[] items;
	protected boolean booleanValue;
	protected int integerValue;

	protected Color[] foreground;
	protected Color[] background;
	protected Font[] font;

	protected AttributeViewerCellData() {
		data = null;
	}

	protected AttributeViewerCellData(Object data, List<ColumnData> columnData) {
		this.data = data;
		stringValue = ZEROSTR;
		index = UNDEFINED;
		booleanValue = false;
		selected = false;
		readOnly = false;
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			name = a.getName();
			choice = a.getChoice();
			min = a.getMin();
			max = a.getMax();
			readOnly = a.isReadOnly();
		} else if (data instanceof Property) {
			Property p = (Property) data;
			name = p.getName();
			readOnly = p.isReadOnly();
		}
		integerValue = min == null ? 0 : min;
		int cols = columnData.size();
		foreground = new Color[cols];
		background = new Color[cols];
		font = new Font[cols];
		for (int i = 0; i < columnData.size(); i++) {
			String attr = columnData.get(i).getForeground();
			if (attr != null) {
				foreground[i] = WidgetBuilderUtils.getColor(attr);
			} else {
				foreground[i] = null;
			}
			attr = columnData.get(i).getBackground();
			if (attr != null) {
				background[i] = WidgetBuilderUtils.getColor(attr);
			} else {
				background[i] = null;
			}
			FontDescriptor fd = columnData.get(i).getFont();
			if (fd != null) {
				font[i] = WidgetBuilderUtils.getFont(fd);
			} else {
				font[i] = null;
			}
		}
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
		return background[columnIndex];
	}

	public Object getData() {
		return data;
	}

	public Font getFont(Object element, int columnIndex) {
		return font[columnIndex];
	}

	public Color getForeground(Object element, int columnIndex) {
		return foreground[columnIndex];
	}

	public String getReplaced(String pattern) {
		String value = getActualValueAsString();
		if (ZEROSTR.equals(value)) {
			return ZEROSTR;
		}
		String result = pattern.replaceAll(NAME_TAG, name);
		result = result.replaceAll(VALUE_TAG, value);
		return result;
	}

	public String getTooltip() {
		if (data instanceof Attribute) {
			String tt = ((Attribute) data).getTooltip();
			if (tt != null) {
				return WidgetBuilderUtils.fitToLineLength(60, tt);
			}
		}
		return ZEROSTR;
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

	public boolean isSelected() {
		return selected;
	}

	public boolean isVisible() {
		if (data instanceof Property) {
			return ((Property) data).isVisible();
		} else if (data instanceof Attribute) {
			return ((Attribute) data).isVisible();
		}
		return false;
	}

	public void setListener(IWidgetListener listener) {
		this.listener = listener;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public void setValue(Object value) {
		if (editor instanceof TextCellEditor) {
			stringValue = (String) value;
		} else if (editor instanceof CheckboxCellEditor) {
			if (value == null) {
				booleanValue = false;
			} else if (value instanceof String) {
				booleanValue = Boolean.parseBoolean((String) value);
			} else {
				booleanValue = (Boolean) value;
			}
		} else if (editor instanceof SpinnerCellEditor) {
			if (value == null) {
				if (min == null) {
					integerValue = 0;
				} else {
					integerValue = min;
				}
			} else if (value instanceof String) {
				integerValue = Integer.parseInt((String) value);
			} else {
				integerValue = (Integer) value;
			}
		} else if (editor instanceof ComboBoxCellEditor) {
			if (value == null) {
				index = UNDEFINED;
				stringValue = ZEROSTR;
			} else {
				stringValue = (String) value;
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
	}

	public void setValueFromEditor(Object value) {
		if (editor instanceof TextCellEditor) {
			stringValue = (String) value;
		} else if (editor instanceof CheckboxCellEditor) {
			if (value == null) {
				booleanValue = false;
			} else {
				booleanValue = (Boolean) value;
			}
		} else if (editor instanceof SpinnerCellEditor) {
			if (value == null) {
				if (min == null) {
					integerValue = 0;
				} else {
					integerValue = min;
				}
			} else {
				integerValue = (Integer) value;
			}
		} else if (editor instanceof ComboBoxCellEditor) {
			if (value == null || ((Integer) value) == UNDEFINED) {
				index = UNDEFINED;
				stringValue = ZEROSTR;
			} else {
				index = (Integer) value;
				stringValue = items[index];
			}
		}
		listener.valueChanged();
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	protected CellEditor createEditor(Composite parent, ColumnData d) {
		CellEditorType type = CellEditorType.getType(data);
		if (type == CellEditorType.TEXT) {
			editor = new TextCellEditor(parent);
		} else if (type == CellEditorType.CHECK) {
			editor = new CheckboxCellEditor(parent);
		} else if (type == CellEditorType.SPINNER) {
			editor = new SpinnerCellEditor(parent, min, max);
		} else if (type == CellEditorType.COMBO) {
			Object o = null;
			if (data instanceof Attribute) {
				if (choice != null) {
					choice = choice.trim();
					items = choice.split(CM);
				} else {
					o = ((Attribute) data).getValue();
				}
			} else if (data instanceof Property) {
				o = ((Property) data).getValue();
			}
			if (items == null) {
				if (o instanceof Collection) {
					items = (String[]) ((Collection) o).toArray(new String[0]);
				} else {
					items = new String[0];
				}
			}
			editor = new ComboBoxCellEditor(parent, items, SWT.READ_ONLY);
		}
		return editor;
	}

	protected String getActualValueAsString() {
		if (editor instanceof TextCellEditor || editor instanceof ComboBoxCellEditor) {
			if (stringValue != null) {
				return stringValue;
			}
		} else if (editor instanceof CheckboxCellEditor) {
			return String.valueOf(booleanValue);
		} else if (editor instanceof SpinnerCellEditor) {
			return String.valueOf(integerValue);
		}
		return ZEROSTR;
	}
}
