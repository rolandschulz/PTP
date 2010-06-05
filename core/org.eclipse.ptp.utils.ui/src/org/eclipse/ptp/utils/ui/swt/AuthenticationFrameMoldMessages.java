/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.utils.ui.swt;

import org.eclipse.osgi.util.NLS;

public class AuthenticationFrameMoldMessages extends NLS {
	// FIXME wrong reference to bundle
	private static final String BUNDLE_NAME = "org.eclipse.ptp.utils.ui.swt.auth_frame_messages"; //$NON-NLS-1$

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, AuthenticationFrameMoldMessages.class);
	}

	private AuthenticationFrameMoldMessages() {
	}
}
