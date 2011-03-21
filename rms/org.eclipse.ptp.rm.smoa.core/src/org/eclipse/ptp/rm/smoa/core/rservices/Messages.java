/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.core.rservices;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rm.smoa.core.rservices.messages"; //$NON-NLS-1$
	public static String SMOAConnection_CannotModifyOpenConnection;
	public static String SMOAConnection_UnsupportedAuthType;
	public static String SMOAConnectionManager_DuplicatedConnection;
	public static String SMOAFileStore_ChmodFailed;
	public static String SMOAFileStore_InputStreamForFileNotReceived;
	public static String SMOAFileStore_MkdirOverAnExistingFile;
	public static String SMOAFileStore_RequestedListingUnexistingDirOrFile;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
