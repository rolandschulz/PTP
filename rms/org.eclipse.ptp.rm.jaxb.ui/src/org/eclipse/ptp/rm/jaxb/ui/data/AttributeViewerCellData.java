package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.Collection;
import java.util.List;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.ui.IAttributeViewerColumnLabelSupport;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBCheckboxCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBComboCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBSpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.cell.JAXBTextCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
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
	protected boolean discovered;

	protected String text;
	protected int selected;
	protected String[] items;
	protected boolean checked;
	protected int count;

	protected Color[] foreground;
	protected Color[] background;
	protected Font[] font;

	protected AttributeViewerCellData() {
		data = null;
	}

	protected AttributeViewerCellData(Object data, List<ColumnData> columnData) {
		this.data = data;
		discovered = false;
		text = ZEROSTR;
		selected = 0;
		checked = false;
		if (data instanceof Attribute) {
			Attribute a = (Attribute) data;
			name = a.getName();
			choice = a.getChoice();
			min = a.getMin();
			max = a.getMax();
		} else if (data instanceof Property) {
			name = ((Property) data).getName();
		}
		count = min == null ? 0 : min;
		int cols = columnData.size();
		foreground = new Color[cols];
		background = new Color[cols];
		font = new Font[cols];
		for (int i = 0; i < columnData.size(); i++) {
			String color = columnData.get(i).getForeground();
			if (color != null) {
				foreground[i] = WidgetBuilderUtils.getColor(color);
			} else {
				foreground[i] = null;
			}
			color = columnData.get(i).getBackground();
			if (color != null) {
				background[i] = WidgetBuilderUtils.getColor(color);
			} else {
				background[i] = null;
			}
		}
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

	public Color getBackground(Object element, int columnIndex) {
		return background[columnIndex];
	}

	public Object getData() {
		return data;
	}

	public Color getForeground(Object element, int columnIndex) {
		return foreground[columnIndex];
	}

	public String getReplaced(String pattern) {
		String value = getActualValueAsString();
		String result = new String(pattern);
		if (name != null) {
			result = result.replaceAll(NAME_TAG, name);
		}
		if (value != null) {
			result = result.replaceAll(VALUE_TAG, value);
		}
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
		if (editor instanceof JAXBTextCellEditor) {
			return text;
		} else if (editor instanceof JAXBCheckboxCellEditor) {
			return checked;
		} else if (editor instanceof JAXBSpinnerCellEditor) {
			return count;
		} else if (editor instanceof JAXBComboCellEditor) {
			return selected;
		}
		return null;
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
		if (editor instanceof JAXBTextCellEditor) {
			text = (String) value;
		} else if (editor instanceof JAXBCheckboxCellEditor) {
			if (value == null) {
				checked = false;
			} else {
				checked = (Boolean) value;
			}
		} else if (editor instanceof JAXBSpinnerCellEditor) {
			if (value == null) {
				if (min == null) {
					count = 0;
				} else {
					count = min;
				}
			} else {
				count = (Integer) value;
			}
		} else if (editor instanceof JAXBComboCellEditor) {
			if (value == null) {
				selected = 0;
				text = ZEROSTR;
			} else {
				text = (String) value;
				for (int i = 0; i < items.length; i++) {
					if (items[i].equals(text)) {
						selected = i;
						break;
					}
				}
			}
		}
	}

	public void setValueFromEditor(Object value) {
		if (editor instanceof JAXBTextCellEditor) {
			text = (String) value;
		} else if (editor instanceof JAXBCheckboxCellEditor) {
			if (value == null) {
				checked = false;
			} else {
				checked = (Boolean) value;
			}
		} else if (editor instanceof JAXBSpinnerCellEditor) {
			if (value == null) {
				if (min == null) {
					count = 0;
				} else {
					count = min;
				}
			} else {
				count = (Integer) value;
			}
		} else if (editor instanceof JAXBComboCellEditor) {
			if (value == null) {
				selected = 0;
			} else {
				selected = (Integer) value;
			}
			/*
			 * have to take care of writing in here
			 */
			text = items[selected];
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
	protected CellEditor createEditor(Composite parent, ColumnDescriptor d) {
		CellEditorType type = CellEditorType.getType(data);
		if (type == CellEditorType.TEXT) {
			editor = new JAXBTextCellEditor(parent);
		} else if (type == CellEditorType.CHECK) {
			editor = new JAXBCheckboxCellEditor(parent);
		} else if (type == CellEditorType.SPINNER) {
			editor = new JAXBSpinnerCellEditor(parent, min, max);
		} else if (type == CellEditorType.COMBO) {
			Object o = null;
			if (data instanceof Attribute) {
				if (choice != null) {
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
			editor = new JAXBComboCellEditor(parent, items);
		}
		return editor;
	}

	protected String getActualValueAsString() {
		if (editor instanceof JAXBTextCellEditor || editor instanceof JAXBComboCellEditor) {
			if (text != null) {
				return text;
			}
		} else if (editor instanceof JAXBCheckboxCellEditor) {
			return String.valueOf(checked);
		} else if (editor instanceof JAXBSpinnerCellEditor) {
			return String.valueOf(count);
		}
		return ZEROSTR;
	}

}
