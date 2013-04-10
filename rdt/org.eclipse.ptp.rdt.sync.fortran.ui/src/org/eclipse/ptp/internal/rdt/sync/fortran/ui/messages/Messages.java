/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.fortran.ui.messages;

import org.eclipse.osgi.util.NLS;

@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.internal.rdt.sync.fortran.ui.messages.messages"; //$NON-NLS-1$
	public static String NewFortranSyncProjectWizard_create_sync_fortran_project;
	public static String NewFortranSyncProjectWizard_new_sync_fortran_project;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
