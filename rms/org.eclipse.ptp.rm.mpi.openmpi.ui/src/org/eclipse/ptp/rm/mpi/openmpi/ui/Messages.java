package org.eclipse.ptp.rm.mpi.openmpi.ui;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.mpi.openmpi.ui.messages"; //$NON-NLS-1$
	public static String OpenMPIUIPlugin_Exception_InternalError;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
		// Prevent instances.
	}
}
