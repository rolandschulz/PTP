package org.eclipse.ptp.debug.internal.ui.sourcelookup;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author clement
 *
 */
public class SourceLookupUIMessages {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.debug.internal.ui.sourcelookup.SourceLookupUIMessages";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private SourceLookupUIMessages() {
	}

	public static String getString(String key) {
		// TODO Auto-generated method stub
		try {
			return RESOURCE_BUNDLE.getString(key);
		}
		catch(MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
