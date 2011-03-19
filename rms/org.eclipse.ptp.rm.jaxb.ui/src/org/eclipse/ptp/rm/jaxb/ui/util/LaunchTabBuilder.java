package org.eclipse.ptp.rm.jaxb.ui.util;

import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.GridDataDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GridLayoutDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.GroupDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolderDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.TabItemDescriptor;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.TabFolder;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final TabController tabController;

	private final Map<Control, Widget> valueWidgets;
	private final Map<String, Boolean> selected;
	private final RMVariableMap rmVars;

	public LaunchTabBuilder(TabController tabController, RMVariableMap rmVars, Map<Control, Widget> valueWidgets,
			Map<String, Boolean> selected) {
		this.tabController = tabController;
		this.valueWidgets = valueWidgets;
		this.selected = selected;
		this.rmVars = rmVars;
	}

	public void build(Composite parent) throws Throwable {
		List<Object> top = tabController.getTabFolderOrGroupDescriptor();
		for (Object o : top) {
			if (o instanceof GroupDescriptor) {
				addGroup((GroupDescriptor) o, parent);
			} else if (o instanceof TabFolderDescriptor) {
				addFolder((TabFolderDescriptor) o, parent);
			}
		}
	}

	private void addFolder(TabFolderDescriptor fd, Composite parent) {
		TabFolder folder = new TabFolder(parent, WidgetBuilderUtils.getStyle(fd.getStyle()));
		List<TabItemDescriptor> items = fd.getItem();
		for (TabItemDescriptor i : items) {
			addItem(folder, i);
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
			}
		}
	}

	private void addItem(TabFolder folder, TabItemDescriptor i) {

	}

	private void addWidget(Widget o, Group group) {

	}

}
