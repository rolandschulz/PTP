package org.eclipse.ptp.rm.jaxb.ui.data;

import org.eclipse.ptp.rm.jaxb.core.data.TabController;
import org.eclipse.ptp.rm.jaxb.ui.IJAXBUINonNLSConstants;
import org.eclipse.swt.widgets.Composite;

public class LaunchTabBuilder implements IJAXBUINonNLSConstants {

	private final TabController tabController;

	public LaunchTabBuilder(TabController tabController) {
		this.tabController = tabController;
	}

	public void build(Composite parent) throws Throwable {

	}
}
