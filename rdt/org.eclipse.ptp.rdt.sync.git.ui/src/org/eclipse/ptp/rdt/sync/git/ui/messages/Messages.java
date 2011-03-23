/*******************************************************************************
 * Copyright (c) 2011 The University of Tennessee and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.sync.git.ui.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.rdt.sync.git.ui.messages.messages"; //$NON-NLS-1$
	public static String GitParticipant_browse;
	public static String GitParticipant_connection;
	public static String GitParticipant_location;
	public static String GitParticipant_new;
	public static String GitParticipant_remoteProvider;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
