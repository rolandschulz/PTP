package org.eclipse.ptp.cell.environment.launcher.pdt.internal;

import org.eclipse.osgi.util.NLS;

public class DefaultValues extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.environment.launcher.pdt.internal.defaultvalues"; //$NON-NLS-1$
	public static String IPdtLaunchAttributes_DEFAULT_LOCAL_TRACE_DIR;
	public static String IPdtLaunchAttributes_DEFAULT_LOCAL_XML_FILE;
	public static String IPdtLaunchAttributes_DEFAULT_PDT_MODULE_PATH;
	public static String IPdtLaunchAttributes_DEFAULT_REMOTE_TRACE_DIR;
	public static String IPdtLaunchAttributes_DEFAULT_REMOTE_XML_DIR;
	public static String IPdtLaunchAttributes_DEFAULT_REMOTE_XML_FILE;
	public static String IPdtLaunchAttributes_DEFAULT_TRACE_FILE_PREFIX;
	public static String IPdtLaunchAttributes_DEFAULT_TRACE_LIB_PATH;
	public static String IPdtLaunchAttributes_DEFAULT_COPY_XML_FILE;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, DefaultValues.class);
	}

	private DefaultValues() {
	}
}
