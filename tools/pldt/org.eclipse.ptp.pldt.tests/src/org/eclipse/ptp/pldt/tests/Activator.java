package org.eclipse.ptp.pldt.tests;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
//http://wiki.eclipse.org/Eclipse_Plug-in_Development_FAQ#How_do_I_read_from_a_file_that_I.27ve_included_in_my_bundle.2Fplug-in.3F
//your BundleActivator implementation will probably look something
//like the following

public class Activator implements BundleActivator {
	private static Activator instance;

	private Bundle bundle;

	public void start(BundleContext context) throws Exception {
		instance = this;
		bundle = context.getBundle();
	}

	public void stop(BundleContext context) throws Exception {
		instance = null;
	}

	public static Activator getDefault() {
		return instance;
	}

	public Bundle getBundle() {
		return bundle;
	}

}
