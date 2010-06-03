package org.eclipse.ptp.etfw.tau.papiselect.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.papiselect.messages.messages"; //$NON-NLS-1$
	public static String EventTree_Events;
	public static String EventTreeDialog_EventModifiersRequired;
	public static String EventTreeDialog_PapiEventSelection;
	public static String EventTreeDialog_PleaseSelectAtLeastOneMod;
	public static String PapiListSelectionDialog_Counter;
	public static String PapiListSelectionDialog_CounterDescs;
	public static String PapiListSelectionDialog_Definition;
	public static String PapiListSelectionDialog_DeselectAll;
	public static String PapiListSelectionDialog_PapiCounters;
	public static String PapiListSelectionDialog_SelectAll;
	public static String PapiListSelectionDialog_SelectPapiCounters;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
