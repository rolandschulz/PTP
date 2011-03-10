package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.ptp.rm.jaxb.ui.launch.JAXBRMLaunchConfigurationDynamicTab;
import org.eclipse.swt.widgets.Composite;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final JAXBRMLaunchConfigurationDynamicTab fLaunchTab;

	public LaunchTabBuilder(JAXBRMLaunchConfigurationDynamicTab launchTab) {

		this.fLaunchTab = launchTab;
	}

	public void build(Composite parent) throws Throwable {

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

	}

}
