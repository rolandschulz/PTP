/*******************************************************************************
 * Copyright (c) 2012 University of Illinois and others.  All rights reserved.
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html 
 *   
 * Contributors: 
 * 		Chris Navarro (Illinois/NCSA) - Design and implementation
 *******************************************************************************/
package org.eclipse.ptp.etfw.launch.ui.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.handlers.ControlStateListener;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.launch.IJAXBLaunchConfigurationTab;
import org.eclipse.ptp.internal.rm.jaxb.control.ui.utils.UpdateModelFactory;
import org.eclipse.ptp.internal.rm.jaxb.ui.util.WidgetBuilderUtils;
import org.eclipse.ptp.launch.ui.extensions.JAXBDynamicLaunchConfigurationTab;
import org.eclipse.ptp.rm.jaxb.control.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.core.IVariableMap;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewerType;
import org.eclipse.ptp.rm.jaxb.core.data.BrowseType;
import org.eclipse.ptp.rm.jaxb.core.data.ButtonGroupType;
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
import org.eclipse.ptp.rm.jaxb.core.data.WidgetType;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Layout;

/**
 * Based on LaunchTabBuilder, this class is responsible for constructing the Performance Analysis Tab as well as building and
 * registering the related object models and listeners
 * 
 * @author "Chris Navarro"
 * 
 */
public class ETFWToolTabBuilder {
	private final IJAXBLaunchConfigurationTab tab;
	// protected Composite control;
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

	public ETFWToolTabBuilder(IJAXBLaunchConfigurationTab tab, IVariableMap etfwVarMap) {
		this.tab = tab;
		this.rmVarMap = etfwVarMap;
	}

	/**
	 * Initialize global state. Must be called prior to building any tabs.
	 */
	public static void initialize() {
		sources.clear();
		targets.clear();
	}

	// TODO : CMN starting to build the panel
	public static void buildToolPane(TabControllerType toolPane, Composite parent) {
		Layout layout = createLayout(toolPane.getLayout());
		Object data = createLayoutData(toolPane.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(toolPane.getStyle());

		// Composite composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		WidgetBuilderUtils.createComposite(parent, style, layout, data);
	}

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
	private static Layout createLayout(LayoutType layout) {
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

	public Composite build(Composite parent) throws Throwable {
		Layout layout = createLayout(tab.getController().getLayout());
		Object layoutData = createLayoutData(tab.getController().getLayoutData());
		int style = WidgetBuilderUtils.getStyle(tab.getController().getStyle());

		Composite composite = WidgetBuilderUtils.createComposite(parent, style, layout, layoutData);
		addChildren(tab.getController().getTabFolderOrCompositeOrWidget(), composite);

		String colorAttribute = tab.getController().getBackground();
		if (colorAttribute != null) {
			composite.setBackground(WidgetBuilderUtils.getColor(colorAttribute));
		}

		FontType fontType = tab.getController().getFont();
		if (fontType != null) {
			composite.setFont(WidgetBuilderUtils.getFont(fontType));
		}

		maybeWireWidgets();

		return composite;
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
		// Removed this because the dependency check is not thorough enough, should file a bug on this.
		for (ControlStateListener listener : listeners) {
			dependSet.clear();
			// listener.findCyclicalDependecies(dependSet);
		}

		tab.setListeners(listeners);
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
				// TODO CMN : implement this if needed
				// addAttributeViewer((AttributeViewerType) o, control);
			}
		}
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
		IUpdateModel model = null;
		try {
			model = UpdateModelFactory.createModel(control, browse, tab, rmVarMap, targets);
		} catch (CoreException e) {
			// Ignore
		}
		if (model != null) {

			tab.getLocalWidgets().put(model.getControl(), model);
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
	 * Constructs the widget and creates its update model. Adds the widget-to-model mapping to the local widget map of the LaunchTab
	 * being built.
	 * 
	 * @param widget
	 *            JAXB data element describing type, style and layout data of widget
	 * @param control
	 *            to which to add the widget
	 */
	private void addWidget(WidgetType widget, Composite control) {
		IUpdateModel model = null;
		try {
			model = UpdateModelFactory.createModel(control, widget, tab, rmVarMap, sources, targets);
		} catch (CoreException e) {
			// Ignore
		}
		/*
		 * Label models are not returned, since they cannot be updated
		 */
		if (model != null) {
			tab.getLocalWidgets().put(model.getControl(), model);
		}
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
	 * The top-level control.
	 */
	// public Control getControl() {
	// return control;
	// }
}
