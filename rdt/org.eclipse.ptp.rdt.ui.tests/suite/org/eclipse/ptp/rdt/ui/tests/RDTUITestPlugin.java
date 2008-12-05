package org.eclipse.ptp.rdt.ui.tests;

import org.eclipse.ui.plugin.AbstractUIPlugin;

public class RDTUITestPlugin extends AbstractUIPlugin {

	
	private static RDTUITestPlugin plugin;
	
	public RDTUITestPlugin() {
		super();
		plugin = this;
	}

	public static RDTUITestPlugin getDefault() {
		return plugin;
	}
}
