/****************************************************************************
 * Copyright (c) 2010, University of Florida
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Max Billingsley III - initial API and implementation
 ****************************************************************************/

package org.eclipse.ptp.etfw.ppw.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.etfw.ppw.messages.messages"; //$NON-NLS-1$
	
	public static String PPWDataManager_0;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
