package org.eclipse.ptp.pldt.mpi.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;
// FIXME for 5.0 export these as more modern Messages class (in private package)
public class Messages {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.mpi.core.messages"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
