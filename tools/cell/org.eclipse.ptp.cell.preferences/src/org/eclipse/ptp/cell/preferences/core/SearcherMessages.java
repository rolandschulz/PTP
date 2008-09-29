/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *****************************************************************************/
package org.eclipse.ptp.cell.preferences.core;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class SearcherMessages extends NLS {

	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.preferences.core.messages"; //$NON-NLS-1$

	public static String fastSearchFailedMessage;

	public static String longSearchConfirmationDialogMessage;

	public static String longSearchConfirmationDialogTitle;

	public static String searchSucceededDialogTitle;
	
	public static String searchSucceededDialogMessage;
	
	public static String searchFailedDialogTitle;
	
	public static String searchFailedDialogMessage;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SearcherMessages.class);
	}

	private SearcherMessages() {
	}
}
