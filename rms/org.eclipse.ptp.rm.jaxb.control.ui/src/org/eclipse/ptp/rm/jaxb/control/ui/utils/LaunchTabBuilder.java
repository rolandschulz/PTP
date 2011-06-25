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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonGroupType;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnDataType;
import org.eclipse.ptp.rm.jaxb.core.data.CompositeType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateRuleType;
import org.eclipse.ptp.rm.jaxb.core.data.ControlStateType;
import org.eclipse.ptp.rm.jaxb.core.data.FillLayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.FontType;
import org.eclipse.ptp.rm.jaxb.core.data.FormAttachmentType;
import org.eclipse.ptp.rm.jaxb.core.data.FormDataType;
import org.eclipse.ptp.rm.jaxb.core.data.FormLayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.GridDataType;
import org.eclipse.ptp.rm.jaxb.core.data.GridLayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataType;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.PropertyType;
import org.eclipse.ptp.rm.jaxb.core.data.RowDataType;
import org.eclipse.ptp.rm.jaxb.core.data.RowLayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderType;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemType;
import org.eclipse.ptp.rm.jaxb.core.data.ViewerItemsType;
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
import org.eclipse.ptp.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.Widget;

/**
 * Object responsible for constructing the configurable JAXB Launch
 * Configuration Tab, as well as building and registering the related model
 * objects and listeners.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.ui.launch.JAXBDynamicLaunchConfigurationTab
 * 
 * @author arossi
 * 
 */
public class LaunchTabBuilder {

	/**
	 * Create the layout data object. Calls
	 * {@link WidgetBuilderUtils#createRowData(Integer, Integer, Boolean)},
	 * {@link WidgetBuilderUtils#createGridData(Integer, Boolean, Boolean, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer)}
	 * , or
	 * {@link WidgetBuilderUtils#createFormAttachment(String, Integer, Integer, Integer)}
	 * and
	 * {@link WidgetBuilderUtils#createFormData(Integer, Integer, FormAttachment, FormAttachment, FormAttachment, FormAttachment)}
	 * .
	 * 
	 * @param layoutData
	 *            JAXB data element describing the layout data object
	 * @return the data object
	 */
	static Object createLayoutData(LayoutDataType layoutData) {
		if (layoutData != null) {
			if (layoutData.getRowData() != null) {
				RowDataType rowData = layoutData.getRowData();
				return WidgetBuilderUtils.createRowData(rowData.getHeight(), rowData.getWidth(), rowData.isExclude());
			} else if (layoutData.getGridData() != null) {
				GridDataType gridData = layoutData.getGridData();
				int style = WidgetBuilderUtils.getStyle(gridData.getStyle());
				int hAlign = WidgetBuilderUtils.getStyle(gridData.getHorizontalAlign());
				int vAlign = WidgetBuilderUtils.getStyle(gridData.getVerticalAlign());
				return WidgetBuilderUtils.createGridData(style, gridData.isGrabExcessHorizontal(), gridData.isGrabExcessVertical(),
						gridData.getWidthHint(), gridData.getHeightHint(), gridData.getMinWidth(), gridData.getMinHeight(),
						gridData.getHorizontalSpan(), gridData.getVerticalSpan(), hAlign, vAlign, gridData.getHorizontalIndent(),
						gridData.getVerticalIndent());
			} else if (layoutData.getFormData() != null) {
				FormDataType formData = layoutData.getFormData();
				FormAttachment top = null;
				FormAttachment bottom = null;
				FormAttachment left = null;
				FormAttachment right = null;
				FormAttachmentType fad = formData.getTop();
				if (fad != null) {
					top = WidgetBuilderUtils.createFormAttachment(fad.getAlignment(), fad.getDenominator(), fad.getNumerator(),
							fad.getOffset());
				}
				fad = formData.getBottom();
				if (fad != null) {
					bottom = WidgetBuilderUtils.createFormAttachment(fad.getAlignment(), fad.getDenominator(), fad.getNumerator(),
							fad.getOffset());
				}
				fad = formData.getLeft();
				if (fad != null) {
					left = WidgetBuilderUtils.createFormAttachment(fad.getAlignment(), fad.getDenominator(), fad.getNumerator(),
							fad.getOffset());
				}
				fad = formData.getRight();
				if (fad != null) {
					right = WidgetBuilderUtils.createFormAttachment(fad.getAlignment(), fad.getDenominator(), fad.getNumerator(),
							fad.getOffset());
				}
				return WidgetBuilderUtils.createFormData(formData.getHeight(), formData.getWidth(), top, bottom, left, right);
			}
		}
		return null;
	}

	/**
	 * @param data
	 *            Attribute or Property
	 * @return whether it is visible to the user (and thus used to generate a
	 *         widget and update model)
	 */
	private static boolean isVisible(Object data) {
		if (data instanceof AttributeType) {
			return ((AttributeType) data).isVisible();
		} else if (data instanceof PropertyType) {
			return ((PropertyType) data).isVisible();
		}
		return false;
	}

	private final JAXBDynamicLaunchConfigurationTab tab;
	private final IVariableMap rmVarMap;
	private final Map<Object, IUpdateModel> localWidgets;

	/*
	 * temporary maps for wiring the widgets
	 */
	private final Map<String, Control> sources;
	private final Map<ControlStateType, Control> targets;

	/**
	 * @param tab
	 *            whose control is configurable from the JAXB data tree
	 */
	public LaunchTabBuilder(JAXBDynamicLaunchConfigurationTab tab) throws Throwable {
		this.tab = tab;
		this.localWidgets = tab.getLocalWidgets();
		this.rmVarMap = tab.getParent().getRmConfig().getRMVariableMap();
		sources = new HashMap<String, Control>();
		targets = new HashMap<ControlStateType, Control>();
	}

	/**
	 * Root call to build the SWT widget tree. Calls
	 * {@link #addComposite(CompositeDescriptor, Composite)}<br>
	 * Clears the widgets map, in the case or reinitializaton. <br>
	 * <br>
	 * This contents of this element are essentially the same as Composite.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.data.CompositeDescriptor
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabFolderDescriptor
	 * 
	 * @param parent
	 *            control of the launch tab
	 * @return top-level composite control
	 * @throws Throwable
	 */
	public Composite build(Composite parent) throws Throwable {
		localWidgets.clear();
		TabControllerType top = tab.getController();
		Layout layout = createLayout(top.getLayout());
		Object data = createLayoutData(top.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(top.getStyle());

		Composite composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);

		addChildren(top.getTabFolderOrCompositeOrWidget(), composite);

		String attr = top.getBackground();
		if (attr != null) {
			composite.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontType fd = top.getFont();
		if (fd != null) {
			composite.setFont(WidgetBuilderUtils.getFont(fd));
		}

		maybeWireWidgets();
		return composite;
	}

	/**
	 * Constructs the viewer, its row items and their update models, and adds it
	 * to the tree. Calls
	 * {@link UpdateModelFactory#createModel(ColumnViewer, AttributeViewer, JAXBDynamicLaunchConfigurationTab)}
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.ViewerUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.utils.UpdateModelFactory#createModel(ColumnViewer,
	 *      AttributeViewer, JAXBDynamicLaunchConfigurationTab)
	 * 
	 * @param descriptor
	 *            JAXB data element describing CheckboxTableViewer or
	 *            CheckboxTreeViewer for displaying a subset of the resource
	 *            manager properties and attributes
	 * @param parent
	 *            control to which to add the viewer
	 */
	private void addAttributeViewer(AttributeViewerType descriptor, Composite parent) {
		Layout layout = createLayout(descriptor.getLayout());
		Object data = createLayoutData(descriptor.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Button showHide = WidgetBuilderUtils.createCheckButton(parent, Messages.ToggleShowHideSelectedAttributes, null);
		ColumnViewer viewer = null;
		if (JAXBControlUIConstants.TABLE.equals(descriptor.getType())) {
			viewer = addCheckboxTableViewer(parent, data, layout, style, descriptor);
		} else if (JAXBControlUIConstants.TREE.equals(descriptor.getType())) {
			viewer = addCheckboxTreeViewer(parent, data, layout, style, descriptor);
		}
		if (viewer != null) {
			Collection<ICellEditorUpdateModel> rows = addRows(viewer, descriptor);
			ViewerUpdateModel model = UpdateModelFactory.createModel(viewer, descriptor, tab);
			for (ICellEditorUpdateModel row : rows) {
				row.setViewer(model);
			}
			viewer.setInput(rows);
			showHide.addSelectionListener(model);
			model.setShowAll(showHide);
			localWidgets.put(viewer, model);

			String id = descriptor.getId();
			if (id != null) {
				sources.put(id, viewer.getControl());
			}

			ControlStateType cst = descriptor.getControlState();
			if (cst != null) {
				targets.put(cst, viewer.getControl());
			}
		}
	}

	/**
	 * Constructs a button group and its model, and adds it to the tree. Calls
	 * {@link UpdateModelFactory#createModel(Group, List, String, JAXBDynamicLaunchConfigurationTab, IVariableMap)}
	 * 
	 * @see UpdateModelFactory#createModel(Group, List, String,
	 *      JAXBDynamicLaunchConfigurationTab, IVariableMap)
	 * @param descriptor
	 *            JAXB data element description the button group
	 * @param parent
	 *            control to which to add the viewer
	 */
	private void addButtonGroup(ButtonGroupType descriptor, Composite parent) {
		Layout layout = createLayout(descriptor.getLayout());
		Object data = createLayoutData(descriptor.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Composite control = null;
		if (descriptor.isGroup()) {
			control = WidgetBuilderUtils.createGroup(parent, style, layout, data, descriptor.getTitle());
		} else {
			control = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		}
		String tooltip = descriptor.getTooltip();
		if (tooltip != null) {
			control.setToolTipText(rmVarMap.getString(tooltip));
		}
		IUpdateModel model = UpdateModelFactory.createModel(descriptor, control, tab, rmVarMap);
		if (model != null) {
			localWidgets.put(control, model);
		}

		String id = descriptor.getId();
		if (id != null) {
			sources.put(id, control);
		}

		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, control);
		}
	}

	/**
	 * Constructs and configures the SWT TableViewer. Calls
	 * {@link WidgetBuilderUtils#createTable(Composite, Integer, Object)},
	 * {@link WidgetBuilderUtils#setupAttributeTable(CheckboxTableViewer, List, org.eclipse.jface.viewers.ISelectionChangedListener, boolean, boolean, boolean, boolean)}
	 * 
	 * @see org.eclipse.jface.viewers.CheckboxTableViewer
	 * 
	 * @param parent
	 *            control to which to add the viewer
	 * @param data
	 *            underlying data object (Property or Attribute)
	 * @param layout
	 *            SWT layout applicable to the viewer
	 * @param style
	 *            of the viewer
	 * @param descriptor
	 *            JAXB data element describing CheckboxTableViewer or
	 *            CheckboxTreeViewer for displaying a subset of the resource
	 *            manager properties and attributes
	 * @return the viewer
	 */
	private CheckboxTableViewer addCheckboxTableViewer(Composite parent, Object data, Layout layout, int style,
			AttributeViewerType descriptor) {
		style |= (SWT.CHECK | SWT.FULL_SELECTION);
		Table t = WidgetBuilderUtils.createTable(parent, style, data);
		CheckboxTableViewer viewer = new CheckboxTableViewer(t);
		ControlWidgetBuilderUtils.setupAttributeTable(viewer, descriptor.getColumnData(), null, descriptor.isSort(),
				descriptor.isTooltipEnabled(), descriptor.isHeaderVisible(), descriptor.isLinesVisible());
		return viewer;
	}

	/**
	 * Constructs and configures the SWT TreeViewer. Calls
	 * {@link WidgetBuilderUtils#createTree(Composite, Integer, Object)},
	 * {@link WidgetBuilderUtils#setupAttributeTree(CheckboxTreeViewer, List, org.eclipse.jface.viewers.ISelectionChangedListener, boolean, boolean, boolean, boolean)}
	 * 
	 * @see org.eclipse.jface.viewers.CheckboxTreeViewer
	 * 
	 * @param parent
	 *            control to which to add the viewer
	 * @param data
	 *            underlying data object (Property or Attribute)
	 * @param layout
	 *            SWT layout applicable to the viewer
	 * @param style
	 *            of the viewer
	 * @param descriptor
	 *            JAXB data element describing CheckboxTableViewer or
	 *            CheckboxTreeViewer for displaying a subset of the resource
	 *            manager properties and attributes
	 * @return the viewer
	 */
	private CheckboxTreeViewer addCheckboxTreeViewer(Composite parent, Object data, Layout layout, int style,
			AttributeViewerType descriptor) {
		style |= (SWT.CHECK | SWT.FULL_SELECTION);
		Tree t = WidgetBuilderUtils.createTree(parent, style, data);
		CheckboxTreeViewer viewer = new CheckboxTreeViewer(t);
		ControlWidgetBuilderUtils.setupAttributeTree(viewer, descriptor.getColumnData(), null, descriptor.isSort(),
				descriptor.isTooltipEnabled(), descriptor.isHeaderVisible(), descriptor.isLinesVisible());
		return viewer;
	}

	/**
	 * Iterates through list and calls add for the type. Called by
	 * {@link #addComposite(CompositeType, Composite)},
	 * {@link #addItem(CTabFolder, TabItemType)},
	 * 
	 * @param children
	 *            list of types
	 */
	private void addChildren(List<Object> children, Composite control) {
		for (Object o : children) {
			if (o instanceof TabFolderType) {
				addFolder((TabFolderType) o, control);
			} else if (o instanceof CompositeType) {
				addComposite((CompositeType) o, control);
			} else if (o instanceof WidgetType) {
				addWidget(control, (WidgetType) o);
			} else if (o instanceof ButtonGroupType) {
				addButtonGroup((ButtonGroupType) o, control);
			} else if (o instanceof AttributeViewerType) {
				addAttributeViewer((AttributeViewerType) o, control);
			}
		}
	}

	/**
	 * Adds an SWT Composite widget. Calls
	 * {@link WidgetBuilderUtils#createComposite(Composite, Integer, Layout, Object)}
	 * ,
	 * {@link WidgetBuilderUtils#createGroup(Composite, Integer, Layout, Object, String)}
	 * , {@link #addComposite(CompositeType, Composite)},
	 * {@link #addWidget(Composite, Widget)}, or
	 * {@link #addAttributeViewer(AttributeViewer, Composite)}.
	 * 
	 * @see org.eclipse.swt.widgets.Composite
	 * @see org.eclipse.swt.widgets.Group
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabFolderType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.CompositeType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.Widget
	 * @see org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer
	 * 
	 * @param descriptor
	 *            JAXB data element describing the contents and layout of the
	 *            composite
	 * @param parent
	 *            control to which to add the composite
	 * @return the SWT Composite
	 */
	private Composite addComposite(CompositeType descriptor, Composite parent) {
		Layout layout = createLayout(descriptor.getLayout());
		Object data = createLayoutData(descriptor.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());

		Composite composite = null;
		if (descriptor.isGroup()) {
			composite = WidgetBuilderUtils.createGroup(parent, style, layout, data, descriptor.getTitle());
		} else {
			composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		}

		addChildren(descriptor.getTabFolderOrCompositeOrWidget(), composite);

		String attr = descriptor.getBackground();
		if (attr != null) {
			composite.setBackground(WidgetBuilderUtils.getColor(attr));
		}

		FontType fd = descriptor.getFont();
		if (fd != null) {
			composite.setFont(WidgetBuilderUtils.getFont(fd));
		}

		String id = descriptor.getId();
		if (id != null) {
			sources.put(id, composite);
		}

		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, composite);
		}

		return composite;
	}

	/**
	 * Constructs and configures an SWT CTabFolder and its CTabItems. Calls
	 * {@link #addItem(TabFolder, TabItemType, int)}.
	 * 
	 * @see org.eclipse.swt.custom.CTabFolder
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabFolderType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabItemType
	 * 
	 * @param descriptor
	 *            JAXB data element describing the folder contents and layout
	 * @param parent
	 *            control to which to add the folder
	 * @return the SWT TabFolder
	 */
	private CTabFolder addFolder(TabFolderType descriptor, Composite parent) {
		CTabFolder folder = new CTabFolder(parent, WidgetBuilderUtils.getStyle(descriptor.getStyle()));
		Layout layout = createLayout(descriptor.getLayout());
		if (layout != null) {
			folder.setLayout(layout);
		}
		Object data = createLayoutData(descriptor.getLayoutData());
		if (data != null) {
			folder.setLayoutData(data);
		}
		String tt = descriptor.getTooltip();
		if (tt != null) {
			folder.setToolTipText(tt);
		}
		List<TabItemType> items = descriptor.getItem();
		int index = 0;
		for (TabItemType i : items) {
			addItem(folder, i, index++);
		}
		String attr = descriptor.getBackground();
		if (attr != null) {
			folder.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontType fd = descriptor.getFont();
		if (fd != null) {
			folder.setFont(WidgetBuilderUtils.getFont(fd));
		}
		String id = descriptor.getId();
		if (id != null) {
			sources.put(id, folder);
		}
		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, folder);
		}
		return folder;
	}

	/**
	 * Adds an SWT TabItem to a TabFolder. Calls
	 * {@link WidgetBuilderUtils#createTabItem(TabFolder, Integer, String, String, Integer)}
	 * , {@link WidgetBuilderUtils#createComposite(Composite, Integer)},
	 * {@link #addFolder(TabFolderType, Composite)},
	 * {@link #addComposite(CompositeType, Composite)} or
	 * {@link #addWidget(Composite, Widget)}.
	 * 
	 * @see org.eclipse.swt.custom.CTabFolder
	 * @see org.eclipse.swt.custom.CTabItem
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabItemType
	 * 
	 * @param folder
	 *            to which to add the item
	 * @param descriptor
	 *            JAXB data element describing content and layout of the item
	 * @param index
	 *            of the tab in the folder
	 */
	private void addItem(CTabFolder folder, TabItemType descriptor, int index) {
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Layout layout = createLayout(descriptor.getLayout());
		if (layout != null) {
			folder.setLayout(layout);
		}
		Object data = createLayoutData(descriptor.getLayoutData());
		if (data != null) {
			folder.setLayoutData(data);
		}
		CTabItem item = WidgetBuilderUtils.createTabItem(folder, style, descriptor.getTitle(), descriptor.getTooltip(), index);
		Composite control = WidgetBuilderUtils.createComposite(folder, style, layout, data);
		item.setControl(control);
		String tt = descriptor.getTooltip();
		if (tt != null) {
			item.setToolTipText(tt);
		}
		String attr = descriptor.getBackground();
		if (attr != null) {
			control.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontType fd = descriptor.getFont();
		if (fd != null) {
			control.setFont(WidgetBuilderUtils.getFont(fd));
		}
		addChildren(descriptor.getCompositeOrTabFolderOrWidget(), control);
		String id = descriptor.getId();
		if (id != null) {
			sources.put(id, control);
		}
		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, control);
		}
	}

	/**
	 * Adds the row items to the table. The row item model is both a CellEditor
	 * update model/listener, as well as the underlying data model for the
	 * viewer provider. Calls
	 * {@link UpdateModelFactory#createModel(Object, ColumnViewer, List, JAXBDynamicLaunchConfigurationTab)}
	 * . Adds the cell editor-to-model mappings to the local widget map of the
	 * LaunchTab being built.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.CellEditorUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.TableRowUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.ui.model.InfoTreeNodeModel
	 * 
	 * @param viewer
	 *            to which to add the rows
	 * @param descriptor
	 *            describing the content of the viewer's rows
	 * @return list of models for the added items
	 */
	private Collection<ICellEditorUpdateModel> addRows(ColumnViewer viewer, AttributeViewerType descriptor) {
		ViewerItemsType items = descriptor.getItems();
		List<ColumnDataType> columnData = descriptor.getColumnData();
		ICellEditorUpdateModel model = null;
		Map<String, ICellEditorUpdateModel> hash = new TreeMap<String, ICellEditorUpdateModel>();
		Map<String, Object> vars = null;
		if (items.isAllPredefined()) {
			vars = rmVarMap.getVariables();
			for (String key : vars.keySet()) {
				Object o = vars.get(key);
				if (!isVisible(o)) {
					continue;
				}
				model = UpdateModelFactory.createModel(o, viewer, columnData, tab);
				hash.put(key, model);
			}
		}
		if (items.isAllDiscovered()) {
			vars = rmVarMap.getDiscovered();
			for (String key : vars.keySet()) {
				Object o = vars.get(key);
				if (!isVisible(o)) {
					continue;
				}
				model = UpdateModelFactory.createModel(o, viewer, columnData, tab);
				hash.put(key, model);
			}
		}
		for (String key : items.getInclude()) {
			if (hash.containsKey(key)) {
				continue;
			}
			Object o = rmVarMap.getVariables().get(key);
			if (!isVisible(o)) {
				continue;
			}
			if (o == null) {
				o = rmVarMap.getDiscovered().get(key);
			}
			if (o != null) {
				model = UpdateModelFactory.createModel(o, viewer, columnData, tab);
				hash.put(key, model);
			}
		}
		for (String key : items.getExclude()) {
			hash.remove(key);
		}
		for (ICellEditorUpdateModel m : hash.values()) {
			localWidgets.put(m.getControl(), m);
		}
		return hash.values();
	}

	/**
	 * Constructs the widget and creates its update model. Adds the
	 * widget-to-model mapping to the local widget map of the LaunchTab being
	 * built. Calls
	 * {@link UpdateModelFactory#createModel(Composite, Widget, JAXBDynamicLaunchConfigurationTab)}
	 * .
	 * 
	 * @param control
	 *            to which to add the widget
	 * @param widget
	 *            JAXB data element describing type, style and layout data of
	 *            widget
	 */
	private void addWidget(Composite control, WidgetType widget) {
		IUpdateModel model = UpdateModelFactory.createModel(control, widget, tab, rmVarMap, sources, targets);
		/*
		 * Label models are not returned, since they cannot be updated
		 */
		if (model != null) {
			localWidgets.put(model.getControl(), model);
		}
	}

	/**
	 * Creates the SWT layout. Calls
	 * {@link WidgetBuilderUtils#createFillLayout(String, Integer, Integer, Integer)}
	 * ,
	 * {@link WidgetBuilderUtils#createRowLayout(Boolean, Boolean, Boolean, Boolean, Integer, Integer, Integer, Integer, Integer, Integer, Integer)}
	 * ,
	 * {@link WidgetBuilderUtils#createGridLayout(Integer, Boolean, Integer, Integer, Integer, Integer, Integer, Integer, Integer, Integer)}
	 * or
	 * {@link WidgetBuilderUtils#createFormLayout(Integer, Integer, Integer, Integer, Integer, Integer, Integer)}
	 * .
	 * 
	 * @see org.eclipse.swt.widgets.Layout
	 * @see org.eclipse.ptp.rm.jaxb.core.data.LayoutType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.FillLayoutType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.RowLayoutType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.GridLayoutType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.FormLayoutType
	 * 
	 * @param layout
	 *            JAXB data element describing the layout
	 * @return the created SWT layout
	 */
	private Layout createLayout(LayoutType layout) {
		if (layout != null) {
			if (layout.getFillLayout() != null) {
				FillLayoutType fillLayout = layout.getFillLayout();
				return WidgetBuilderUtils.createFillLayout(fillLayout.getType(), fillLayout.getMarginHeight(),
						fillLayout.getMarginWidth(), fillLayout.getSpacing());
			} else if (layout.getRowLayout() != null) {
				RowLayoutType rowLayout = layout.getRowLayout();
				return WidgetBuilderUtils.createRowLayout(rowLayout.getType(), rowLayout.isCenter(), rowLayout.isFill(),
						rowLayout.isJustify(), rowLayout.isPack(), rowLayout.isWrap(), rowLayout.getMarginHeight(),
						rowLayout.getMarginWidth(), rowLayout.getMarginTop(), rowLayout.getMarginBottom(),
						rowLayout.getMarginLeft(), rowLayout.getMarginRight(), rowLayout.getSpacing());
			} else if (layout.getGridLayout() != null) {
				GridLayoutType gridLayout = layout.getGridLayout();
				return WidgetBuilderUtils.createGridLayout(gridLayout.getNumColumns(), gridLayout.isMakeColumnsEqualWidth(),
						gridLayout.getHorizontalSpacing(), gridLayout.getVerticalSpacing(), gridLayout.getMarginWidth(),
						gridLayout.getMarginHeight(), gridLayout.getMarginLeft(), gridLayout.getMarginRight(),
						gridLayout.getMarginTop(), gridLayout.getMarginBottom());
			} else if (layout.getFormLayout() != null) {
				FormLayoutType formLayout = layout.getFormLayout();
				return WidgetBuilderUtils.createFormLayout(formLayout.getMarginHeight(), formLayout.getMarginWidth(),
						formLayout.getMarginTop(), formLayout.getMarginBottom(), formLayout.getMarginLeft(),
						formLayout.getMarginRight(), formLayout.getSpacing());
			}
		}
		return null;
	}

	/**
	 * Constructs a listener for each defined category of action on the target
	 * and add it to the sources referenced in the rule.
	 */
	private void maybeWireWidgets() {
		Collection<ControlStateListener> listeners = new HashSet<ControlStateListener>();

		for (ControlStateType cst : targets.keySet()) {
			Control target = targets.get(cst);
			ControlStateRuleType rule = cst.getEnableIf();
			if (rule != null) {
				listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.ENABLE, sources));
			} else {
				rule = cst.getDisableIf();
				if (rule != null) {
					listeners.add(new ControlStateListener(targets.get(cst), rule, ControlStateListener.Action.DISABLE, sources));
				}
			}

			rule = cst.getHideIf();
			if (rule != null) {
				listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.HIDE, sources));
			} else {
				rule = cst.getShowIf();
				if (rule != null) {
					listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.SHOW, sources));
				}
			}

			rule = cst.getDeselectIf();
			if (rule != null) {
				listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.DESELECT, sources));
			} else {
				rule = cst.getSelectIf();
				if (rule != null) {
					listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.SELECT, sources));
				}
			}
		}

		tab.setListeners(listeners);
	}
}
