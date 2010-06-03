package org.eclipse.ptp.etfw.tau.selinst.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.selinst.messages.messages"; //$NON-NLS-1$
	public static String AtomicInstrument_EnterStaticValueOrVariable;
	public static String AtomicInstrument_EnterUniqueName;
	public static String AtomicInstrument_EnterValidDoubOrVar;
	public static String AtomicInstrument_EnterValidText;
	public static String AtomicInstrument_UserDefinedEventName;
	public static String AtomicInstrument_UserDefinedEventValue;
	public static String Clear_InvalidSelection;
	public static String Clear_NoSelection;
	public static String IncrementInstrument_Cancel;
	public static String IncrementInstrument_EnterUniqueName;
	public static String IncrementInstrument_EnterValidText;
	public static String IncrementInstrument_InstTypeSelect;
	public static String IncrementInstrument_SelectAreaToInstrument;
	public static String IncrementInstrument_SelectOneOfFollowing;
	public static String IncrementInstrument_UserDefEventName;
	public static String SelectiveInstrument_InvalidSelection;
	public static String SelectiveInstrument_NoSelection;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
