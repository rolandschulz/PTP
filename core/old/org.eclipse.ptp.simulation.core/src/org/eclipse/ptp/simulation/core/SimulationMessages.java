package org.eclipse.ptp.simulation.core;

import java.text.MessageFormat;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class SimulationMessages {
    private static final String BUNDLE_NAME = Activator.PLUGIN_ID + ".messages";
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

    /** Get string
     * @param key
     * @return
     */
    public static String getResourceString(String key) {
		// No point trying if bundle is null as exceptions are costly
		if (RESOURCE_BUNDLE != null) {
			try {
				return RESOURCE_BUNDLE.getString(key);
			} catch (MissingResourceException e) {
				return "!" + key + "!"; //$NON-NLS-1$ //$NON-NLS-2$
			} catch (NullPointerException e) {
				return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
			}
		}

		// If we get here, then bundle is null.
		return "#" + key + "#"; //$NON-NLS-1$ //$NON-NLS-2$
	}

	public static String getFormattedResourceString(String key, String arg) {
		return MessageFormat.format(getResourceString(key), new Object[]{arg});
	}

	public static String getFormattedResourceString(String key, String[] args) {
		return MessageFormat.format(getResourceString(key), (Object[])args);
	}
}
