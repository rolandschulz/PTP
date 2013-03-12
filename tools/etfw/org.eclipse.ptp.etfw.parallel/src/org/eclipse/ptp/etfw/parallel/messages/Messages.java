package org.eclipse.ptp.etfw.parallel.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.parallel.messages.messages"; //$NON-NLS-1$
	public static String ParallelToolLaunchConfigurationDelegate_Launching;
	public static String ParallelToolLaunchConfigurationDelegate_ProfilingToolNotSpecified;
	public static String ParallelToolRecompMainTab_BuildConfNotSpeced;
	public static String ParallelToolRecompMainTab_LangBuildConf;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
