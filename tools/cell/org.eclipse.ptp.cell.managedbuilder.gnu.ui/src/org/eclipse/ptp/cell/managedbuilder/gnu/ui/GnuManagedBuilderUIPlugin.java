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
package org.eclipse.ptp.cell.managedbuilder.gnu.ui;

import java.util.Date;

import org.eclipse.ptp.cell.managedbuilder.gnu.core.GnuToolChainEnvironmentSupplier;
import org.eclipse.ptp.cell.managedbuilder.gnu.ui.debug.Debug;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * @author laggarcia
 *
 */
public class GnuManagedBuilderUIPlugin extends AbstractUIPlugin {

	//The shared instance.
	private static GnuManagedBuilderUIPlugin plugin;

	/**
	 * The constructor.
	 */
	public GnuManagedBuilderUIPlugin() {
		plugin = this;
	}

	/**
	 * Returns the shared instance.
	 */
	public static GnuManagedBuilderUIPlugin getDefault() {
		return plugin;
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		GnuToolChainEnvironmentSupplier.setPreferenceStore(getPreferenceStore());
		
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
