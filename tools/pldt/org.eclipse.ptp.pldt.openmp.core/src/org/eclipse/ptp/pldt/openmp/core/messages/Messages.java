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

package org.eclipse.ptp.pldt.openmp.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.openmp.core.messages.messages"; //$NON-NLS-1$
	public static String OpenMPArtifactView_Construct;
	public static String OpenMPArtifactView_noArtifactSelected;
	public static String OpenMPArtifactView_noSelection;
	public static String OpenMPArtifactView_OpenMP_Artifact;
	public static String OpenMPArtifactView_OpenMP_Artifacts;
	public static String OpenMPArtifactView_showPragmaRegion;
	public static String OpenMPArtifactView_showRegionForSelected;
	public static String OpenMPCASTVisitor_OpenMP_Call;
	public static String OpenMPCASTVisitor_OpenMP_Constant;
	public static String OpenMPIDs_OpenMP_includes;
	public static String OpenMPPlugin_OpenMP_includes;
	public static String OpenMPPreferencePage_OpenMP_include_paths;
	public static String OpenMPPreferencePage_PleaseChooseAdirForOpenMPincludes;
	public static String OpenMPPreferencePage_recognizeAPISByPrefixAlone;
	public static String RunAnalyseOpenMPcommandHandler_OpenMP_directive;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}

	private Messages() {
	}
}
