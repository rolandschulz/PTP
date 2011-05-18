package org.eclipse.ptp.utils.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.utils.ui.messages.messages"; //$NON-NLS-1$
	public static String AuthenticationFrame_EmptyHostAddressException;
	public static String AuthenticationFrame_EmptyHostPortException;
	public static String AuthenticationFrame_EmptyPrivateKeyPathException;
	public static String AuthenticationFrame_EmptyUsernameException;
	public static String AuthenticationFrame_FileDoesNotExistException;
	public static String AuthenticationFrame_FileNotReadableException;
	public static String AuthenticationFrame_Host;
	public static String AuthenticationFrame_InvalidCipherTypeException;
	public static String AuthenticationFrame_PathIsNotFileExcpetion;
	public static String AuthenticationFrame_PortNumberException;
	public static String AuthenticationFrame_TimeoutException;
	public static String BrowseButtonText;
	public static String Frame_0;
	public static String Frame_1;
	public static String Frame_NoDescriptionSet;
	public static String Frame_NoEnclosingGroup;
	public static String Frame_NoExpandButton;
	public static String GenericControlGroup_NoControlGroupButton;
	public static String GenericControlGroup_NoControlGroupLabel;
	public static String AuthenticationFrameMold_Button_HideAdvanced;
	public static String AuthenticationFrameMold_Button_PrivateKeyPath;
	public static String AuthenticationFrameMold_Button_ShowAdvanced;
	public static String AuthenticationFrameMold_Combo_CipherType;
	public static String AuthenticationFrameMold_Field_Host;
	public static String AuthenticationFrameMold_Field_Passphrase;
	public static String AuthenticationFrameMold_Field_Password;
	public static String AuthenticationFrameMold_Field_Port;
	public static String AuthenticationFrameMold_Field_Timeout;
	public static String AuthenticationFrameMold_Field_User;
	public static String AuthenticationFrameMold_Option_localhost;
	public static String AuthenticationFrameMold_Option_PasswordAuthentication;
	public static String AuthenticationFrameMold_Option_PublicKeyAuth;
	public static String AuthenticationFrameMold_Option_Remotehost;
	public static String AuthenticationFrameMold_SelectionWindow_Label_PrivateKeyPath;
	public static String AuthenticationFrameMold_SelectionWindow_Title_PrivateKeyPath;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
