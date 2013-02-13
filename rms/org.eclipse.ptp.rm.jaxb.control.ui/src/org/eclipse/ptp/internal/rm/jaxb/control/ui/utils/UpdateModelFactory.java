/*******************************************************************************
 * Copyright (c) 2011, 2012 University of Illinois.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 * 
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 * 	Jeff Overbey - Environment Manager support
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.utils;

import java.lang.reflect.Constructor;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.CheckboxCellEditor;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.ComboBoxCellEditor;
import org.eclipse.jface.viewers.ICheckable;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.core.util.CoreExceptionUtils;
import org.eclipse.ptp.internal.rm.jaxb.control.core.JAXBControlConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIPlugin;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.cell.SpinnerCellEditor;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBParentLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ButtonGroupUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ButtonUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ComboUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.SpinnerUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.TableRowUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.TextUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.ui.RemoteUIServicesUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IValidator;
import org.eclipse.ptp.rm.jaxb.control.ui.IWidgetDescriptor;
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
import org.eclipse.ptp.rm.jaxb.core.data.PushButtonType;
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
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
 * Utilities for creating update models for control widgets, cell editors and viewers.
 * 
 * @author arossi
 * @author Jeff Overbey - Environment Manager support
 */
public class UpdateModelFactory {

	/**
	 * Internal data object for configuration convenience.
	 * 
	 * @author arossi
	 * 
	 */
	private static class CellDescriptor {
		private final String name;
		private String tooltip;
		private String description;
		private String choice;
		private final String itemsFrom;
		private final String translateBooleanAs;
		private final Integer min;
		private final Integer max;
		private final boolean readOnly;
		private final CellEditorType type;
		private String[] items;
		protected Color[] foreground;
		protected Color[] background;
		protected Font[] font;

		/**
		 * @param data
		 *            Attribute
		 * @param columnData
		 *            list of JAXB data elements describing the viewer columns
		 */
		private CellDescriptor(AttributeType data, List<ColumnDataType> columnData) {
			type = CellEditorType.getType(data);
			name = data.getName();
			choice = data.getChoice();
			itemsFrom = data.getItemsFrom();
			translateBooleanAs = data.getTranslateBooleanAs();
			min = data.getMin();
			max = data.getMax();
			readOnly = data.isReadOnly();
			tooltip = data.getTooltip();
			if (tooltip == null) {
				tooltip = JAXBControlUIConstants.ZEROSTR;
			} else {
				tooltip = WidgetBuilderUtils.removeTabOrLineBreak(tooltip);
			}
			description = data.getDescription();
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
		 * Determine the CellEditor from the Property or Attribute type or value.
		 * 
		 * @param object
		 *            Property or Attribute
		 * @return enum value for editor
		 */
		public static CellEditorType getType(Object object) {
			if (object instanceof AttributeType) {
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
		 * If the Property or Attribute carries an explicit type name, determine CellEditor type from it.
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
	private static class ControlDescriptor implements IWidgetDescriptor {
		private final IJAXBLaunchConfigurationTab tab;
		private String widgetType;
		private String typeId;
		private String title;
		private Object layoutData;
		private Object subLayoutData;
		private boolean readOnly;
		private boolean localOnly;
		private boolean directory;
		private boolean returnUri;
		private int style;
		private Color background;
		private Color foreground;
		private Font font;
		private Integer min;
		private Integer max;
		private String tooltip;
		private String choice;
		private String fixedText;
		private ButtonActionType action;
		private String itemsFrom;
		private String translateBooleanAs;

		private ControlDescriptor(BrowseType browse, IVariableMap rmMap, IJAXBLaunchConfigurationTab tab) {
			this.tab = tab;
			setControlData(browse);
			setMapDependentData(browse, rmMap);
		}

		private ControlDescriptor(PushButtonType button, IVariableMap rmMap, IJAXBLaunchConfigurationTab tab) {
			this.tab = tab;
			setControlData(button);
			setMapDependentData(button, rmMap);
		}

		/**
		 * @param widget
		 *            JAXB data element describing widget
		 * @param rmMap
		 *            used to retrieve data object info
		 */
		private ControlDescriptor(WidgetType widget, IVariableMap rmMap, IJAXBLaunchConfigurationTab tab) {
			this.tab = tab;
			setControlData(widget);
			setMapDependentData(widget, rmMap);
			/*
			 * NOTE: this will override the attribute field, so check for null first
			 */
			String s = widget.getItemsFrom();
			if (s != null) {
				itemsFrom = s;
			}
		}

		/**
		 * Configure the fields which do not depend on the underlying data object.
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
			String background = browse.getBackground();
			if (background != null) {
				this.background = WidgetBuilderUtils.getColor(background);
			}
			String foreground = browse.getForeground();
			if (foreground != null) {
				this.foreground = WidgetBuilderUtils.getColor(foreground);
			}
			FontType font = browse.getFont();
			if (font != null) {
				this.font = WidgetBuilderUtils.getFont(font);
			}
			tooltip = browse.getTooltip();
		}

		/**
		 * Configure the fields which do not depend on the underlying data object.
		 * 
		 * @param button
		 *            JAXB data element describing widget
		 */
		private void setControlData(PushButtonType button) {
			widgetType = JAXBControlUIConstants.ACTION;
			title = button.getTitle();
			LayoutDataType layout = button.getLayoutData();
			layoutData = LaunchTabBuilder.createLayoutData(layout);
			String background = button.getBackground();
			if (background != null) {
				this.background = WidgetBuilderUtils.getColor(background);
			}
			String foreground = button.getForeground();
			if (foreground != null) {
				this.foreground = WidgetBuilderUtils.getColor(foreground);
			}
			FontType font = button.getFont();
			if (font != null) {
				this.font = WidgetBuilderUtils.getFont(font);
			}
			tooltip = button.getTooltip();
			action = button.getButtonAction();
		}

		/**
		 * Configure the fields which do not depend on the underlying data object.
		 * 
		 * @param widget
		 *            JAXB data element describing widget
		 */
		private void setControlData(WidgetType widget) {
			widgetType = widget.getType();
			typeId = widget.getTypeId();
			title = widget.getTitle();
			LayoutDataType layout = widget.getLayoutData();
			layoutData = LaunchTabBuilder.createLayoutData(layout);
			style = WidgetBuilderUtils.getStyle(widget.getStyle());
			readOnly = widget.isReadOnly();
			if (readOnly) {
				style |= SWT.READ_ONLY;
			}
			String background = widget.getBackground();
			if (background != null) {
				this.background = WidgetBuilderUtils.getColor(background);
			}
			String foreground = widget.getForeground();
			if (foreground != null) {
				this.foreground = WidgetBuilderUtils.getColor(foreground);
			}
			FontType font = widget.getFont();
			if (font != null) {
				this.font = WidgetBuilderUtils.getFont(font);
			}
			tooltip = widget.getTooltip();
			fixedText = widget.getFixedText();
		}

		/**
		 * Get the choice (Combo), min and max (Spinner) settings.
		 * 
		 * @param data
		 *            or Property
		 */
		private void setData(AttributeType data) {
			AttributeType a = data;
			choice = a.getChoice();
			itemsFrom = a.getItemsFrom();
			min = a.getMin();
			if (min == null) {
				min = 0;
			}
			max = a.getMax();
			if (max == null) {
				max = Integer.MAX_VALUE;
			}
			translateBooleanAs = a.getTranslateBooleanAs();
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

			String attr = null;
			if (widget instanceof WidgetType) {
				attr = ((WidgetType) widget).getAttribute();
			} else if (widget instanceof BrowseType) {
				attr = ((BrowseType) widget).getAttribute();
			}

			if (attr != null) {
				AttributeType data = rmMap.get(attr);
				if (data != null) {
					setData(data);
				}
			}
		}

		public Color getBackground() {
			return background;
		}

		public String getChoice() {
			return choice;
		}

		public String getFixedText() {
			return fixedText;
		}

		public Font getFont() {
			return font;
		}

		public Color getForeground() {
			return foreground;
		}

		public String getItemsFrom() {
			return itemsFrom;
		}

		public Object getLayoutData() {
			return layoutData;
		}

		public Integer getMax() {
			return max;
		}

		public Integer getMin() {
			return min;
		}

		public boolean getReadOnly() {
			return readOnly;
		}

		public IRemoteConnection getRemoteConnection() {
			return tab.getRemoteConnection();
		}

		public String getTitle() {
			return title;
		}

		public String getToolTipText() {
			return tooltip;
		}

		public String getTranslateBooleanAs() {
			return translateBooleanAs;
		}

		public String getType() {
			return widgetType;
		}

		public String getTypeId() {
			return typeId;
		}

		public boolean isReadOnly() {
			return readOnly;
		}

		public int getStyle() {
			return style;
		}

	}

	/**
	 * Extension-based instantiation for custom control.
	 * 
	 * @param type
	 *            extension name
	 * @return the control instance
	 * @throws CoreException
	 */
	private static Control createWidget(IWidgetDescriptor wd, Composite parent) throws CoreException {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(JAXBControlUIPlugin.PLUGIN_ID,
				JAXBControlUIConstants.WIDGET_EXT_PT);
		IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			try {
				if (element.getAttribute(JAXBControlConstants.ID).equals(wd.getTypeId())) {
					String widgetClass = element.getAttribute(JAXBControlUIConstants.WIDGETCLASS);
					Class<?> cls = Platform.getBundle(element.getDeclaringExtension().getContributor().getName()).loadClass(
							widgetClass);
					Constructor<?> cons = cls.getConstructor(Composite.class, IWidgetDescriptor.class);
					return (Control) cons.newInstance(parent, wd);
				}
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.WidgetInstantiationError + wd.getType(), t);
			}
		}
		return null;
	}

	/**
	 * Extension-based instantiation for custom control's update model.
	 * 
	 * @param type
	 *            extension name
	 * @return the control instance
	 * @throws CoreException
	 */
	private static IUpdateModel createModel(IWidgetDescriptor wd, String attr, IUpdateHandler handler, Control control)
			throws CoreException {
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(JAXBControlUIPlugin.PLUGIN_ID,
				JAXBControlUIConstants.WIDGET_EXT_PT);
		IConfigurationElement[] elements = extensionPoint.getConfigurationElements();
		for (IConfigurationElement element : elements) {
			try {
				if (element.getAttribute(JAXBControlConstants.ID).equals(wd.getTypeId())) {
					String updateModelClass = element.getAttribute(JAXBControlUIConstants.UPDATEMODELCLASS);
					Class<?> cls = Platform.getBundle(element.getDeclaringExtension().getContributor().getName()).loadClass(
							updateModelClass);
					Constructor<?> cons = cls.getConstructor(String.class, IUpdateHandler.class, Control.class);
					return (IUpdateModel) cons.newInstance(attr, handler, control);
				}
			} catch (Throwable t) {
				throw CoreExceptionUtils.newException(Messages.WidgetInstantiationError + wd.getType(), t);
			}
		}
		return null;
	}

	/**
	 * Constructs button group update model.
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ButtonGroupUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler
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
	public static IUpdateModel createModel(ButtonGroupType bGroupDescriptor, Composite bGroup, IJAXBLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<String, Button> sources, Map<ControlStateType, Control> targets) {
		List<WidgetType> bWidgets = bGroupDescriptor.getButton();
		List<Button> buttons = new ArrayList<Button>();
		for (WidgetType widget : bWidgets) {
			ControlDescriptor cd = new ControlDescriptor(widget, rmVarMap, tab);
			Control control = createControl(bGroup, cd);
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
		return new ButtonGroupUpdateModel(bGroupDescriptor.getAttribute(), tab.getParent().getUpdateHandler(), bGroup, buttons);
	}

	/**
	 * Constructs the viewer update model.
	 * 
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ViewerUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateHandler
	 * 
	 * @param viewer
	 *            SWT object to which this model is bound
	 * @param descriptor
	 *            JAXB data element describing the viewer
	 * @param tab
	 *            launch tab being built (accessed for value handler)
	 * @return
	 */
	public static ViewerUpdateModel createModel(ColumnViewer viewer, AttributeViewerType descriptor, IJAXBLaunchConfigurationTab tab) {
		return new ViewerUpdateModel(descriptor.getName(), descriptor.isInitialAllChecked(), tab.getParent().getUpdateHandler(),
				(ICheckable) viewer, descriptor.getValue());
	}

	/**
	 * Constructs browse widget pair.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param browse
	 *            JAXB data object describing the widget to which this model is bound
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
	public static IUpdateModel createModel(Composite parent, BrowseType browse, IJAXBLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(browse, rmVarMap, tab);

		Control control = createBrowse(parent, browse, cd, tab, targets);

		String attr = browse.getAttribute();
		IUpdateHandler handler = tab.getParent().getUpdateHandler();
		IUpdateModel model = null;
		if (control instanceof Text) {
			if (attr != null && !JAXBControlUIConstants.ZEROSTR.equals(attr)) {
				model = new TextUpdateModel(attr, handler, (Text) control);
			}
		}

		if (attr != null && !JAXBUIConstants.ZEROSTR.equals(attr)) {
			maybeAddValidator(model, rmVarMap.get(attr), tab.getParent());
		}
		return model;
	}

	/**
	 * Constructs the widget and its update model.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param widget
	 *            JAXB data object describing the widget to which this model is bound
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
	public static IUpdateModel createModel(Composite parent, WidgetType widget, IJAXBLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<String, Button> sources, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(widget, rmVarMap, tab);
		Control control = createControl(parent, cd);

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

		String attr = widget.getAttribute();
		List<ArgType> dynamic = null;

		WidgetType.DynamicText dt = widget.getDynamicText();
		if (dt != null) {
			dynamic = dt.getArg();
		}

		IUpdateHandler handler = tab.getParent().getUpdateHandler();
		IUpdateModel model = null;
		if (JAXBControlUIConstants.TEXT.equals(cd.getType())) {
			if (attr != null && !JAXBControlUIConstants.ZEROSTR.equals(attr)) {
				model = new TextUpdateModel(attr, handler, (Text) control);
			}
			if (dynamic != null) {
				model = new TextUpdateModel(dynamic, handler, (Text) control);
			}
		} else if (JAXBControlUIConstants.COMBO.equals(cd.getType())) {
			model = new ComboUpdateModel(attr, cd.itemsFrom, handler, (Combo) control);
		} else if (JAXBControlUIConstants.SPINNER.equals(cd.getType())) {
			model = new SpinnerUpdateModel(attr, handler, (Spinner) control);
		} else if (JAXBControlUIConstants.RADIOBUTTON.equals(cd.getType()) || JAXBControlUIConstants.CHECKBOX.equals(cd.getType())) {
			model = new ButtonUpdateModel(attr, handler, (Button) control, cd.translateBooleanAs);
		} else if (JAXBControlUIConstants.CUSTOM.equals(cd.getType())) {
			try {
				model = createModel(cd, attr, handler, control);
			} catch (CoreException e) {
				// Will be missing from UI
			}
		}

		if (attr != null && !JAXBUIConstants.ZEROSTR.equals(attr)) {
			maybeAddValidator(model, rmVarMap.get(attr), tab.getParent());
		}
		return model;
	}

	/**
	 * Constructs the cell editor and its update model.
	 * 
	 * @param attr
	 *            Attribute for this viewer row.
	 * @param viewer
	 *            CheckboxTableViewer or CheckboxTreeViewer
	 * @param columnData
	 *            list of JAXB data elements describing the viewer columns
	 * @param tab
	 *            launch tab being built
	 * @return
	 */
	public static ICellEditorUpdateModel createModel(AttributeType attr, ColumnViewer viewer, List<ColumnDataType> columnData,
			IJAXBLaunchConfigurationTab tab) {
		ICellEditorUpdateModel model = null;
		if (viewer instanceof TableViewer) {
			model = createModel(attr, (TableViewer) viewer, columnData, tab);
		} else {
			model = createModel(attr, (TreeViewer) viewer, columnData, tab);
		}
		maybeAddValidator(model, attr, tab.getParent());
		return model;
	}

	/**
	 * Constructs push-button which activates an external command. There is no model generated for this widget.
	 * 
	 * @param parent
	 *            control to which the widget belongs
	 * @param button
	 *            JAXB data object describing the widget to which this model is bound
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
	public static void createPushButton(Composite parent, PushButtonType button, IJAXBLaunchConfigurationTab tab,
			IVariableMap rmVarMap, Map<ControlStateType, Control> targets) {
		ControlDescriptor cd = new ControlDescriptor(button, rmVarMap, tab);
		Control c = createActionButton(parent, cd, tab);

		if (c != null) {
			if (!JAXBControlUIConstants.ZEROSTR.equals(cd.tooltip)) {
				c.setToolTipText(cd.tooltip);
			}
			if (cd.foreground != null) {
				c.setForeground(cd.foreground);
			}
			if (cd.background != null) {
				c.setBackground(cd.background);
			}
			if (cd.font != null) {
				c.setFont(cd.font);
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
	private static Control createActionButton(Composite parent, final ControlDescriptor cd, final IJAXBLaunchConfigurationTab tab) {
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
	 * Creates a read-only Text area and adjacent browse button using remote connection information. The update model will be
	 * attached to the returned text widget, as its text value will trigger updates.
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
			final IJAXBLaunchConfigurationTab tab, Map<ControlStateType, Control> targets) {
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
						uri = tab.getParent().getRemoteServicesDelegate().getRemoteHome();
					} else {
						uri = new URI(initial);
					}
					uri = RemoteUIServicesUtils.browse(parent.getShell(), uri, tab.getParent().getRemoteServicesDelegate(),
							!cd.localOnly, cd.readOnly, cd.directory);
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
			t.setForeground(cd.foreground);
		}
		if (cd.background != null) {
			t.setBackground(cd.background);
		}
		if (cd.font != null) {
			t.setFont(cd.font);
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
	 *            control descriptor for the JAXB data element describing the widget
	 * @return the resulting control (<code>null</code> if the widget is a Label).
	 */
	private static Control createControl(final Composite parent, ControlDescriptor cd) {
		Control c = null;
		if (JAXBControlUIConstants.LABEL.equals(cd.getType())) {
			c = WidgetBuilderUtils.createLabel(parent, cd.fixedText, cd.style, cd.layoutData);
		} else if (JAXBControlUIConstants.TEXT.equals(cd.getType())) {
			c = createText(parent, cd);
		} else if (JAXBControlUIConstants.RADIOBUTTON.equals(cd.getType())) {
			if (cd.style == SWT.NONE) {
				cd.style = SWT.RADIO;
			} else {
				cd.style |= SWT.RADIO;
			}
			c = WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, null);
		} else if (JAXBControlUIConstants.CHECKBOX.equals(cd.getType())) {
			if (cd.style == SWT.NONE) {
				cd.style = SWT.CHECK;
			} else {
				cd.style |= SWT.CHECK;
			}
			c = WidgetBuilderUtils.createButton(parent, cd.layoutData, cd.title, cd.style, null);
		} else if (JAXBControlUIConstants.SPINNER.equals(cd.getType())) {
			c = WidgetBuilderUtils.createSpinner(parent, cd.style, cd.layoutData, cd.title, cd.min, cd.max, cd.min, null);
		} else if (JAXBControlUIConstants.COMBO.equals(cd.getType())) {
			c = createCombo(parent, cd);
		} else if (JAXBControlUIConstants.CUSTOM.equals(cd.getType())) {
			try {
				c = createWidget(cd, parent);
				c.setLayoutData(cd.layoutData);
			} catch (CoreException e) {
				// Widget will be missing from UI
			}
		}

		if (c != null) {
			if (!JAXBControlUIConstants.ZEROSTR.equals(cd.tooltip)) {
				c.setToolTipText(cd.tooltip);
			}
			if (cd.foreground != null) {
				c.setForeground(cd.foreground);
			}
			if (cd.background != null) {
				c.setBackground(cd.background);
			}
			if (cd.font != null) {
				c.setFont(cd.font);
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
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.cell.SpinnerCellEditor
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
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.TableRowUpdateModel
	 * 
	 * @param attr
	 *            Attribute
	 * @param viewer
	 *            to which this row belongs
	 * @param columnData
	 *            list of JAXB data elements describing table columns
	 * @param tab
	 *            launch tab being built
	 * @return the cell editor update model, which contains a reference to the CellEditor
	 */
	private static ICellEditorUpdateModel createModel(AttributeType attr, TableViewer viewer, List<ColumnDataType> columnData,
			IJAXBLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(attr, columnData);
		CellEditor editor = createEditor(cd, attr, viewer.getTable());
		IUpdateHandler handler = tab.getParent().getUpdateHandler();
		ICellEditorUpdateModel model = new TableRowUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom,
				cd.translateBooleanAs, cd.readOnly, attr);
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
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel
	 * 
	 * @param attr
	 *            Attribute
	 * @param viewer
	 *            to which this row belongs
	 * @param columnData
	 *            list of JAXB data elements describing table columns
	 * @param tab
	 *            launch tab being built
	 * @return the cell editor update model, which contains a reference to the CellEditor
	 */
	private static ICellEditorUpdateModel createModel(AttributeType attr, TreeViewer viewer, List<ColumnDataType> columnData,
			IJAXBLaunchConfigurationTab tab) {
		CellDescriptor cd = new CellDescriptor(attr, columnData);
		CellEditor editor = createEditor(cd, attr, viewer.getTree());
		IUpdateHandler handler = tab.getParent().getUpdateHandler();
		Object[] properties = viewer.getColumnProperties();
		boolean inValueCol = properties.length == 2;
		ICellEditorUpdateModel model = new ValueTreeNodeUpdateModel(cd.name, handler, editor, cd.items, cd.itemsFrom,
				cd.translateBooleanAs, cd.readOnly, inValueCol, attr);
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
	 * Checks to see if there is a validator on the attribute and adds this to the model.
	 * 
	 * @param model
	 *            update model object to associate validator with
	 * @param attr
	 *            JAXB attribute descriptor
	 * @param tab
	 *            launch tab being built
	 */
	private static void maybeAddValidator(IUpdateModel model, final AttributeType attr, final IJAXBParentLaunchConfigurationTab tab) {
		if (attr != null) {
			IValidator validator = new IValidator() {

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ptp.rm.jaxb.control.ui.IValidator#validate(java.lang.Object)
				 */
				public Object validate(Object value) throws Exception {
					WidgetActionUtils.validate(String.valueOf(value), attr.getValidator(), tab.getRemoteServicesDelegate()
							.getRemoteFileManager());
					return value;
				}

				/*
				 * (non-Javadoc)
				 * 
				 * @see org.eclipse.ptp.rm.jaxb.control.ui.IValidator#getErrorMessage()
				 */
				public String getErrorMessage() {
					return attr.getValidator().getErrorMessage();
				}

			};
			model.setValidator(validator);
		}
	}
}
