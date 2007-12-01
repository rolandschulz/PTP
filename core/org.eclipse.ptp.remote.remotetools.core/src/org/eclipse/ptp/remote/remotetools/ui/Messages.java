package org.eclipse.ptp.remote.remotetools.ui;

import org.eclipse.osgi.util.NLS;

/**
 * @author Daniel Felix Ferber
 */
public class Messages extends NLS {
	private static String BUNDLE_NAME = "org.eclipse.ptp.remote.remotetools.ui.messages"; //$NON-NLS-1$

	public static String ConfigurationDialog_LabelRemoteHost;
	
	public static String ConfigurationDialog_LabelHideAdvancedOptions;

	public static String ConfigurationDialog_LabelHostAddress;

	public static String ConfigurationDialog_LabelHostPort;

	public static String ConfigurationDialog_LabelIsPasswordBased;

	public static String ConfigurationDialog_LabelIsPublicKeyBased;

	public static String ConfigurationDialog_LabelPassphrase;

	public static String ConfigurationDialog_LabelPassword;

	public static String ConfigurationDialog_LabelPublicKeyPath;

	public static String ConfigurationDialog_LabelPublicKeyPathButton;

	public static String ConfigurationDialog_LabelUserName;

	public static String ConfigurationDialog_LabelTimeout;

	public static String ConfigurationDialog_LabelShowAdvancedOptions;

	public static String ConfigurationDialog_LabelPublicKeyPathTitle;

	public static String ConfigurationDialog_ConnectionFrameTitle;

	public static String ConfigurationDialog_DefaultTargetName;

	public static String ConfigurationDialog_DialogDescription;

	public static String ConfigurationDialog_DialogTitle;

	public static String ConfigurationDialog_LabelConnectionName;

	public static String ConfigurationDialog_CipherType;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
