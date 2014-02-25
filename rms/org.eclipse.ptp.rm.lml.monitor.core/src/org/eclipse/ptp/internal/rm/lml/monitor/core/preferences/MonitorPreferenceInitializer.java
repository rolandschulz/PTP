/**
 * Copyright (c) 2014 Forschungszentrum Juelich GmbH
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * 		Carsten Karbach, FZ Juelich
 */
package org.eclipse.ptp.internal.rm.lml.monitor.core.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.ptp.internal.rm.lml.monitor.core.LMLMonitorCorePlugin;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorCoreConstants;

/**
 * Sets default values for all preferences available for this plug-in.
 * This is necessary, because the preference page might never be presented
 * to the user, but there should be default values present right from the beginning.
 */
public class MonitorPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		IEclipsePreferences preferences = DefaultScope.INSTANCE.getNode(LMLMonitorCorePlugin.PLUGIN_ID);

		preferences.putInt(IMonitorCoreConstants.PREF_UPDATE_INTERVAL, IMonitorCoreConstants.PREF_UPDATE_INTERVAL_DEFAULT);
		preferences.putBoolean(IMonitorCoreConstants.PREF_FORCE_UPDATE, IMonitorCoreConstants.PREF_FORCE_UPDATE_DEFAULT);
	}
}
