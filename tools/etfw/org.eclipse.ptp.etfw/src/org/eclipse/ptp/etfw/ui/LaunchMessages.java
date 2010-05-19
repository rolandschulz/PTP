package org.eclipse.ptp.etfw.ui;

import org.eclipse.osgi.util.NLS;

public class LaunchMessages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.ui.LaunchMessages"; //$NON-NLS-1$
	public static String ToolRecompMainTab_BuildConfNoExist;
	public static String ToolRecompMainTab_BuildConfNoSpeced;
	public static String ToolRecompMainTab_LangBuildConf;
	public static String ToolRecompMainTab_NoValidConfs;
	public static String ToolRecompMainTab_ProjNoValidBuildInfo;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, LaunchMessages.class);
	}

	private LaunchMessages() {
	}
}
