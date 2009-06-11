package org.eclipse.ptp.debug.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.debug.ui.messages.messages"; //$NON-NLS-1$
	
	public static String ResumeAction_0;

	public static String StepIntoAction_0;
	public static String StepOverAction_0;
	public static String StepReturnAction_0;
	public static String SuspendAction_0;
	public static String TerminateAction_0;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
