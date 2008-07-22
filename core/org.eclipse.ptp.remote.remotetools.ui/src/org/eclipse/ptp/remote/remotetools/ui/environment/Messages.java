package org.eclipse.ptp.remote.remotetools.ui.environment;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Felix Ferber
 */
public class Messages extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.ptp.remote.remotetools.ui.environment.messages"; //$NON-NLS-1$

	public static String ConfigurationPage_LabelLocalhost;
	
	public static String ConfigurationPage_LabelRemoteHost;
	
	public static String ConfigurationPage_LabelHideAdvancedOptions;

	public static String ConfigurationPage_LabelHostAddress;

	public static String ConfigurationPage_LabelHostPort;

	public static String ConfigurationPage_LabelIsPasswordBased;

	public static String ConfigurationPage_LabelIsPublicKeyBased;

	public static String ConfigurationPage_LabelPassphrase;

	public static String ConfigurationPage_LabelPassword;

	public static String ConfigurationPage_LabelPublicKeyPath;

	public static String ConfigurationPage_LabelPublicKeyPathButton;

	public static String ConfigurationPage_LabelUserName;

	public static String ConfigurationPage_LabelTimeout;

	public static String ConfigurationPage_LabelShowAdvancedOptions;

	public static String ConfigurationPage_LabelPublicKeyPathTitle;

	public static String ConfigurationPage_ConnectionFrameTitle;

	public static String ConfigurationPage_DefaultTargetName;

	public static String ConfigurationPage_DialogDescription;

	public static String ConfigurationPage_DialogTitle;

	public static String ConfigurationPage_LabelTargetName;

	public static String ConfigurationPage_CipherType;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
