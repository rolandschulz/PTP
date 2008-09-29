package org.eclipse.ptp.cell.pdt.xml.core;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.pdt.xml.core.messages"; //$NON-NLS-1$
	public static String PdtEventForestFactory_CreateEventForest_Error_InvalidElementType;
	public static String PdtEventForestFactory_createEventGroupForest_InvalidElementType;
	public static String PdtEventForestFactory_createEventGroupForest_PathNotADir;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
