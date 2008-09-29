package org.eclipse.ptp.cell.managedbuilder.core;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class PDTBuildProperties {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.managedbuilder.core.pdt"; //$NON-NLS-1$

	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle
			.getBundle(BUNDLE_NAME);

	public static final String SPU_PDT_LIBRARY = PDTBuildProperties
			.getString("SPUPDTLibrary"); //$NON-NLS-1$

	public static final String PDT_MAIN_SYMBOL = PDTBuildProperties
			.getString("PDTMainSymbol"); //$NON-NLS-1$

	public static final String PDT_EXIT_SYMBOL = PDTBuildProperties
			.getString("PDTExitSymbol"); //$NON-NLS-1$

	public static final String MFCIO_TRACE_SYMBOL = PDTBuildProperties
			.getString("MFCIOTraceSymbol"); //$NON-NLS-1$

	public static final String SPU_PDT_INCLUDE_PATH = PDTBuildProperties
			.getString("SPUPDTIncludePath"); //$NON-NLS-1$ 

	private PDTBuildProperties() {
		// Hide constructor to prevent instances.
	}

	public static String getString(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return '!' + key + '!';
		}
	}
}
