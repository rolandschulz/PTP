package org.eclipse.ptp.utils.ui.swt;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.utils.ui.swt.messages"; //$NON-NLS-1$
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
	public static String Frame_ExpandButtonLabel;
	public static String Frame_NoDescriptionSet;
	public static String Frame_NoEnclosingGroup;
	public static String Frame_NoExpandButton;
	public static String Frame_ShrinkButtonLable;
	public static String GenericControlGroup_NoControlGroupButton;
	public static String GenericControlGroup_NoControlGroupLabel;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
