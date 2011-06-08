package org.eclipse.ptp.rm.lml.internal.core.elements;

import org.eclipse.osgi.util.NLS;

/**
 * @author arossi
 *
 */
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.lml.internal.core.elements.messages"; //$NON-NLS-1$
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}

	public static String ColumnType_0;
	public static String SortingType_0;
	public static String SortingType_1;
	public static String SortingType_2;
	public static String UsagebarlayoutType_0;
	public static String UsagebarlayoutType_1;
}
