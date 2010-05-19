package org.eclipse.ptp.etfw.tau.selinstfort.popup.actions;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.tau.selinstfort.popup.actions.messages"; //$NON-NLS-1$
	public static String IncrementInstrument_SelectAreaToInstrument;
	public static String Instrument_EnterStaticValueOrVariable;
	public static String Instrument_EnterUniqueName;
	public static String Instrument_EnterValidDoubleOrVar;
	public static String Instrument_EnterValidText;
	public static String Instrument_UserDefinedEventName;
	public static String Instrument_UserDefinedEventValue;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
