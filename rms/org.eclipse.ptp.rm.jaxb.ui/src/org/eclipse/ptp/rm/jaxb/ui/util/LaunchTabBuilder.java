package org.eclipse.ptp.rm.jaxb.ui.util;

import java.util.List;

import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.CheckboxTreeViewer;
import org.eclipse.jface.viewers.ColumnViewer;
import org.eclipse.ptp.rm.jaxb.core.data.AttributeViewer;
import org.eclipse.ptp.rm.jaxb.core.data.GridDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GroupDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMConfigurableAttributesTab;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Tree;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMConfigurableAttributesTab tab;

	public LaunchTabBuilder(JAXBRMConfigurableAttributesTab tab) {
		this.tab = tab;
	}

	public void build(Composite parent) throws Throwable {
		List<Object> top = tab.getController().getTabFolderOrGroupDescriptor();
		for (Object o : top) {
			if (o instanceof GroupDescriptor) {
				addGroup((GroupDescriptor) o, parent);
			} else if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, parent);
			}
		}
	}

	/*
	 * For viewers and widgets, the listeners need to be set. Notes for
	 * construction. we always construct a checkbutton for toggling full and
	 * selected. Need a way to associated the template? that's just on update,
	 * should be in the descriptor.
	 */
	private void addAttributeViewer(AttributeViewer descriptor, Composite parent) {
		GridData data = addGridData(descriptor.getGridData());
		GridLayout layout = addGridLayout(descriptor.getGridLayout());
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		Group group = WidgetBuilderUtils.createGroup(parent, style, layout, data, descriptor.getLabel());
		if (TABLE.equals(descriptor.getType())) {
			addCheckboxTableViewer(group, data, layout, style, descriptor);
		} else if (TREE.equals(descriptor.getType())) {
			addCheckboxTreeViewer(group, data, layout, style, descriptor);
		}
	}

	private void addCheckboxTableViewer(Composite parent, GridData data, GridLayout layout, int style, AttributeViewer descriptor) {
		Table t = WidgetBuilderUtils.createTable(parent, style, data);
		new CheckboxTableViewer(t);
	}

	private void addCheckboxTreeViewer(Composite parent, GridData data, GridLayout layout, int style, AttributeViewer descriptor) {
		Tree t = WidgetBuilderUtils.createTree(parent, style, data);
		new CheckboxTreeViewer(t);
	}

	private void addFolder(TabFolderDescriptor fd, Composite parent) {
		TabFolder folder = new TabFolder(parent, WidgetBuilderUtils.getStyle(fd.getStyle()));
		List<TabItemDescriptor> items = fd.getItem();
		int index = 0;
		for (TabItemDescriptor i : items) {
			addItem(folder, i, index++);
		}
	}

	private GridData addGridData(GridDataDescriptor gridData) {
		int style = WidgetBuilderUtils.getStyle(gridData.getStyle());
		int hAlign = WidgetBuilderUtils.getStyle(gridData.getHorizontalAlign());
		int vAlign = WidgetBuilderUtils.getStyle(gridData.getVerticalAlign());
		return WidgetBuilderUtils.createGridData(style, gridData.isGrabExcessHorizontal(), gridData.isGrabExcessVertical(),
				gridData.getWidthHint(), gridData.getHeightHint(), gridData.getMinWidth(), gridData.getMinHeight(),
				gridData.getHorizontalSpan(), gridData.getVerticalSpan(), hAlign, vAlign);
	}

	private GridLayout addGridLayout(GridLayoutDescriptor gridLayout) {
		return WidgetBuilderUtils.createGridLayout(gridLayout.getNumColumns(), gridLayout.isMakeColumnsEqualWidth(),
				gridLayout.getHorizontalSpacing(), gridLayout.getVerticalSpacing(), gridLayout.getMarginWidth(),
				gridLayout.getMarginHeight(), gridLayout.getMarginLeft(), gridLayout.getMarginRight(), gridLayout.getMarginTop(),
				gridLayout.getMarginBottom());
	}

	private void addGroup(GroupDescriptor grp, Composite parent) {
		GridData data = addGridData(grp.getGridData());
		GridLayout layout = addGridLayout(grp.getGridLayout());
		int style = WidgetBuilderUtils.getStyle(grp.getStyle());
		Group group = WidgetBuilderUtils.createGroup(parent, style, layout, data, grp.getTitle());
		List<Object> widget = grp.getTabFolderOrGroupOrWidget();
		for (Object o : widget) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, group);
			} else if (o instanceof GroupDescriptor) {
				addGroup((GroupDescriptor) o, group);
			} else if (o instanceof Widget) {
				addWidget((Widget) o, group);
			} else if (o instanceof AttributeViewer) {
				addAttributeViewer((AttributeViewer) o, group);
			}
		}
	}

	private void addItem(TabFolder folder, TabItemDescriptor descriptor, int index) {
		int style = WidgetBuilderUtils.getStyle(descriptor.getStyle());
		TabItem item = WidgetBuilderUtils.createTabItem(folder, style, descriptor.getTitle(), descriptor.getTooltip(), index);
		Composite control = WidgetBuilderUtils.createComposite(folder, 1);
		item.setControl(control);
		List<Object> children = descriptor.getGroupOrTabFolder();
		for (Object o : children) {
			if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, control);
			} else if (o instanceof GroupDescriptor) {
				addGroup((GroupDescriptor) o, control);
			}
		}
	}

	private void addRows(ColumnViewer viewer, AttributeViewer descriptor) {

	}

	private void addWidget(Widget o, Composite parent) {

	}
}
