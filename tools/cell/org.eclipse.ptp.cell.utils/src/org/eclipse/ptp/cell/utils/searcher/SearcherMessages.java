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
package org.eclipse.ptp.cell.utils.searcher;

import org.eclipse.osgi.util.NLS;

/**
 * 
 * @author laggarcia
 * @since 3.0.0
 */
public class SearcherMessages extends NLS {
	
	private static final String BUNDLE_NAME = "org.eclipse.ptp.cell.utils.searcher.messages"; //$NON-NLS-1$

	public static String noSearchEngine;
	
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, SearcherMessages.class);
	}

	private SearcherMessages() {
		// Private constructor to prevent instances of this class.
	}
}
