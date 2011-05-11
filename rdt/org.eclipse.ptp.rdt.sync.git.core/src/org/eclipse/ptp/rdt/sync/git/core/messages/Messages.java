package org.eclipse.ptp.rdt.sync.git.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.git.core.messages.messages"; //$NON-NLS-1$
	public static String CR_CreateInstanceError;
	public static String GRSC_CommitMessage;
	public static String GRSC_GitAddFailure;
	public static String GRSC_GitCommitFailure;
	public static String GRSC_GitInitFailure;
	public static String GRSC_GitLsFilesFailure;
	public static String GRSC_GitMergeFailure;
	public static String GRSC_GitRmFailure;
	public static String GSP_ChangeConnectionError;
	public static String GSP_ChangeLocationError;
	public static String GSP_ChangeProjectError;
	public static String GSP_SyncErrorMessage;
	public static String GSP_SyncTaskName;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
