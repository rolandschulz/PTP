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

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ptp.rm.jaxb.core.data.Attribute;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.ColumnData;
import org.eclipse.ptp.rm.jaxb.core.data.CompositeDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.FillLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.FontDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.FormAttachmentDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.FormDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.FormLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.LayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Property;
import org.eclipse.ptp.rm.jaxb.core.data.RowDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.RowLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.ViewerItems;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.ICellEditorUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.IUpdateModel;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBConfigurableAttributesTab;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.model.ViewerUpdateModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final JAXBLaunchConfigurationDynamicTab parentTab;
	private final TabController controller;;
	private final Map<Object, IUpdateModel> localWidgets;

	public LaunchTabBuilder(JAXBConfigurableAttributesTab tab) {
		parentTab = tab.getParent();
		controller = tab.getController();
		localWidgets = tab.getLocalWidgets();
	}

	public void build(Composite parent) throws Throwable {
		List<Object> top = controller.getTabFolderOrComposite();

		for (Object o : top) {
			if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, parent);
			} else if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, parent);
			}
		}

		for (Object o : localWidgets.keySet()) {
			parentTab.getUpdateHandler().addUpdateModelEntry(o, localWidgets.get(o));
		}
	}

	private void addAttributeViewer(AttributeViewer descriptor, Composite parent) {
		Layout layout = createLayout(descriptor.getLayout());
		Object data = createLayoutData(descriptor.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Button showHide = WidgetBuilderUtils.createCheckButton(parent, Messages.ToggleShowHideSelectedAttributes, null);
		ColumnViewer viewer = null;
		if (TABLE.equals(descriptor.getType())) {
			viewer = addCheckboxTableViewer(parent, data, layout, style, descriptor);
		} else if (TREE.equals(descriptor.getType())) {
			viewer = addCheckboxTreeViewer(parent, data, layout, style, descriptor);
		}
		Collection<ICellEditorUpdateModel> rows = addRows(viewer, descriptor);
		ViewerUpdateModel model = UpdateModelFactory.createModel(viewer, descriptor, parentTab);
		for (ICellEditorUpdateModel row : rows) {
			row.setViewer(model);
		}
		showHide.addSelectionListener(model);
		model.setShowAll(showHide);
		localWidgets.put(viewer, model);
	}

	private ColumnViewer addCheckboxTableViewer(Composite parent, Object data, Layout layout, int style, AttributeViewer descriptor) {
		style |= (SWT.CHECK | SWT.FULL_SELECTION);
		Table t = WidgetBuilderUtils.createTable(parent, style, data);
		CheckboxTableViewer viewer = new CheckboxTableViewer(t);
		WidgetBuilderUtils.setupAttributeTable(viewer, descriptor.getColumnData(), null, descriptor.isSort(),
				descriptor.isTooltipEnabled(), descriptor.isHeaderVisible(), descriptor.isLinesVisible());
		return viewer;
	}

	private ColumnViewer addCheckboxTreeViewer(Composite parent, Object data, Layout layout, int style, AttributeViewer descriptor) {
		style |= (SWT.CHECK | SWT.FULL_SELECTION);
		Tree t = WidgetBuilderUtils.createTree(parent, style, data);
		CheckboxTreeViewer viewer = new CheckboxTreeViewer(t);
		WidgetBuilderUtils.setupAttributeTree(viewer, descriptor.getColumnData(), null, descriptor.isSort(),
				descriptor.isTooltipEnabled(), descriptor.isHeaderVisible(), descriptor.isLinesVisible());
		return viewer;
	}

	private Composite addComposite(CompositeDescriptor descriptor, Composite parent) {
		Layout layout = createLayout(descriptor.getLayout());
		Object data = createLayoutData(descriptor.getLayoutData());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Composite composite = null;
		if (descriptor.isGroup()) {
			composite = WidgetBuilderUtils.createGroup(parent, style, layout, data, descriptor.getTitle());
		} else {
			composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		}
		List<Object> widget = descriptor.getTabFolderOrCompositeOrWidget();
		for (Object o : widget) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, composite);
			} else if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, composite);
			} else if (o instanceof Widget) {
				addWidget(composite, (Widget) o);
			} else if (o instanceof AttributeViewer) {
				addAttributeViewer((AttributeViewer) o, composite);
			}
		}
		String attr = descriptor.getBackground();
		if (attr != null) {
			composite.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontDescriptor fd = descriptor.getFont();
		if (fd != null) {
			composite.setFont(WidgetBuilderUtils.getFont(fd));
		}
		return composite;
	}

	private TabFolder addFolder(TabFolderDescriptor descriptor, Composite parent) {
		TabFolder folder = new TabFolder(parent, WidgetBuilderUtils.getStyle(descriptor.getStyle()));
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
		List<TabItemDescriptor> items = descriptor.getItem();
		int index = 0;
		for (TabItemDescriptor i : items) {
			addItem(folder, i, index++);
		}
		String attr = descriptor.getBackground();
		if (attr != null) {
			folder.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontDescriptor fd = descriptor.getFont();
		if (fd != null) {
			folder.setFont(WidgetBuilderUtils.getFont(fd));
		}
		return folder;
	}

	private void addItem(TabFolder folder, TabItemDescriptor descriptor, int index) {
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		TabItem item = WidgetBuilderUtils.createTabItem(folder, style, descriptor.getTitle(), descriptor.getTooltip(), index);
		Composite control = WidgetBuilderUtils.createComposite(folder, 1);
		item.setControl(control);
		String tt = descriptor.getTooltip();
		if (tt != null) {
			item.setToolTipText(tt);
		}
		String attr = descriptor.getBackground();
		if (attr != null) {
			control.setBackground(WidgetBuilderUtils.getColor(attr));
		}
		FontDescriptor fd = descriptor.getFont();
		if (fd != null) {
			control.setFont(WidgetBuilderUtils.getFont(fd));
		}

		List<Object> children = descriptor.getCompositeOrTabFolderOrWidget();
		for (Object o : children) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, control);
			} else if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, control);
			} else if (o instanceof Widget) {
				addWidget(control, (Widget) o);
			}
		}
	}

	private Collection<ICellEditorUpdateModel> addRows(ColumnViewer viewer, AttributeViewer descriptor) {
		RMVariableMap rmMap = RMVariableMap.getActiveInstance();
		ViewerItems items = descriptor.getItems();
		List<ColumnData> columnData = descriptor.getColumnData();
		ICellEditorUpdateModel model = null;
		Map<String, ICellEditorUpdateModel> hash = new TreeMap<String, ICellEditorUpdateModel>();
		Map<String, Object> vars = null;
		if (items.isAllPredefined()) {
			vars = rmMap.getVariables();
			for (String key : vars.keySet()) {
				Object o = vars.get(key);
				if (!isVisible(o)) {
					continue;
				}
				model = UpdateModelFactory.createModel(o, viewer, columnData, parentTab);
				hash.put(key, model);
			}
		}
		if (items.isAllDiscovered()) {
			vars = rmMap.getDiscovered();
			for (String key : vars.keySet()) {
				Object o = vars.get(key);
				if (!isVisible(o)) {
					continue;
				}
				model = UpdateModelFactory.createModel(o, viewer, columnData, parentTab);
				hash.put(key, model);
			}
		}
		for (String key : items.getInclude()) {
			if (hash.containsKey(key)) {
				continue;
			}
			Object o = rmMap.getVariables().get(key);
			if (!isVisible(o)) {
				continue;
			}
			if (o == null) {
				o = rmMap.getDiscovered().get(key);
			}
			if (o != null) {
				model = UpdateModelFactory.createModel(o, viewer, columnData, parentTab);
				hash.put(key, model);
			}
		}
		for (String key : items.getExclude()) {
			hash.remove(key);
		}
		viewer.setInput(hash.values());

		for (ICellEditorUpdateModel cm : hash.values()) {
			localWidgets.put(cm.getCellEditor(), cm);
		}
		return hash.values();
	}

	private void addWidget(Composite control, Widget widget) {
		IUpdateModel model = UpdateModelFactory.createModel(control, widget, parentTab);
		if (model != null) {
			localWidgets.put(model.getControl(), model);
		}
	}

	private Layout createLayout(LayoutDescriptor layout) {
		if (layout != null) {
			if (layout.getFillLayout() != null) {
				FillLayoutDescriptor fillLayout = layout.getFillLayout();
				return WidgetBuilderUtils.createFillLayout(fillLayout.getType(), fillLayout.getMarginHeight(),
						fillLayout.getMarginWidth(), fillLayout.getSpacing());
			} else if (layout.getRowLayout() != null) {
				RowLayoutDescriptor rowLayout = layout.getRowLayout();
				return WidgetBuilderUtils.createRowLayout(rowLayout.isCenter(), rowLayout.isFill(), rowLayout.isJustify(),
						rowLayout.isPack(), rowLayout.getMarginHeight(), rowLayout.getMarginWidth(), rowLayout.getMarginTop(),
						rowLayout.getMarginBottom(), rowLayout.getMarginLeft(), rowLayout.getMarginRight(), rowLayout.getSpacing());
			} else if (layout.getGridLayout() != null) {
				GridLayoutDescriptor gridLayout = layout.getGridLayout();
				return WidgetBuilderUtils.createGridLayout(gridLayout.getNumColumns(), gridLayout.isMakeColumnsEqualWidth(),
						gridLayout.getHorizontalSpacing(), gridLayout.getVerticalSpacing(), gridLayout.getMarginWidth(),
						gridLayout.getMarginHeight(), gridLayout.getMarginLeft(), gridLayout.getMarginRight(),
						gridLayout.getMarginTop(), gridLayout.getMarginBottom());
			} else if (layout.getFormLayout() != null) {
				FormLayoutDescriptor formLayout = layout.getFormLayout();
				return WidgetBuilderUtils.createFormLayout(formLayout.getMarginHeight(), formLayout.getMarginWidth(),
						formLayout.getMarginTop(), formLayout.getMarginBottom(), formLayout.getMarginLeft(),
						formLayout.getMarginRight(), formLayout.getSpacing());
			}
		}
		return null;
	}

	static Object createLayoutData(LayoutDataDescriptor layoutData) {
		if (layoutData != null) {
			if (layoutData.getRowData() != null) {
				RowDataDescriptor rowData = layoutData.getRowData();
				return WidgetBuilderUtils.createRowData(rowData.getHeight(), rowData.getWidth(), rowData.isExclude());
			} else if (layoutData.getGridData() != null) {
				GridDataDescriptor gridData = layoutData.getGridData();
				int style = WidgetBuilderUtils.getStyle(gridData.getStyle());
				int hAlign = WidgetBuilderUtils.getStyle(gridData.getHorizontalAlign());
				int vAlign = WidgetBuilderUtils.getStyle(gridData.getVerticalAlign());
				return WidgetBuilderUtils.createGridData(style, gridData.isGrabExcessHorizontal(), gridData.isGrabExcessVertical(),
						gridData.getWidthHint(), gridData.getHeightHint(), gridData.getMinWidth(), gridData.getMinHeight(),
						gridData.getHorizontalSpan(), gridData.getVerticalSpan(), hAlign, vAlign);

			} else if (layoutData.getFormData() != null) {
				FormDataDescriptor formData = layoutData.getFormData();
				FormAttachment top = null;
				FormAttachment bottom = null;
				FormAttachment left = null;
				FormAttachment right = null;
				FormAttachmentDescriptor fad = formData.getTop();
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

	private static boolean isVisible(Object data) {
		if (data instanceof Attribute) {
			return ((Attribute) data).isVisible();
		} else if (data instanceof Property) {
			return ((Property) data).isVisible();
		}
		return false;
	}
}
