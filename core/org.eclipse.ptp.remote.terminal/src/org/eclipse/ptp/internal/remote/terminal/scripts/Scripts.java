package org.eclipse.ptp.internal.remote.terminal.scripts;

import org.eclipse.osgi.util.NLS;

public class Scripts extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.remote.terminal.scripts.scripts"; //$NON-NLS-1$

	/**
	 * This perl script monitors the ~/.history file on the remote machine
	 * to see if csh or tcsh has updated it, and if so if it figures out
	 * what the new commands are.
	 */
	public static String WATCH_CSH_HISTORY_PERL_SCRIPT;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Scripts.class);
		WATCH_CSH_HISTORY_PERL_SCRIPT = WATCH_CSH_HISTORY_PERL_SCRIPT.replaceAll("\\s", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}

	private Scripts() {
	}
}
