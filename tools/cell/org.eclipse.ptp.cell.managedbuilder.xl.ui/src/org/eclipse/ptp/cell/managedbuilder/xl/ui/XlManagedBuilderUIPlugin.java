/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.managedbuilder.xl.ui;

import java.util.Date;

import org.eclipse.ptp.cell.managedbuilder.gnu.ui.GnuManagedBuilderUIPlugin;
import org.eclipse.ptp.cell.managedbuilder.xl.core.XlToolChainEnvironmentSupplier;
import org.eclipse.ptp.cell.managedbuilder.xl.ui.debug.Debug;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * @author laggarcia
 *
 */
public class XlManagedBuilderUIPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static XlManagedBuilderUIPlugin plugin;
	
	/**
	 * The constructor.
	 */
	public XlManagedBuilderUIPlugin() {
		plugin = this;
	}
	
	/**
	 * Returns the shared instance.
	 */
	public static XlManagedBuilderUIPlugin getDefault() {
		return plugin;
	}
	
	public void start(BundleContext context) throws Exception {
		super.start(context);
		XlToolChainEnvironmentSupplier.setXlPreferenceStore(getPreferenceStore());
		XlToolChainEnvironmentSupplier.setGnuPreferenceStore(GnuManagedBuilderUIPlugin.getDefault().getPreferenceStore());
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle started at {0}", date.toString()); //$NON-NLS-1$
		}
	}
	
	public void stop(BundleContext context) throws Exception {
		super.stop(context);
		plugin = null;
		
		Debug.read();
		if (Debug.DEBUG) {
			Date date = new Date();
			Debug.POLICY.trace("Bundle stopped at {0}", date.toString()); //$NON-NLS-1$
		}
	}
}
