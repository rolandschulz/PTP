package org.eclipse.ptp.etfw.parallel;

import org.eclipse.osgi.util.NLS;

public class LaunchMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.parallel.LaunchMessages"; //$NON-NLS-1$
	public static String ParallelToolRecompMainTab_BuildConfNotSpeced;
	public static String ParallelToolRecompMainTab_LangBuildConf;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LaunchMessages.class);
	}

	private LaunchMessages() {
	}
}
