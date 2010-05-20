/**********************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.upc.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.upc.messages.messages"; //$NON-NLS-1$
	public static String UPCCHelpBook_upc_addrfield;
	public static String UPCCHelpBook_upc_affinitysize;
	public static String UPCCHelpBook_upc_all_alloc;
	public static String UPCCHelpBook_upc_all_broadcast;
	public static String UPCCHelpBook_upc_all_exchange;
	public static String UPCCHelpBook_upc_all_gather;
	public static String UPCCHelpBook_upc_all_gather_all;
	public static String UPCCHelpBook_upc_all_lock_alloc;
	public static String UPCCHelpBook_upc_all_permute;
	public static String UPCCHelpBook_upc_all_scatter;
	public static String UPCCHelpBook_upc_alloc;
	public static String UPCCHelpBook_upc_c_help_book_title;
	public static String UPCCHelpBook_upc_description;
	public static String UPCCHelpBook_upc_free;
	public static String UPCCHelpBook_upc_global_alloc;
	public static String UPCCHelpBook_upc_global_exit;
	public static String UPCCHelpBook_upc_global_lock_alloc;
	public static String UPCCHelpBook_upc_local_alloc;
	public static String UPCCHelpBook_upc_lock;
	public static String UPCCHelpBook_upc_lock_attempt;
	public static String UPCCHelpBook_upc_lock_free;
	public static String UPCCHelpBook_upc_lock_t;
	public static String UPCCHelpBook_upc_memcpy;
	public static String UPCCHelpBook_upc_memget;
	public static String UPCCHelpBook_upc_memset;
	public static String UPCCHelpBook_upc_phaseof;
	public static String UPCCHelpBook_upc_resetphase;
	public static String UPCCHelpBook_upc_shared;
	public static String UPCCHelpBook_upc_threadof;
	public static String UPCCHelpBook_upc_unlock;
	public static String UPCArtifactView_construct;
	public static String UPCArtifactView_upc_artifact;
	public static String UPCArtifactView_upc_artifacts;
	public static String UPCCASTVisitor_upc_call;
	public static String UPCCASTVisitor_upc_constant;
	public static String UPCIDs_upc_includes;
	public static String UPCPreferencePage_location_of_upc_help_files;
	public static String UPCPreferencePage_please_choose_a_directory;
	public static String UPCPreferencePage_preference_label_upc_include_paths;
	public static String UPCPreferencePage_use_default;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
