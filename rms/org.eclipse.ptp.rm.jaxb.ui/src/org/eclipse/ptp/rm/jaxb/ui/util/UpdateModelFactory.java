/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.util;

import java.io.File;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Arg;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.Template;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rm.jaxb.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.ui.model.ButtonUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.ComboUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.SpinnerUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.TableRowUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.TextUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.ValueTreeNodeUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

public class UpdateModelFactory implements IJAXBUINonNLSConstants {

	private static class CellDescriptor {
		private String name;
		private String tooltip;
		private String description;
		private String choice;
		private Integer min;
		private Integer max;
		private boolean readOnly;
		private final CellEditorType type;
		private String[] items;
		protected Color[] foreground;
		protected Color[] background;
		protected Font[] font;

		private CellDescriptor(Object data, List<ColumnData> columnData) {
			type = CellEditorType.getType(data);
			if (data instanceof Attribute) {
				Attribute a = (Attribute) data;
				name = a.getName();
				choice = a.getChoice();
				min = a.getMin();
				max = a.getMax();
				readOnly = a.isReadOnly();
				tooltip = a.getTooltip();
				if (tooltip == null) {
					tooltip = ZEROSTR;
				} else {
					tooltip = WidgetBuilderUtils.fitToLineLength(64, tooltip);
				}
				description = a.getDescription();
			} else if (data instanceof Property) {
				Property p = (Property) data;
				name = p.getName();
				readOnly = p.isReadOnly();
			}
			if (description == null) {
				description = ZEROSTR;
			}
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
	}

	private enum CellEditorType {
		TEXT, COMBO, SPINNER, CHECK;

		public static CellEditorType getType(Object object) {
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

	private static class ControlDescriptor {
		private String title;
		private Object layoutData;
		private boolean readOnly;
		private int style;
		private String background;
		private String foreground;
		private FontDescriptor font;
		private Integer min;
		private Integer max;
		private String tooltip;
		private String choice;
		private String fixedText;
		private List<String> valueList;

		private ControlDescriptor(Widget widget, RMVariableMap rmMap) {
			setControlData(widget);
			setMapDependentData(widget, rmMap);
		}

		private void setControlData(Widget widget) {
			title = widget.getTitle();
			LayoutDataDescriptor layout = widget.getLayoutData();
			layoutData = LaunchTabBuilder.createLayoutData(layout);
			style = WidgetBuilderUtils.getStyle(widget.getStyle());
			readOnly = widget.isReadOnly();
			if (readOnly) {
				style |= SWT.READ_ONLY;
			}
			background = widget.getBackground();
			foreground = widget.getForeground();
			font = widget.getFont();
			tooltip = widget.getTooltip();
			fixedText = widget.getFixedText();
		}

		private void setMapDependentData(Widget widget, RMVariableMap rmMap) {
			if (tooltip == null) {
				tooltip = ZEROSTR;
			} else {
				tooltip = WidgetBuilderUtils.fitToLineLength(64, rmMap.getString(tooltip));
			}

			if (fixedText != null) {
				fixedText = rmMap.getString(fixedText);
			}

			Map<String, Object> vars = rmMap.getVariables();
			String s = widget.getSaveValueTo();
			if (s != null) {
				Object data = vars.get(s);
				if (data != null) {
					setPropertyData(data);
				}
			}
			s = widget.getValueListFrom();
			if (s != null) {
				Object data = vars.get(s);
				if (data != null) {
					setValueListData(data);
				}
			}
		}

		private void setPropertyData(Object data) {
			if (data instanceof Attribute) {
				Attribute a = (Attribute) data;
				choice = a.getChoice();
				min = a.getMin();
				max = a.getMax();
			}
		}

		@SuppressWarnings("unchecked")
		private void setValueListData(Object data) {
			if (data instanceof Attribute) {
				Attribute a = (Attribute) data;
				Object value = a.getValue();
				if (value instanceof List<?>) {
					valueList = (List<String>) value;
				}
			} else {
				Property p = (Property) data;
				Object value = p.getValue();
				if (value instanceof List<?>) {
					valueList = (List<String>) value;
				}
			}
		}
	}

	public static ViewerUpdateModel createModel(ColumnViewer viewer, AttributeViewer descriptor,
			JAXBDynamicLaunchConfigurationTab tab) {
		String name = descriptor.getName();
		Template template = descriptor.getValue();
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ViewerUpdateModel model = new ViewerUpdateModel(name, handler, (ICheckable) viewer, template);
		return model;
	}

	public static IUpdateModel createModel(Composite parent, Widget widget, JAXBDynamicLaunchConfigurationTab tab) {
		Control control = createControl(parent, widget, tab);
		if (control instanceof Label) {
			return null;
		}

		String name = widget.getSaveValueTo();
		List<Arg> dynamic = null;

		Widget.DynamicText dt = widget.getDynamicText();
		if (dt != null) {
			dynamic = dt.getArg();
		}

		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		IUpdateModel model = null;
		if (control instanceof Text) {
			if (name != null && !ZEROSTR.equals(name)) {
				model = new TextUpdateModel(name, handler, (Text) control);
			}
			if (dynamic != null) {
				model = new TextUpdateModel(dynamic, handler, (Text) control);
			}
		} else if (control instanceof Combo) {
			model = new ComboUpdateModel(name, handler, (Combo) control);
		} else if (control instanceof Spinner) {
			model = new SpinnerUpdateModel(name, handler, (Spinner) control);
		} else if (control instanceof Button) {
			model = new ButtonUpdateModel(name, handler, (Button) control);
		}
		return model;
	}

	public static ICellEditorUpdateModel createModel(Object data, ColumnViewer viewer, List<ColumnData> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		ICellEditorUpdateModel model = null;
		if (viewer instanceof TableViewer) {
			model = createModel(data, (TableViewer) viewer, columnData, tab);
		} else {
			model = createModel(data, (TreeViewer) viewer, columnData, tab);
		}
		return model;
	}

	private static Text createBrowseLocal(final Composite parent, final ControlDescriptor cd) {
		final Text t = WidgetBuilderUtils.createText(parent, SWT.BORDER, cd.layoutData, true, ZEROSTR);
		WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					URI uri = new URI(t.getText());
					int type = cd.readOnly ? SWT.OPEN : SWT.SAVE;
					FileDialog d = new FileDialog(parent.getShell(), type);
					d.setFileName(uri.getPath());
					String f = d.open();
					if (f != null) {
						t.setText(new File(f).toURI().toString());
					} else {
						t.setText(ZEROSTR);
					}
				} catch (Throwable t) {
					JAXBUIPlugin.log(t);
				}
			}
		});
		return t;
	}

	private static Text createBrowseRemote(final Composite parent, final ControlDescriptor cd,
			final JAXBDynamicLaunchConfigurationTab tab) {
		final Text t = WidgetBuilderUtils.createText(parent, SWT.BORDER, cd.layoutData, true, ZEROSTR);
		WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					URI uri = new URI(t.getText());
					uri = RemoteUIServicesUtils.browse(parent.getShell(), uri, tab.getParent().getDelegate(), true, cd.readOnly);
					if (uri != null) {
						t.setText(uri.toString());
					} else {
						t.setText(ZEROSTR);
					}
				} catch (Throwable t) {
					JAXBUIPlugin.log(t);
				}
			}
		});
		return t;
	}

	private static Combo createCombo(Composite parent, final ControlDescriptor cd) {
		String[] items = null;
		if (cd.valueList != null) {
			items = cd.valueList.toArray(new String[0]);
		} else if (cd.choice != null) {
			items = cd.choice.split(CM);
		}
		if (items == null) {
			items = new String[0];
		} else {
			if (items.length > 0) {
				items = WidgetBuilderUtils.normalizeComboItems(items);
			}
		}
		return WidgetBuilderUtils.createCombo(parent, cd.style, cd.layoutData, items, ZEROSTR, cd.title, cd.tooltip, null);
	}

	private static Control createControl(final Composite parent, Widget widget, JAXBDynamicLaunchConfigurationTab tab) {
		ControlDescriptor cd = new ControlDescriptor(widget, RMVariableMap.getActiveInstance());
		String type = widget.getType();

		Control c = null;
		if (LABEL.equals(type)) {
			c = WidgetBuilderUtils.createLabel(parent, cd.fixedText, cd.style, cd.layoutData);
		} else if (TEXT.equals(type)) {
			c = createText(parent, cd);
		} else if (RADIOBUTTON.equals(type)) {
			c = WidgetBuilderUtils.createRadioButton(parent, cd.title, null, null);
		} else if (CHECKBOX.equals(type)) {
			c = WidgetBuilderUtils.createCheckButton(parent, cd.title, null);
		} else if (SPINNER.equals(type)) {
			c = WidgetBuilderUtils.createSpinner(parent, cd.layoutData, cd.title, cd.min, cd.max, cd.min, null);
		} else if (COMBO.equals(type)) {
			c = createCombo(parent, cd);
		} else if (BROWSELOCAL.equals(type)) {
			c = createBrowseLocal(parent, cd);
		} else if (BROWSEREMOTE.equals(type)) {
			c = createBrowseRemote(parent, cd, tab);
		}

		if (c != null) {
			if (!ZEROSTR.equals(cd.tooltip)) {
				c.setToolTipText(cd.tooltip);
			}
			if (cd.foreground != null) {
				c.setForeground(WidgetBuilderUtils.getColor(cd.foreground));
			}
			if (cd.background != null) {
				c.setBackground(WidgetBuilderUtils.getColor(cd.background));
			}
			if (cd.font != null) {
				c.setFont(WidgetBuilderUtils.getFont(cd.font));
			}
		}
		return c;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static CellEditor createEditor(CellDescriptor cd, Object data, Composite parent) {
		CellEditor editor = null;
		if (cd.type == CellEditorType.TEXT) {
			editor = new TextCellEditor(parent);
		} else if (cd.type == CellEditorType.CHECK) {
			editor = new CheckboxCellEditor(parent);
		} else if (cd.type == CellEditorType.SPINNER) {
			editor = new SpinnerCellEditor(parent, cd.min, cd.max);
		} else if (cd.type == CellEditorType.COMBO) {
			Object o = null;
			if (data instanceof Attribute) {
				if (cd.choice != null) {
					cd.choice = cd.choice.trim();
					cd.items = cd.choice.split(CM);
				} else {
					o = ((Attribute) data).getValue();
				}
			} else {
				o = ((Property) data).getValue();
			}
			if (cd.items == null) {
				if (o instanceof Collection) {
					cd.items = (String[]) ((Collection) o).toArray(new String[0]);
				} else {
					cd.items = new String[0];
				}
			}
			editor = new ComboBoxCellEditor(parent, cd.items, SWT.READ_ONLY);
		}
		return editor;
	}

	private static ICellEditorUpdateModel createModel(Object data, TableViewer viewer, List<ColumnData> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(data, columnData);
		CellEditor editor = createEditor(cd, data, viewer.getTable());
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ICellEditorUpdateModel model = null;
		if (data instanceof Attribute) {
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, (Attribute) data);
		} else if (data instanceof Property) {
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly);
		}
		return model;
	}

	private static ICellEditorUpdateModel createModel(Object data, TreeViewer viewer, List<ColumnData> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(data, columnData);
		CellEditor editor = createEditor(cd, data, viewer.getTree());
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ICellEditorUpdateModel model = null;
		Object[] properties = viewer.getColumnProperties();
		boolean inValueCol = properties.length == 2;
		if (data instanceof Attribute) {
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, inValueCol, (Attribute) data);
		} else if (data instanceof Property) {
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, inValueCol);
		}
		return model;
	}

	private static Text createText(final Composite parent, final ControlDescriptor cd) {
		return WidgetBuilderUtils.createText(parent, cd.style, cd.layoutData, cd.readOnly, ZEROSTR);
	}
}
