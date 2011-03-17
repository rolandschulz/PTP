package org.eclipse.ptp.rm.jaxb.ui.util;

import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.Group;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.core.data.TabFolder;
import org.eclipse.ptp.rm.jaxb.core.data.Widget;
import org.eclipse.ptp.rm.jaxb.core.variables.RMVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;

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
		List<Object> top = tabController.getTabFolderOrGroup();
		for (Object o : top) {
			if (o instanceof Group) {
				addGroup((Group) o, parent);
			} else if (o instanceof TabFolder) {
				addFolder((TabFolder) o, parent);
			}
		}
	}

	private void addFolder(TabFolder folder, Composite parent) {
		WidgetBuilderUtils.createTabFolder(folder.getTitle(), parent);
	}

	private void addGroup(Group group, Composite parent) {
		// TODO Auto-generated method stub

	}
}
