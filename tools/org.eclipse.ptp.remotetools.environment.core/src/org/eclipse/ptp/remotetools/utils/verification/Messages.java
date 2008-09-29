package org.eclipse.ptp.remotetools.utils.verification;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.verification.messages"; //$NON-NLS-1$
	public static String AttributeVerification_ConfigurationOK;
	public static String AttributeVerification_ErrorMessage;
	public static String AttributeVerification_InvalidConfiguration;
	public static String AttributeVerification_InvalidPath;
	public static String AttributeVerification_MustBeLocalFile;
	public static String AttributeVerification_NotAnAbsolutePath;
	public static String AttributeVerification_NoValue;
	public static String AttributeVerification_PathDoesNotExist;
	public static String AttributeVerification_PathIsNotDir;
	public static String AttributeVerification_PathIsNotExecutableFile;
	public static String AttributeVerification_PathIsNotFile;
	public static String AttributeVerification_PathIsNotWritable;
	public static String AttributeVerification_PathNotFound;
	public static String ControlAttributes_InvalidDecimalNumber;
	public static String ControlAttributes_InvalidIntegerNumber;
	public static String ControlAttributes_InvalidPath;
	public static String ControlAttributes_MustNotBeEmpty;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
