package org.eclipse.ptp.rm.ibm.lsf.ui.widgets;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	public static String ApplicationCommandDesc;
	public static String ApplicationQueryControl_ApplicationCommandDesc;
	public static String ApplicationQueryTitle;
	public static String CommandCancelMessage;
	public static String ErrorMessage;
	public static String InformationalMessage;
	public static String WarningMessageLabel;
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.ibm.lsf.ui.widgets.messages"; //$NON-NLS-1$
	public static String JobQueueTitle;
	public static String LSFCommandFailed;
	public static String NoProfileMessage;
	public static String NoReservationMessage;
	public static String OkMessage;
	public static String QueueCommandDesc;
	public static String ReservationCommandDesc;
	public static String ReservationQueryTitle;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
