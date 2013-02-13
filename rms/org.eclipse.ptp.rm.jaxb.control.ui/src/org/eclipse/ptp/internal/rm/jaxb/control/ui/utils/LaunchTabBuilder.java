/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.internal.rm.jaxb.control.ui.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.JAXBControlUIConstants;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.messages.Messages;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ViewerUpdateModel;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.rm.jaxb.control.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeType;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.BrowseType;
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
import org.eclipse.ptp.rm.jaxb.core.data.PushButtonType;
import org.eclipse.ptp.rm.jaxb.core.data.RowDataType;
import org.eclipse.ptp.rm.jaxb.core.data.RowLayoutType;
import org.eclipse.ptp.rm.jaxb.core.data.TabControllerType;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderType;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemType;
import org.eclipse.ptp.rm.jaxb.core.data.ViewerItemsType;
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

/**
 * Object responsible for constructing the configurable JAXB Launch Configuration Tab, as well as building and registering the
 * related model objects and listeners.
 * 
 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.launch.JAXBDynamicLaunchConfigurationTab
 * 
 * @author arossi
 * 
 */
public class LaunchTabBuilder {

	/**
	 * Create the layout data object. Calls
	 * 
	 * @param layoutData
	 *            JAXB data element describing the layout data object
	 * @return the data object
	 */
	public static Object createLayoutData(LayoutDataType layoutData) {
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
	 * Initialize global state. Must be called prior to building any tabs.
	 */
	public static void initialize() {
		sources.clear();
		targets.clear();
	}

	private final IJAXBLaunchConfigurationTab tab;
	private final IVariableMap rmVarMap;

	/*
	 * Temporary maps for wiring the widgets. Sources contains buttons that have buttonId specified. Targets contain controls that
	 * have their state updated by a button.
	 * 
	 * NOTE: button ID's are global across all tabs. This allows the button on one tab to update the state of controls on other
	 * tabs.
	 */
	private static final Map<String, Button> sources = new HashMap<String, Button>();
	private static final Map<ControlStateType, Control> targets = new HashMap<ControlStateType, Control>();

	/**
	 * @param tab
	 *            whose control is configurable from the JAXB data tree
	 */
	public LaunchTabBuilder(IJAXBLaunchConfigurationTab tab) throws Throwable {
		this.tab = tab;
		this.rmVarMap = tab.getParent().getJobControl().getEnvironment();
	}

	/**
	 * Constructs the viewer, its row items and their update models, and adds it to the tree.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.ICellEditorUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ViewerUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.UpdateModelFactory#createModel(ColumnViewer, AttributeViewer,
	 *      JAXBDynamicLaunchConfigurationTab)
	 * 
	 * @param descriptor
	 *            JAXB data element describing CheckboxTableViewer or CheckboxTreeViewer for displaying a subset of the resource
	 *            manager properties and attributes
	 * @param parent
	 *            control to which to add the viewer
	 * @return the control
	 */
	public ColumnViewer addAttributeViewer(AttributeViewerType descriptor, Composite parent) {
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
			tab.getLocalWidgets().put(viewer, model);

			ControlStateType cst = descriptor.getControlState();
			if (cst != null) {
				targets.put(cst, viewer.getControl());
			}
		}
		return viewer;
	}

	/**
	 * Root call to build the SWT widget tree.<br>
	 * <br>
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
		tab.getLocalWidgets().clear();
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
	 * Constructs the widget pair and creates its update model. Adds the widget-to-model mapping to the local widget map of the
	 * LaunchTab being built.
	 * 
	 * @param browse
	 *            JAXB data element describing type, style and layout data of widget
	 * @param control
	 *            to which to add the widget
	 */
	private void addBrowse(BrowseType browse, Composite control) {
		IUpdateModel model = UpdateModelFactory.createModel(control, browse, tab, rmVarMap, targets);
		if (model != null) {
			tab.getLocalWidgets().put(model.getControl(), model);
		}
	}

	/**
	 * Constructs a button group and its model, and adds it to the tree.
	 * 
	 * @see UpdateModelFactory#createModel(Group, List, String, JAXBDynamicLaunchConfigurationTab, IVariableMap)
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
		IUpdateModel model = UpdateModelFactory.createModel(descriptor, control, tab, rmVarMap, sources, targets);
		if (model != null) {
			tab.getLocalWidgets().put(control, model);
		}

		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, control);
		}
	}

	/**
	 * Constructs and configures the SWT TableViewer.
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
	 *            JAXB data element describing CheckboxTableViewer or CheckboxTreeViewer for displaying a subset of the resource
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
	 * Constructs and configures the SWT TreeViewer.
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
	 *            JAXB data element describing CheckboxTableViewer or CheckboxTreeViewer for displaying a subset of the resource
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
	 * Iterates through list and calls add for the type.
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
				addWidget((WidgetType) o, control);
			} else if (o instanceof ButtonGroupType) {
				addButtonGroup((ButtonGroupType) o, control);
			} else if (o instanceof PushButtonType) {
				addPushButton((PushButtonType) o, control);
			} else if (o instanceof BrowseType) {
				addBrowse((BrowseType) o, control);
			} else if (o instanceof AttributeViewerType) {
				addAttributeViewer((AttributeViewerType) o, control);
			}
		}
	}

	/**
	 * Adds an SWT Composite widget.
	 * 
	 * @see org.eclipse.swt.widgets.Composite
	 * @see org.eclipse.swt.widgets.Group
	 * @see org.eclipse.ptp.rm.jaxb.core.data.TabFolderType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.CompositeType
	 * @see org.eclipse.ptp.rm.jaxb.core.data.Widget
	 * @see org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer
	 * 
	 * @param descriptor
	 *            JAXB data element describing the contents and layout of the composite
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

		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, composite);
		}

		return composite;
	}

	/**
	 * Constructs and configures an SWT CTabFolder and its CTabItems.
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
		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, folder);
		}
		return folder;
	}

	/**
	 * Adds an SWT TabItem to a TabFolder.
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
		ControlStateType cst = descriptor.getControlState();
		if (cst != null) {
			targets.put(cst, control);
		}
	}

	/**
	 * Constructs the widget pair and creates its update model. Calls .
	 * 
	 * @param button
	 *            JAXB data element describing type, style and layout data of widget
	 * @param control
	 *            to which to add the widget
	 */
	private void addPushButton(PushButtonType button, Composite control) {
		UpdateModelFactory.createPushButton(control, button, tab, rmVarMap, targets);
	}

	/**
	 * Adds the row items to the table. The row item model is both a CellEditor update model/listener, as well as the underlying
	 * data model for the viewer provider. Adds the cell editor-to-model mappings to the local widget map of the LaunchTab being
	 * built.
	 * 
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.IUpdateModel
	 * @see org.eclipse.ptp.rm.jaxb.control.core.ui.ICellEditorUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.CellEditorUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.TableRowUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.ValueTreeNodeUpdateModel
	 * @see org.eclipse.ptp.internal.rm.jaxb.control.ui.model.InfoTreeNodeModel
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
		Map<String, AttributeType> vars = null;
		if (items.isAllPredefined()) {
			vars = rmVarMap.getAttributes();
			for (String key : vars.keySet()) {
				AttributeType a = vars.get(key);
				if (!a.isVisible()) {
					continue;
				}
				model = UpdateModelFactory.createModel(a, viewer, columnData, tab);
				hash.put(key, model);
			}
		}
		if (items.isAllDiscovered()) {
			vars = rmVarMap.getDiscovered();
			for (String key : vars.keySet()) {
				AttributeType a = vars.get(key);
				if (!a.isVisible()) {
					continue;
				}
				model = UpdateModelFactory.createModel(a, viewer, columnData, tab);
				hash.put(key, model);
			}
		}
		for (String key : items.getInclude()) {
			if (!hash.containsKey(key)) {
				AttributeType a = rmVarMap.getAttributes().get(key);
				if (a == null) {
					a = rmVarMap.getDiscovered().get(key);
				}
				if (a != null && a.isVisible()) {
					model = UpdateModelFactory.createModel(a, viewer, columnData, tab);
					hash.put(key, model);
				}
			}
		}
		for (String key : items.getExclude()) {
			hash.remove(key);
		}
		for (ICellEditorUpdateModel m : hash.values()) {
			tab.getLocalWidgets().put(m.getControl(), m);
		}
		return hash.values();
	}

	/**
	 * Constructs the widget and creates its update model. Adds the widget-to-model mapping to the local widget map of the LaunchTab
	 * being built.
	 * 
	 * @param widget
	 *            JAXB data element describing type, style and layout data of widget
	 * @param control
	 *            to which to add the widget
	 */
	private void addWidget(WidgetType widget, Composite control) {
		IUpdateModel model = UpdateModelFactory.createModel(control, widget, tab, rmVarMap, sources, targets);
		/*
		 * Label models are not returned, since they cannot be updated
		 */
		if (model != null) {
			tab.getLocalWidgets().put(model.getControl(), model);
		}
	}

	/**
	 * Creates the SWT layout.
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
	 * Constructs a listener for each defined category of action on the target and adds it to the sources referenced in the rule.
	 */
	private void maybeWireWidgets() throws Throwable {
		Collection<ControlStateListener> listeners = new HashSet<ControlStateListener>();
		for (ControlStateType cst : targets.keySet()) {
			Control target = targets.get(cst);
			ControlStateRuleType rule = cst.getEnableIf();
			if (rule != null) {
				listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.ENABLE, sources, tab.getParent()
						.getVariableMap()));
			} else {
				rule = cst.getDisableIf();
				if (rule != null) {
					listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.DISABLE, sources, tab
							.getParent().getVariableMap()));
				}
			}

			rule = cst.getHideIf();
			if (rule != null) {
				listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.HIDE, sources, tab.getParent()
						.getVariableMap()));
			} else {
				rule = cst.getShowIf();
				if (rule != null) {
					listeners.add(new ControlStateListener(target, rule, ControlStateListener.Action.SHOW, sources, tab.getParent()
							.getVariableMap()));
				}
			}
		}

		Set<Button> dependSet = new HashSet<Button>();
		for (ControlStateListener listener : listeners) {
			dependSet.clear();
			listener.findCyclicalDependecies(dependSet);
		}

		tab.setListeners(listeners);
	}
}
