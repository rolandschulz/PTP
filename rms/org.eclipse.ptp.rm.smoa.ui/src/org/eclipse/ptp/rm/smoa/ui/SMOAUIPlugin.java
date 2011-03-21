/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui;

import org.eclipse.ptp.rm.smoa.core.rmsystem.PoolingIntervalsAndStatic;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * the plug-in
 */
public class SMOAUIPlugin extends AbstractUIPlugin {

	/** plug-in ID */
	public static final String PLUGIN_ID = "org.eclipse.ptp.rm.smoa.ui"; //$NON-NLS-1$

	/** shared instance */
	private static SMOAUIPlugin plugin;

	public static final String KEY_INTERVAL_TASK = "pooling_interval_task"; //$NON-NLS-1$
	public static final String KEY_INTERVAL_OUT = "pooling_interval_out"; //$NON-NLS-1$

	/** Returns the shared instance */
	public static SMOAUIPlugin getDefault() {
		return plugin;
	}

	public SMOAUIPlugin() {
		super();
		plugin = this;
	}

	/**
	 * Starts the plug-in and applies user preferences
	 */
	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);

		PoolingIntervalsAndStatic.setPoolingIntervalTask(getPreferenceStore().getInt(
				KEY_INTERVAL_TASK));
		PoolingIntervalsAndStatic.setPoolingIntervalOut(getPreferenceStore().getInt(
				KEY_INTERVAL_OUT));
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

}
