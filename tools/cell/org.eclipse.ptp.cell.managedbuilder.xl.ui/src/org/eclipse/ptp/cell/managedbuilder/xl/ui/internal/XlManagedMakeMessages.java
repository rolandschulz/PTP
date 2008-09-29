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
package org.eclipse.ptp.cell.managedbuilder.xl.ui.internal;

import org.eclipse.osgi.util.NLS;

/**
 * @author laggarcia
 * @since 3.0.0
 */
public class XlManagedMakeMessages extends NLS {

	private static String BUNDLE_ID = "org.eclipse.ptp.cell.managedbuilder.xl.ui.internal.messages"; //$NON-NLS-1$
	
	public static String VmxWarningDialogTitle;
	
	public static String VmxWarningDialogMessage;

	public static String AltivecWarningDialogTitle;

	public static String AltivecWarningDialogMessage;
	
	static {
		// load message values from bundle file
		NLS.initializeMessages(BUNDLE_ID, XlManagedMakeMessages.class);
	}
	
	private XlManagedMakeMessages() {
		// cannot create new instance
	}

}
