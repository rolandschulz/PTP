/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.utils;

import java.net.URI;
import java.util.ArrayList;
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
import org.eclipse.ptp.remote.ui.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.rm.jaxb.control.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBControllerLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ButtonGroupUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ButtonUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ComboUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.SpinnerUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.TableRowUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.TextUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.ArgType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.BrowseType;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonActionType;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonGroupType;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateType;
import org.eclipse.ptp.rm.jaxb.core.data.FontType;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.PushButtonType;
import org.eclipse.ptp.rm.jaxb.core.data.TemplateType;
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.utils.ui.swt.SWTUtil;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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
public class UpdateModelFactory {

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
		private String itemsFrom;
		private String translateBooleanAs;
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
				itemsFrom = a.getItemsFrom();
				translateBooleanAs = a.getTranslateBooleanAs();
				min = a.getMin();
				max = a.getMax();
				readOnly = a.isReadOnly();
				tooltip = a.getTooltip();
				if (tooltip == null) {
					tooltip = JAXBControlUIConstants.ZEROSTR;
				} else {
					tooltip = WidgetBuilderUtils.removeTabOrLineBreak(tooltip);
				}
				description = a.getDescription();
			} else if (data instanceof PropertyType) {
				PropertyType p = (PropertyType) data;
				name = p.getName();
				translateBooleanAs = p.getTranslateBooleanAs();
				readOnly = p.isReadOnly();
			}
			if (description == null) {
				description = JAXBControlUIConstants.ZEROSTR;
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
				if (p.getTranslateBooleanAs() != null) {
					return CHECK;
				}
				Object value = p.getValue();
				String clzz = p.getType();
				if (clzz != null) {
					return getTypeFromClass(clzz);
				}
				if (value != null) {
					return getType(value);
				}
			} else if (object instanceof AttributeType) {
				AttributeType a = (AttributeType) object;
				if (a.getTranslateBooleanAs() != null) {
					return CHECK;
				}
				if (a.getChoice() != null || a.getItemsFrom() != null) {
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
			}
			return TEXT;
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
			if (clzz.indexOf(JAXBControlUIConstants.NT) > 0) {
				return SPINNER;
			}
			if (clzz.indexOf(JAXBControlUIConstants.BOOL) >= 0) {
				return CHECK;
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
		private String widgetType;
		private String title;
		private Object layoutData;
		private Object subLayoutData;
		private boolean readOnly;
		private boolean localOnly;
		private boolean directory;
		private boolean returnUri;
		private int style;
		private String background;
		private String foreground;
		private FontType font;
		private Integer min;
		private Integer max;
		private String tooltip;
		private String choice;
		private String fixedText;
		private ButtonActionType action;
		private String itemsFrom;
		private String translateBooleanAs;

		private ControlDescriptor(BrowseType browse, IVariableMap rmMap) {
			setControlData(browse);
			setMapDependentData(browse, rmMap);
		}

		private ControlDescriptor(PushButtonType button, IVariableMap rmMap) {
			setControlData(button);
			setMapDependentData(button, rmMap);
		}

		/**
		 * @param widget
		 *            JAXB data element describing widget
		 * @param rmMap
		 *            used to retrieve data object info
		 */
		private ControlDescriptor(WidgetType widget, IVariableMap rmMap) {
			setControlData(widget);
			setMapDependentData(widget, rmMap);
			/*
			 * NOTE: this will override the attribute field, so check for null
			 * first
			 */
			String s = widget.getItemsFrom();
			if (s != null) {
				itemsFrom = s;
			}
		}

		/**
		 * Configure the fields which do not depend on the underlying data
		 * object.
		 * 
		 * @param browse
		 *            JAXB data element describing widget
		 */
		private void setControlData(BrowseType browse) {
			widgetType = JAXBControlUIConstants.BROWSE;
			title = browse.getTitle();
			style = WidgetBuilderUtils.getStyle(browse.getTextStyle());
			LayoutDataType layout = browse.getTextLayoutData();
			layoutData = LaunchTabBuilder.createLayoutData(layout);
			layout = browse.getButtonLayoutData();
			subLayoutData = LaunchTabBuilder.createLayoutData(layout);
			directory = browse.isDirectory();
			returnUri = browse.isUri();
			localOnly = browse.isLocalOnly();
			background = browse.getBackground();
			foreground = browse.getForeground();
			font = browse.getFont();
			tooltip = browse.getTooltip();
		}

		/**
		 * Configure the fields which do not depend on the underlying data
		 * object.
		 * 
		 * @param button
		 *            JAXB data element describing widget
		 */
		private void setControlData(PushButtonType button) {
			widgetType = JAXBControlUIConstants.ACTION;
			title = button.getTitle();
			LayoutDataType layout = button.getLayoutData();
			layoutData = LaunchTabBuilder.createLayoutData(layout);
			background = button.getBackground();
			foreground = button.getForeground();
			font = button.getFont();
			tooltip = button.getTooltip();
			action = button.getButtonAction();
		}

		/**
		 * Configure the fields which do not depend on the underlying data
		 * object.
		 * 
		 * @param widget
		 *            JAXB data element describing widget
		 */
		private void setControlData(WidgetType widget) {
			widgetType = widget.getType();
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
		 *            Attribute or Property
		 */
		private void setData(Object data) {
			if (data instanceof AttributeType) {
				AttributeType a = (AttributeType) data;
				choice = a.getChoice();
				itemsFrom = a.getItemsFrom();
				min = a.getMin();
				max = a.getMax();
				translateBooleanAs = a.getTranslateBooleanAs();
			} else if (data instanceof PropertyType) {
				translateBooleanAs = ((PropertyType) data).getTranslateBooleanAs();
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
		private void setMapDependentData(Object widget, IVariableMap rmMap) {
			if (tooltip == null) {
				tooltip = JAXBControlUIConstants.ZEROSTR;
			} else {
				tooltip = WidgetBuilderUtils.removeTabOrLineBreak(rmMap.getString(tooltip));
			}

			if (fixedText != null) {
				fixedText = rmMap.getString(fixedText);
			}

			String s = null;
			if (widget instanceof WidgetType) {
				s = ((WidgetType) widget).getSaveValueTo();
			} else if (widget instanceof BrowseType) {
				s = ((BrowseType) widget).getSaveValueTo();
			}

			if (s != null) {
				Object data = rmMap.get(s);
				if (data != null) {
					setData(data);
				}
			}
		}
	}

	/**
	 * Constructs button group update model.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.ButtonGroupUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler
	 * 
	 * @param bGroupDescriptor
	 *            JAXB data element describing the button group.
	 * @param bGroup
	 *            SWT object to which this model is bound
	 * @param tab
	 *            launch tab being built (accessed for value handler)
	 * @param rmVarMap
	 *            resource manager environment
	 * @return
	 */
	public static IUpdateModel createModel(ButtonGroupType bGroupDescriptor, Composite bGroup,
			JAXBDynamicLaunchConfigurationTab tab, IVariableMap rmVarMap, Map<String, Button> sources,
			Map<ControlStateType, Control> targets) {
		List<WidgetType> bWidgets = bGroupDescriptor.getButton();
		List<Button> buttons = new ArrayList<Button>();
		for (WidgetType widget : bWidgets) {
			ControlDescriptor cd = new ControlDescriptor(widget, rmVarMap);
			Control control = createControl(bGroup, cd, tab);
			if (control instanceof Button) {
				Button b = (Button) control;
				buttons.add(b);
				String id = widget.getButtonId();
				if (id != null) {
					sources.put(id, b);
				}
			}
			ControlStateType cst = widget.getControlState();
			if (cst != null) {
				targets.put(cst, control);
			}
		}
		String name = bGroupDescriptor.getSaveValueTo();
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		return new ButtonGroupUpdateModel(name, handler, bGroup, buttons);
	}

	/**
	 * Constructs the viewer update model.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.handlers.ValueUpdateHandler
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
		return new ViewerUpdateModel(name, descriptor.isInitialAllChecked(), handler, (ICheckable) viewer, template);
	}

	/**
	 * Constructs browse widget pair.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param browse
	 *            JAXB data object describing the widget to which this model is
	 *            bound
	 * @param tab
	 *            launch tab being built
	 * @param rmVarMap
	 *            resource manager environment
	 * @param sources
	 *            map of widgets to check for state
	 * @param targets
	 *            map of widgets on which to set state
	 * 
	 */
	public static IUpdateModel createModel(Composite parent, BrowseType browse, JAXBDynamicLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(browse, rmVarMap);

		Control control = createBrowse(parent, browse, cd, tab, targets);

		String name = browse.getSaveValueTo();
		ValueUpdateHandler handler = tab.getParent().getUpdateHandler();
		IUpdateModel model = null;
		if (control instanceof Text) {
			if (name != null && !JAXBControlUIConstants.ZEROSTR.equals(name)) {
				model = new TextUpdateModel(name, handler, (Text) control);
			}
		}

		if (name != null && !JAXBUIConstants.ZEROSTR.equals(name)) {
			maybeAddValidator(model, rmVarMap.get(name), tab.getParent());
		}
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
	 * @param rmVarMap
	 *            resource manager environment
	 * @param sources
	 *            map of widgets to check for state
	 * @param targets
	 *            map of widgets on which to set state
	 * 
	 * @return update model if not a label
	 */
	public static IUpdateModel createModel(Composite parent, WidgetType widget, JAXBDynamicLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<String, Button> sources, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(widget, rmVarMap);
		Control control = createControl(parent, cd, tab);

		String id = widget.getButtonId();
		if (id != null && control instanceof Button) {
			sources.put(id, (Button) control);
		}

		ControlStateType cst = widget.getControlState();
		if (cst != null) {
			targets.put(cst, control);
		}

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
			if (name != null && !JAXBControlUIConstants.ZEROSTR.equals(name)) {
				model = new TextUpdateModel(name, handler, (Text) control);
			}
			if (dynamic != null) {
				model = new TextUpdateModel(dynamic, handler, (Text) control);
			}
		} else if (control instanceof Combo) {
			model = new ComboUpdateModel(name, cd.itemsFrom, handler, (Combo) control);
		} else if (control instanceof Spinner) {
			model = new SpinnerUpdateModel(name, handler, (Spinner) control);
		} else if (control instanceof Button) {
			model = new ButtonUpdateModel(name, handler, (Button) control, cd.translateBooleanAs);
		}

		if (name != null && !JAXBUIConstants.ZEROSTR.equals(name)) {
			maybeAddValidator(model, rmVarMap.get(name), tab.getParent());
		}
		return model;
	}

	/**
	 * Constructs the cell editor and its update model.
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
		maybeAddValidator(model, data, tab.getParent());
		return model;
	}

	/**
	 * Constructs push-button which activates an external command. There is no
	 * model generated for this widget.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param button
	 *            JAXB data object describing the widget to which this model is
	 *            bound
	 * @param tab
	 *            launch tab being built
	 * @param rmVarMap
	 *            resource manager environment
	 * @param sources
	 *            map of widgets to check for state
	 * @param targets
	 *            map of widgets on which to set state
	 * 
	 */
	public static void createPushButton(Composite parent, PushButtonType button, JAXBDynamicLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(button, rmVarMap);
		Control c = createActionButton(parent, cd, tab);

		if (c != null) {
			if (!JAXBControlUIConstants.ZEROSTR.equals(cd.tooltip)) {
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

		ControlStateType cst = button.getControlState();
		if (cst != null) {
			targets.put(cst, c);
		}
	}

	/**
	 * Creates a push-button and connects it to the command through a listener.
	 * 
	 * @see org.eclipse.swt.widgets.Text
	 * @see org.eclipse.swt.widgets.Button
	 * 
	 * @param parent
	 * @param cd
	 * @param tab
	 * @return
	 */
	private static Control createActionButton(Composite parent, final ControlDescriptor cd,
			final JAXBDynamicLaunchConfigurationTab tab) {
		Button b = WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, SWT.PUSH, new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					tab.run(cd.action);
				} catch (Throwable t) {
					JAXBControlUIPlugin.log(t);
				}
			}
		});
		SWTUtil.setButtonDimensionHint(b);
		return null;
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
	 * @param sources
	 *            map of widgets to check for state
	 * @param targets
	 *            map of widgets on which to set state
	 * 
	 * @return the text widget carrying the browse selection
	 */
	private static Text createBrowse(final Composite parent, BrowseType d, final ControlDescriptor cd,
			final JAXBDynamicLaunchConfigurationTab tab, Map<ControlStateType, Control> targets) {
		final Text t = WidgetBuilderUtils.createText(parent, cd.style, cd.layoutData, cd.readOnly, JAXBControlUIConstants.ZEROSTR);

		Button b = WidgetBuilderUtils.createButton(parent, cd.subLayoutData, cd.title, SWT.NONE, new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				try {
					String initial = t.getText();
					URI uri = null;
					if (JAXBControlUIConstants.ZEROSTR.equals(initial)) {
						uri = tab.getParent().getDelegate().getRemoteHome();
					} else {
						uri = new URI(initial);
					}
					uri = RemoteUIServicesUtils.browse(parent.getShell(), uri, tab.getParent().getDelegate(), !cd.localOnly,
							cd.readOnly, cd.directory);
					if (uri != null) {
						if (cd.returnUri) {
							t.setText(uri.toString());
						} else {
							t.setText(uri.getPath());
						}
					} else {
						t.setText(JAXBControlUIConstants.ZEROSTR);
					}
				} catch (Throwable t) {
					JAXBControlUIPlugin.log(t);
				}
			}
		});

		SWTUtil.setButtonDimensionHint(b);

		if (!JAXBControlUIConstants.ZEROSTR.equals(cd.tooltip)) {
			t.setToolTipText(cd.tooltip);
		}
		if (cd.foreground != null) {
			t.setForeground(WidgetBuilderUtils.getColor(cd.foreground));
		}
		if (cd.background != null) {
			t.setBackground(WidgetBuilderUtils.getColor(cd.background));
		}
		if (cd.font != null) {
			t.setFont(WidgetBuilderUtils.getFont(cd.font));
		}

		ControlStateType cst = d.getTextControlState();
		if (cst != null) {
			targets.put(cst, t);
		}

		cst = d.getButtonControlState();
		if (cst != null) {
			targets.put(cst, b);
		}

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
		if (cd.choice != null) {
			items = cd.choice.split(JAXBControlUIConstants.CM);
		}
		if (items == null) {
			items = new String[0];
		} else if (items.length > 0) {
			items = WidgetBuilderUtils.normalizeComboItems(items);
		}
		return WidgetBuilderUtils.createCombo(parent, cd.style, cd.layoutData, items, JAXBControlUIConstants.ZEROSTR, cd.title,
				cd.tooltip, null);
	}

	/**
	 * 
	 * @param parent
	 *            to which the control belongs
	 * @param cd
	 *            control descriptor for the JAXB data element describing the
	 *            widget
	 * @param tab
	 *            launch tab being built
	 * @return the resulting control (<code>null</code> if the widget is a
	 *         Label).
	 */
	private static Control createControl(final Composite parent, ControlDescriptor cd, JAXBDynamicLaunchConfigurationTab tab) {
		Control c = null;
		if (JAXBControlUIConstants.LABEL.equals(cd.widgetType)) {
			c = WidgetBuilderUtils.createLabel(parent, cd.fixedText, cd.style, cd.layoutData);
		} else if (JAXBControlUIConstants.TEXT.equals(cd.widgetType)) {
			c = createText(parent, cd);
		} else if (JAXBControlUIConstants.RADIOBUTTON.equals(cd.widgetType)) {
			if (cd.style == SWT.NONE) {
				cd.style = SWT.RADIO;
			} else {
				cd.style |= SWT.RADIO;
			}
			c = WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, null);
		} else if (JAXBControlUIConstants.CHECKBOX.equals(cd.widgetType)) {
			if (cd.style == SWT.NONE) {
				cd.style = SWT.CHECK;
			} else {
				cd.style |= SWT.CHECK;
			}
			c = WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, null);
		} else if (JAXBControlUIConstants.SPINNER.equals(cd.widgetType)) {
			c = WidgetBuilderUtils.createSpinner(parent, cd.style, cd.layoutData, cd.title, cd.min, cd.max, cd.min, null);
		} else if (JAXBControlUIConstants.COMBO.equals(cd.widgetType)) {
			c = createCombo(parent, cd);
		}

		if (c != null) {
			if (!JAXBControlUIConstants.ZEROSTR.equals(cd.tooltip)) {
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
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.cell.SpinnerCellEditor
	 * 
	 * @param cd
	 *            internal object holding model description
	 * @param data
	 *            Property or Attribute
	 * @param parent
	 *            Table or Tree to which the editor belongs
	 * @return the editor of appropriate type
	 */
	private static CellEditor createEditor(CellDescriptor cd, Object data, Composite parent) {
		CellEditor editor = null;
		if (cd.type == CellEditorType.TEXT) {
			editor = new TextCellEditor(parent);
		} else if (cd.type == CellEditorType.CHECK) {
			editor = new CheckboxCellEditor(parent);
		} else if (cd.type == CellEditorType.SPINNER) {
			editor = new SpinnerCellEditor(parent, cd.min, cd.max);
		} else if (cd.type == CellEditorType.COMBO) {
			if (data instanceof AttributeType) {
				if (cd.choice != null) {
					cd.choice = cd.choice.trim();
					cd.items = cd.choice.split(JAXBControlUIConstants.CM);
				} else {
					cd.items = new String[0];
				}
			}
			editor = new ComboBoxCellEditor(parent, cd.items, SWT.READ_ONLY);
		}
		return editor;
	}

	/**
	 * Constructs CellEditor and model for table row.
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.TableRowUpdateModel
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
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom, cd.translateBooleanAs, cd.readOnly,
					(AttributeType) data);
		} else if (data instanceof PropertyType) {
			model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom, cd.translateBooleanAs, cd.readOnly);
		}
		if (model != null) {
			model.setBackground(cd.background);
			model.setFont(cd.font);
			model.setForeground(cd.foreground);
		}
		return model;
	}

	/**
	 * Constructs CellEditor and model for tree node.
	 * 
	 * @see org.eclipse.jface.viewers.CellEditor
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel
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
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom, cd.translateBooleanAs,
					cd.readOnly, inValueCol, (AttributeType) data);
		} else if (data instanceof PropertyType) {
			model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom, cd.translateBooleanAs,
					cd.readOnly, inValueCol);
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
		return WidgetBuilderUtils.createText(parent, cd.style, cd.layoutData, cd.readOnly, JAXBControlUIConstants.ZEROSTR);
	}

	/**
	 * Checks to see if there is a validator on the attribute and adds this to
	 * the model.
	 * 
	 * @param model
	 *            update model object to associate validator with
	 * @param data
	 *            JAXB property or attribute descriptor
	 * @param tab
	 *            launch tab being built
	 */
	private static void maybeAddValidator(IUpdateModel model, Object data, JAXBControllerLaunchConfigurationTab tab) {
		if (data != null && data instanceof AttributeType) {
			model.setValidator(((AttributeType) data).getValidator(), tab);
		}
	}
}
