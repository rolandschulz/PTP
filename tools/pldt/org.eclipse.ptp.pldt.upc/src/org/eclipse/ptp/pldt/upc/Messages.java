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
package org.eclipse.ptp.pldt.upc;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.upc.messages"; //$NON-NLS-1$
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
