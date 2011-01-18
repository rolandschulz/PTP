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
package org.eclipse.ptp.pldt.lapi.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.lapi.messages.messages"; //$NON-NLS-1$
	public static String LapiArtifactView_construct_column_title;
	public static String LapiArtifactView_lapi_artifact_column_title;
	public static String LapiArtifactView_lapi_artifacts_plural;
	public static String LapiCASTVisitor_lapi_call;
	public static String LapiCASTVisitor_lapi_constant;
	public static String LapiIDs_lapi_includes_pref_page_title;
	public static String LAPIPreferencePage_includes_preference_browse_dialog_title;
	public static String LAPIPreferencePage_includes_preference_label;
	public static String LAPIPreferencePage_recognizeAPISByPrefixAlone;
	
	public static String LapiCHelpBook_LAPI_Addr_get;
	public static String LapiCHelpBook_LAPI_Addr_set;
	public static String LapiCHelpBook_LAPI_Address;
	public static String LapiCHelpBook_LAPI_Address_init;
	public static String LapiCHelpBook_LAPI_Address_init64;
	public static String LapiCHelpBook_LAPI_Amsend;
	public static String LapiCHelpBook_LAPI_Amsendv;
	public static String LapiCHelpBook_LAPI_c_help_book_title;
	public static String LapiCHelpBook_LAPI_Fence;
	public static String LapiCHelpBook_LAPI_Get;
	public static String LapiCHelpBook_LAPI_Getcntr;
	public static String LapiCHelpBook_LAPI_Getv;
	public static String LapiCHelpBook_LAPI_Gfence;
	public static String LapiCHelpBook_LAPI_Init;
	public static String LapiCHelpBook_LAPI_Msg_string;
	public static String LapiCHelpBook_LAPI_Msgpoll;
	public static String LapiCHelpBook_LAPI_Nopoll_wait;
	public static String LapiCHelpBook_LAPI_Probe;
	public static String LapiCHelpBook_LAPI_Purge_totask;
	public static String LapiCHelpBook_LAPI_Put;
	public static String LapiCHelpBook_LAPI_Putv;
	public static String LapiCHelpBook_LAPI_Qenv;
	public static String LapiCHelpBook_LAPI_Resume_totask;
	public static String LapiCHelpBook_LAPI_Rmw;
	public static String LapiCHelpBook_LAPI_Rmw64;
	public static String LapiCHelpBook_LAPI_Senv;
	public static String LapiCHelpBook_LAPI_Setcntr;
	public static String LapiCHelpBook_LAPI_Setcntr_wstatus;
	public static String LapiCHelpBook_LAPI_Term;
	public static String LapiCHelpBook_LAPI_Util;
	public static String LapiCHelpBook_LAPI_Waitcntr;
	public static String LapiCHelpBook_LAPI_Xfer;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
