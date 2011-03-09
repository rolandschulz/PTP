package org.eclipse.ptp.rm.jaxb.ui.data;

import java.util.List;
import java.util.Map;

import org.eclipse.ptp.rm.jaxb.core.data.LaunchTab;
import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.listener.JAXBRMLaunchTabWidgetListener;
import org.eclipse.swt.widgets.Composite;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final LaunchTab launchTabData;
	private final JAXBRMLaunchTabWidgetListener listener;
	private final Map<Object, String> widgetToValueIndex;

	public LaunchTabBuilder(LaunchTab launchTabData, JAXBRMLaunchTabWidgetListener listener, Map<Object, String> widgetToValueIndex) {
		this.launchTabData = launchTabData;
		this.listener = listener;
		this.widgetToValueIndex = widgetToValueIndex;
	}

	public void build(Composite parent) throws Throwable {
		if (launchTabData.isAdvancedModeEnabled()) {
			addCustomScriptButton();
		}
		List<TabController> controllers = launchTabData.getTabController();
		for (TabController c : controllers) {
			addTabController(parent, c);
		}
	}

	private void addCustomScriptButton() {
		// TODO Auto-generated method stub

	}

	private void addTabController(Composite parent, TabController c) {
		/*
		 * create the controller as in:
		 * org.eclipse.ptp.rm.ui.launch.ExtendableRMLaunchConfigurationDynamicTab
		 * a top-level "switch" between views.
		 */
		c.getTitle();

		c.getGroup();
		c.getTabFolder();

	}

}
