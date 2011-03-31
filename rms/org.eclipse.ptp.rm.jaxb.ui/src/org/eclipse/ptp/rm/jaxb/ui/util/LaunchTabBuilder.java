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

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.CompositeDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.ViewerItems;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerData;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerMap;
import org.eclipse.ptp.rm.jaxb.ui.data.AttributeViewerRowData;
import org.eclipse.ptp.rm.jaxb.ui.data.WidgetMap;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMConfigurableAttributesTab;
import org.eclipse.ptp.rm.jaxb.ui.messages.Messages;
import org.eclipse.ptp.rm.jaxb.ui.providers.TableDataContentProvider;
import org.eclipse.ptp.rm.jaxb.ui.providers.TreeDataContentProvider;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMConfigurableAttributesTab tab;
	private WidgetMap widgetMap;
	private AttributeViewerMap viewerMap;

	public LaunchTabBuilder(JAXBRMConfigurableAttributesTab tab) {
		this.tab = tab;
	}

	public void build(Composite parent) throws Throwable {
		List<Object> top = tab.getController().getTabFolderOrCompositeDescriptor();
		for (Object o : top) {
			if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, parent);
			} else if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, parent);
			}
		}
	}

	private void addAttributeViewer(AttributeViewer descriptor, Composite parent) {
		GridData data = addGridData(descriptor.getGridData());
		GridLayout layout = addGridLayout(descriptor.getGridLayout());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		String title = descriptor.getLabel();
		Composite composite = null;
		if (title == null) {
			composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		} else {
			composite = WidgetBuilderUtils.createGroup(parent, style, layout, data, descriptor.getLabel());
		}
		ColumnViewer viewer = null;
		if (TABLE.equals(descriptor.getType())) {
			viewer = addCheckboxTableViewer(composite, data, layout, style, descriptor);
		} else if (TREE.equals(descriptor.getType())) {
			viewer = addCheckboxTreeViewer(composite, data, layout, style, descriptor);
		}
		addToggleVisible(composite, viewer);
		addRows(viewer, descriptor);
		addToViewerMap(viewer, descriptor);
	}

	private ColumnViewer addCheckboxTableViewer(Composite parent, GridData data, GridLayout layout, int style,
			AttributeViewer descriptor) {
		Table t = WidgetBuilderUtils.createTable(parent, style, data);
		CheckboxTableViewer viewer = new CheckboxTableViewer(t);
		WidgetBuilderUtils.setupAttributeTable(viewer, WidgetBuilderUtils.getColumnDescriptors(descriptor), null,
				descriptor.isSort());
		return viewer;
	}

	private ColumnViewer addCheckboxTreeViewer(Composite parent, GridData data, GridLayout layout, int style,
			AttributeViewer descriptor) {
		Tree t = WidgetBuilderUtils.createTree(parent, style, data);
		CheckboxTreeViewer viewer = new CheckboxTreeViewer(t);
		WidgetBuilderUtils.setupAttributeTree(viewer, WidgetBuilderUtils.getColumnDescriptors(descriptor), null,
				descriptor.isSort());
		return viewer;
	}

	private Composite addComposite(CompositeDescriptor cd, Composite parent) {
		GridData data = addGridData(cd.getGridData());
		GridLayout layout = addGridLayout(cd.getGridLayout());
		int style = WidgetBuilderUtils.getStyle(cd.getStyle());
		Composite composite = null;
		if (cd.isGroup()) {
			composite = WidgetBuilderUtils.createGroup(parent, style, layout, data, cd.getTitle());
		} else {
			composite = WidgetBuilderUtils.createComposite(parent, style, layout, data);
		}
		List<Object> widget = cd.getTabFolderOrCompositeOrWidget();
		for (Object o : widget) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, composite);
			} else if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, composite);
			} else if (o instanceof Widget) {
				System.out.println("Adding widget: " + o + ", type: " + ((Widget) o).getType());
				addWidget((Widget) o, composite);
			} else if (o instanceof AttributeViewer) {
				addAttributeViewer((AttributeViewer) o, composite);
			}
		}
		return composite;
	}

	private void addFolder(TabFolderDescriptor fd, Composite parent) {
		TabFolder folder = new TabFolder(parent, WidgetBuilderUtils.getStyle(fd.getStyle()));
		List<TabItemDescriptor> items = fd.getItem();
		int index = 0;
		for (TabItemDescriptor i : items) {
			addItem(folder, i, index++);
		}
	}

	private GridLayout addGridLayout(GridLayoutDescriptor gridLayout) {
		if (gridLayout == null) {
			return null;
		}
		return WidgetBuilderUtils.createGridLayout(gridLayout.getNumColumns(), gridLayout.isMakeColumnsEqualWidth(),
				gridLayout.getHorizontalSpacing(), gridLayout.getVerticalSpacing(), gridLayout.getMarginWidth(),
				gridLayout.getMarginHeight(), gridLayout.getMarginLeft(), gridLayout.getMarginRight(), gridLayout.getMarginTop(),
				gridLayout.getMarginBottom());
	}

	private void addItem(TabFolder folder, TabItemDescriptor descriptor, int index) {
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		TabItem item = WidgetBuilderUtils.createTabItem(folder, style, descriptor.getTitle(), descriptor.getTooltip(), index);
		Composite control = WidgetBuilderUtils.createComposite(folder, 1);
		item.setControl(control);
		List<Object> children = descriptor.getCompositeOrTabFolderOrWidget();
		for (Object o : children) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, control);
			} else if (o instanceof CompositeDescriptor) {
				addComposite((CompositeDescriptor) o, control);
			} else if (o instanceof Widget) {
				addWidget((Widget) o, control);
			}
		}
	}

	private void addRows(ColumnViewer viewer, AttributeViewer descriptor) {
		RMVariableMap rmMap = RMVariableMap.getActiveInstance();
		AttributeViewerData data = new AttributeViewerData();
		ViewerItems items = descriptor.getItems();
		if (items.isAllPredefined()) {
			for (Object o : rmMap.getVariables().values()) {
				data.addRow(new AttributeViewerRowData(o));
			}
		} else {
			List<String> refs = items.getRef();
			for (String ref : refs) {
				Object o = rmMap.getVariables().get(ref);
				if (o != null) {
					data.addRow(new AttributeViewerRowData(o));
				}
			}
		}
		if (items.isAllDiscovered()) {
			for (Object o : rmMap.getDiscovered().values()) {
				data.addRow(new AttributeViewerRowData(o));
			}
		}
		viewer.setInput(data);
	}

	private void addToggleVisible(Composite parent, final ColumnViewer viewer) {
		WidgetBuilderUtils.createCheckButton(parent, Messages.ToggleVisibleAttributes, new SelectionListener() {

			public void widgetDefaultSelected(SelectionEvent e) {
				widgetSelected(e);
			}

			public void widgetSelected(SelectionEvent e) {
				Button b = (Button) e.getSource();
				if (viewer instanceof TableViewer) {
					TableDataContentProvider p = (TableDataContentProvider) viewer.getContentProvider();
					p.setSelectedOnly(!b.getSelection());
				} else if (viewer instanceof TreeViewer) {
					TreeDataContentProvider p = (TreeDataContentProvider) viewer.getContentProvider();
					p.setSelectedOnly(!b.getSelection());
				}
			}
		});
	}

	private void addToViewerMap(ColumnViewer viewer, AttributeViewer descriptor) {
		if (viewerMap == null) {
			viewerMap = new AttributeViewerMap();
			tab.getHandlers().add(viewerMap);
		}
		viewerMap.add(viewer, descriptor);
	}

	private void addToWidgetMap(Control control, Widget widget) {
		if (widgetMap == null) {
			widgetMap = new WidgetMap();
			tab.getHandlers().add(widgetMap);
		}
		widgetMap.add(control, widget);
	}

	private Control addWidget(Widget widget, Composite parent) {
		Control control = new WidgetBuilder(widget, RMVariableMap.getActiveInstance(), tab).createControl(parent);
		addToWidgetMap(control, widget);
		return control;
	}

	static GridData addGridData(GridDataDescriptor gridData) {
		if (gridData == null) {
			return null;
		}
		int style = WidgetBuilderUtils.getStyle(gridData.getStyle());
		int hAlign = WidgetBuilderUtils.getStyle(gridData.getHorizontalAlign());
		int vAlign = WidgetBuilderUtils.getStyle(gridData.getVerticalAlign());
		return WidgetBuilderUtils.createGridData(style, gridData.isGrabExcessHorizontal(), gridData.isGrabExcessVertical(),
				gridData.getWidthHint(), gridData.getHeightHint(), gridData.getMinWidth(), gridData.getMinHeight(),
				gridData.getHorizontalSpan(), gridData.getVerticalSpan(), hAlign, vAlign);
	}
}
