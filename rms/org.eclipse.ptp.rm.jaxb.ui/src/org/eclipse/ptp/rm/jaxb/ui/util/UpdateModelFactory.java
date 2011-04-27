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
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.ptp.rm.jaxb.core.data.FontType;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.TemplateType;
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
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

/**
 * Utilities for creating update models for control widgets, cell editors and
 * viewers.
 * 
 * @author arossi
 * 
 */
public class UpdateModelFactory implements IJAXBUINonNLSConstants {

	/**
	 * Internal data object for configuration convenience.
	 * 
	 * @author arossi
	 * 
	 */
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

		/**
		 * @param data
		 *            Property or Attribute
		 * @param columnData
		 *            list of JAXB data elements describing the viewer columns
		 */
		private CellDescriptor(Object data, List<ColumnDataType> columnData) {
			type = CellEditorType.getType(data);
			if (data instanceof AttributeType) {
				AttributeType a = (AttributeType) data;
				name = a.getName();
				choice = a.getChoice();
				min = a.getMin();
				max = a.getMax();
				readOnly = a.isReadOnly();
				tooltip = a.getTooltip();
				if (tooltip == null) {
					tooltip = ZEROSTR;
				} else {
					tooltip = WidgetBuilderUtils.removeTabOrLineBreak(tooltip);
				}
				description = a.getDescription();
			} else if (data instanceof PropertyType) {
				PropertyType p = (PropertyType) data;
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
				FontType fd = columnData.get(i).getFont();
				if (fd != null) {
					font[i] = WidgetBuilderUtils.getFont(fd);
				} else {
					font[i] = null;
				}
			}
		}
	}

	/**
	 * Mapping of Property or Attribute type to appropriate cell editor.
	 * 
	 * @author arossi
	 * 
	 */
	private enum CellEditorType {
		TEXT, COMBO, SPINNER, CHECK;

		/**
		 * Determine the CellEditor from the Property or Attribute type or
		 * value.
		 * 
		 * @param object
		 *            Property or Attribute
		 * @return enum value for editor
		 */
		public static CellEditorType getType(Object object) {
			if (object instanceof PropertyType) {
				PropertyType p = (PropertyType) object;
				Object value = p.getValue();
				String clzz = p.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				if (value != null) {
					return getType(value);
				}
				return TEXT;
			} else if (object instanceof AttributeType) {
				AttributeType a = (AttributeType) object;
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

		/**
		 * If the Property or Attribute carries an explicit type name, determine
		 * CellEditor type from it.
		 * 
		 * @param clzz
		 *            type name
		 * @return enum value for editor
		 */
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

	/**
	 * Internal data object for configuration convenience.
	 * 
	 * @author arossi
	 * 
	 */
	private static class ControlDescriptor {
		private String title;
		private Object layoutData;
		private boolean readOnly;
		private int style;
		private String background;
		private String foreground;
		private FontType font;
		private Integer min;
		private Integer max;
		private String tooltip;
		private String choice;
		private String fixedText;
		private List<String> valueList;

		/**
		 * @param widget
		 *            JAXB data element describing widget
		 * @param rmMap
		 *            used to retrieve data object info
		 */
		private ControlDescriptor(WidgetType widget, RMVariableMap rmMap) {
			setControlData(widget);
			setMapDependentData(widget, rmMap);
		}

		/**
		 * Configure the fields which do not depend on the underlying data
		 * object.
		 * 
		 * @param widget
		 *            JAXB data element describing widget
		 */
		private void setControlData(WidgetType widget) {
			title = widget.getTitle();
			LayoutDataType layout = widget.getLayoutData();
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

		/**
		 * Get the choice (Combo), min and max (Spinner) settings.
		 * 
		 * @param data
		 *            Attribute
		 */
		private void setData(Object data) {
			if (data instanceof AttributeType) {
				AttributeType a = (AttributeType) data;
				choice = a.getChoice();
				min = a.getMin();
				max = a.getMax();
			}
		}

		/**
		 * Configure the fields which depend on the underlying data object.
		 * 
		 * @param widget
		 *            JAXB data element describing widget
		 * @param rmMap
		 *            used to retrieve data object info
		 */
		private void setMapDependentData(WidgetType widget, RMVariableMap rmMap) {
			if (tooltip == null) {
				tooltip = ZEROSTR;
			} else {
				tooltip = WidgetBuilderUtils.removeTabOrLineBreak(rmMap.getString(tooltip));
			}

			if (fixedText != null) {
				fixedText = rmMap.getString(fixedText);
			}

			Map<String, Object> vars = rmMap.getVariables();
			String s = widget.getSaveValueTo();
			if (s != null) {
				Object data = vars.get(s);
				if (data != null) {
					setData(data);
				}
			}
			s = widget.getItemsFrom();
			if (s != null) {
				Object data = vars.get(s);
				if (data != null) {
					setValueListData(data);
				}
			}
		}

		/**
		 * Get Combo items from a reference to another Attribute or Property
		 * with a List as its value.
		 * 
		 * @param data
		 *            Property or Attribute
		 */
		@SuppressWarnings("unchecked")
		private void setValueListData(Object data) {
			if (data instanceof AttributeType) {
				AttributeType a = (AttributeType) data;
				Object value = a.getValue();
				if (value instanceof List<?>) {
					valueList = (List<String>) value;
				}
			} else {
				PropertyType p = (PropertyType) data;
				Object value = p.getValue();
				if (value instanceof List<?>) {
					valueList = (List<String>) value;
				}
			}
		}
	}

	/**
	 * Constructs the viewer update model.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.ui.handlers.ValueUpdateHandler
	 * 
	 * @param viewer
	 *            SWT object to which this model is bound
	 * @param descriptor
	 *            JAXB data element describing the viewer
	 * @param tab
	 *            launch tab being built (accessed for value handler)
	 * @return
	 */
	public static ViewerUpdateModel createModel(ColumnViewer viewer, AttributeViewerType descriptor,
			JAXBDynamicLaunchConfigurationTab tab) {
		String name = descriptor.getName();
		TemplateType template = descriptor.getValue();
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ViewerUpdateModel model = new ViewerUpdateModel(name, handler, (ICheckable) viewer, template);
		return model;
	}

	/**
	 * Constructs the widget and its update model.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param widget
	 *            JAXB data object describing the widget to which this model is
	 *            bound
	 * @param tab
	 *            launch tab being built
	 * @return
	 */
	public static IUpdateModel createModel(Composite parent, WidgetType widget, JAXBDynamicLaunchConfigurationTab tab,
			RMVariableMap rmVarMap) {
		Control control = createControl(parent, widget, tab, rmVarMap);
		if (control instanceof Label) {
			return null;
		}

		String name = widget.getSaveValueTo();
		List<ArgType> dynamic = null;

		WidgetType.DynamicText dt = widget.getDynamicText();
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

	/**
	 * Constructs the cell editor and its update model. Calls
	 * {@link #createModel(Object, TableViewer, List, JAXBDynamicLaunchConfigurationTab)}
	 * or
	 * {@link #createModel(Object, TreeViewer, List, JAXBDynamicLaunchConfigurationTab)}
	 * .
	 * 
	 * @param data
	 *            Property or Attribute for this viewer row.
	 * @param viewer
	 *            CheckboxTableViewer or CheckboxTreeViewer
	 * @param columnData
	 *            list of JAXB data elements describing the viewer columns
	 * @param tab
	 *            launch tab being built
	 * @return
	 */
	public static ICellEditorUpdateModel createModel(Object data, ColumnViewer viewer, List<ColumnDataType> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		ICellEditorUpdateModel model = null;
		if (viewer instanceof TableViewer) {
			model = createModel(data, (TableViewer) viewer, columnData, tab);
		} else {
			model = createModel(data, (TreeViewer) viewer, columnData, tab);
		}
		return model;
	}

	/**
	 * Creates a read-only Text area and adjacent browse button using local
	 * connection information. The update model will be attached to the returned
	 * text widget, as its text value will trigger updates.
	 * 
	 * @see org.eclipse.swt.widgets.Text
	 * @see org.eclipse.swt.widgets.Button
	 * 
	 * @param parent
	 *            to which the control belongs
	 * @param cd
	 *            internal data object carrying model description info
	 * @param tab
	 *            launch tab being built
	 * @return the text widget carrying the browse selection
	 */
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

	/**
	 * Creates a read-only Text area and adjacent browse button using remote
	 * connection information. The update model will be attached to the returned
	 * text widget, as its text value will trigger updates.
	 * 
	 * @see org.eclipse.swt.widgets.Text
	 * @see org.eclipse.swt.widgets.Button
	 * 
	 * @param parent
	 *            to which the control belongs
	 * @param cd
	 *            internal data object carrying model description info
	 * @param tab
	 *            launch tab being built
	 * @return the text widget carrying the browse selection
	 */
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

	/**
	 * @see org.eclipse.swt.widgets.Combo
	 * 
	 * @param parent
	 *            to which the control belongs
	 * @param cd
	 *            internal data object carrying model description info
	 * @return the Combo widget
	 */
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

	/**
	 * Calls: {@link #createCombo(Composite, ControlDescriptor)},
	 * {@link #createBrowseLocal(Composite, ControlDescriptor)},
	 * {@link #createBrowseRemote(Composite, ControlDescriptor, JAXBDynamicLaunchConfigurationTab)}
	 * ,
	 * {@link WidgetBuilderUtils#createLabel(Composite, String, Integer, Object)}
	 * ,
	 * {@link WidgetBuilderUtils#createRadioButton(Composite, String, String, SelectionListener)}
	 * , {@link WidgetBuilderUtils#createButton(Composite, String, Integer)},
	 * {@link WidgetBuilderUtils#createSpinner(Composite, Object, String, Integer, Integer, Integer, org.eclipse.swt.events.ModifyListener)}
	 * .
	 * 
	 * @param parent
	 *            to which the control belongs
	 * @param widget
	 *            JAXB data element describing the widget
	 * @param tab
	 *            launch tab being built
	 * @return the resulting control (<code>null</code> if the widget is a
	 *         Label).
	 */
	private static Control createControl(final Composite parent, WidgetType widget, JAXBDynamicLaunchConfigurationTab tab,
			RMVariableMap rmVarMap) {
		ControlDescriptor cd = new ControlDescriptor(widget, rmVarMap);
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

	/**
	 * Constructs the editor of appropriate type.
	 * 
	 * @see org.eclipse.jface.viewers.TextCellEditor
	 * @see org.eclipse.jface.viewers.CheckboxCellEditor
	 * @see org.eclipse.jface.viewers.ComboBoxCellEditor
	 * @see org.eclipse.ptp.rm.jaxb.ui.cell.SpinnerCellEditor
	 * 
	 * @param cd
	 *            internal object holding model description
	 * @param data
	 *            Property or Attribute
	 * @param parent
	 *            Table or Tree to which the editor belongs
	 * @return the editor of appropriate type
	 */
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
			if (data instanceof AttributeType) {
				if (cd.choice != null) {
					cd.choice = cd.choice.trim();
					cd.items = cd.choice.split(CM);
				} else {
					o = ((AttributeType) data).getValue();
				}
			} else {
				o = ((PropertyType) data).getValue();
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

	/**
	 * Constructs CellEditor and model for table row. Calls
	 * {@link #createEditor(CellDescriptor, Object, Composite)}.
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.TableRowUpdateModel
	 * 
	 * @param data
	 *            Property or Attribute
	 * @param viewer
	 *            to which this row belongs
	 * @param columnData
	 *            list of JAXB data elements describing table columns
	 * @param tab
	 *            launch tab being built
	 * @return the cell editor update model, which contains a reference to the
	 *         CellEditor
	 */
	private static ICellEditorUpdateModel createModel(Object data, TableViewer viewer, List<ColumnDataType> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(data, columnData);
		CellEditor editor = createEditor(cd, data, viewer.getTable());
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ICellEditorUpdateModel model = null;
		if (data instanceof AttributeType) {
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, (AttributeType) data);
		} else if (data instanceof PropertyType) {
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly);
		}
		if (model != null) {
			model.setBackground(cd.background);
			model.setFont(cd.font);
			model.setForeground(cd.foreground);
		}
		return model;
	}

	/**
	 * Constructs CellEditor and model for tree node. Calls
	 * {@link #createEditor(CellDescriptor, Object, Composite)}.
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor
	 * @see org.eclipse.ptp.rm.jaxb.ui.model.ValueTreeNodeUpdateModel
	 * 
	 * @param data
	 *            Property or Attribute
	 * @param viewer
	 *            to which this row belongs
	 * @param columnData
	 *            list of JAXB data elements describing table columns
	 * @param tab
	 *            launch tab being built
	 * @return the cell editor update model, which contains a reference to the
	 *         CellEditor
	 */
	private static ICellEditorUpdateModel createModel(Object data, TreeViewer viewer, List<ColumnDataType> columnData,
			JAXBDynamicLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(data, columnData);
		CellEditor editor = createEditor(cd, data, viewer.getTree());
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		ICellEditorUpdateModel model = null;
		Object[] properties = viewer.getColumnProperties();
		boolean inValueCol = properties.length == 2;
		if (data instanceof AttributeType) {
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, inValueCol, (AttributeType) data);
		} else if (data instanceof PropertyType) {
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.readOnly, inValueCol);
		}
		if (model != null) {
			model.setBackground(cd.background);
			model.setFont(cd.font);
			model.setForeground(cd.foreground);
		}
		return model;
	}

	/**
	 * @see org.eclipse.swt.widgets.Text
	 * 
	 * @param parent
	 *            control to which widget belongs
	 * @param cd
	 *            internal data object describing the widget model
	 * @return SWT text widget
	 */
	private static Text createText(final Composite parent, final ControlDescriptor cd) {
		return WidgetBuilderUtils.createText(parent, cd.style, cd.layoutData, cd.readOnly, ZEROSTR);
	}
}
