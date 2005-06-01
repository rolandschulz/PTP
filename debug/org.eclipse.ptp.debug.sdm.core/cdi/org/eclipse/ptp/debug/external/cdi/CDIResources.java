package org.eclipse.ptp.debug.external.cdi;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class CDIResources {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.debug.mi.core.cdi.CDIResources";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private CDIResources() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
