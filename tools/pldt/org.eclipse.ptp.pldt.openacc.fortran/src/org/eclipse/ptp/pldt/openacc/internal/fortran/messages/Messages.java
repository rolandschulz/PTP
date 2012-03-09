/**********************************************************************
 * Copyright (c) 2010-2012 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (Illinois) - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.fortran.messages;

import org.eclipse.osgi.util.NLS;

/** NLS messages */
@SuppressWarnings("javadoc")
public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openacc.internal.fortran.messages"; //$NON-NLS-1$

	public static String OpenACCCHelpBook_Description_acc_async_test;
	public static String OpenACCCHelpBook_Description_acc_async_test_all;
	public static String OpenACCCHelpBook_Description_acc_async_wait;
	public static String OpenACCCHelpBook_Description_acc_async_wait_all;
	public static String OpenACCCHelpBook_Description_acc_free;
	public static String OpenACCCHelpBook_Description_acc_get_device_num;
	public static String OpenACCCHelpBook_Description_acc_get_device_type;
	public static String OpenACCCHelpBook_Description_acc_get_num_devices;
	public static String OpenACCCHelpBook_Description_acc_init;
	public static String OpenACCCHelpBook_Description_acc_malloc;
	public static String OpenACCCHelpBook_Description_acc_on_device;
	public static String OpenACCCHelpBook_Description_acc_set_device_num;
	public static String OpenACCCHelpBook_Description_acc_set_device_type;
	public static String OpenACCCHelpBook_Description_acc_shutdown;

	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
