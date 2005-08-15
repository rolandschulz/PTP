package org.eclipse.ptp.debug.internal.ui.propertypages;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PropertyPageMessages {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.debug.internal.ui.propertypages.PropertyPageMessages";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private PropertyPageMessages() {}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch(MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
