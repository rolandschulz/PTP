package org.eclipse.ptp.etfw.toolopts;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.toolopts.messages"; //$NON-NLS-1$
	public static String ToolMaker_AtLine;
	public static String ToolMaker_Browse;
	public static String ToolMaker_Column;
	public static String ToolMaker_ErrorInWorkflowDefinition;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
