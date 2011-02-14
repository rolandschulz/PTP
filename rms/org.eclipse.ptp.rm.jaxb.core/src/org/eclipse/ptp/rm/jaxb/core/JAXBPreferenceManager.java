package org.eclipse.ptp.rm.jaxb.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.AbstractToolsPreferenceManager;

public class JAXBPreferenceManager extends AbstractToolsPreferenceManager {

	@Override
	public void initializeDefaultPreferences() {
		try {
			JAXBDefaults.loadDefaults();
		} catch (CoreException e) {
			JAXBCorePlugin.log(e);
		}
		Preferences.setDefaultString(JAXBCorePlugin.getUniqueIdentifier(), PREFS_LAUNCH_CMD, JAXBDefaults.LAUNCH_CMD);
		Preferences.setDefaultString(JAXBCorePlugin.getUniqueIdentifier(), PREFS_DEBUG_CMD, JAXBDefaults.DEBUG_CMD);
		Preferences.setDefaultString(JAXBCorePlugin.getUniqueIdentifier(), PREFS_REMOTE_INSTALL_PATH, JAXBDefaults.PATH);
	}

	public static void savePreferences() {
		Preferences.savePreferences(JAXBCorePlugin.getUniqueIdentifier());
	}

}
