/**********************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.mpi.core.messages;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "org.eclipse.ptp.pldt.mpi.core.messages"; //$NON-NLS-1$

	// private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME);

	private Messages() {
	}

	/*
	 * public static String getStringHIDE(String key) {
	 * try {
	 * return RESOURCE_BUNDLE.getString(key);
	 * } catch (MissingResourceException e) {
	 * return '!' + key + '!';
	 * }
	 * }
	 */
	public static String MpiCASTVisitor_mpiCall;
	public static String MpiCASTVisitor_mpiConstant;
	public static String MpiCHelpBook_MPI_C_HELP_BOOK_TITLE;
	public static String MpiCPPASTVisitor_mpiCall;
	public static String MpiCPPASTVisitor_mpiConstant;
	public static String MpiCPPHelpBook_MPI_CPP_HELP_BOOK_TITLE;
	public static String MpiIDs_MPI_INCLUDES;
	public static String MPIPreferencePage_mpiIncludePaths;
	public static String MPIPreferencePage_pleaseChooseAdirectory;
	public static String MPIPreferencePage_mpiBuildCommand;
	public static String MPIPreferencePage_mpiCppBuildCommand;
	public static String MPIPreferencePage_promptToIncludeOtherLocations;
	public static String MPIPreferencePage_recognizeAPISByPrefixAlone;
	public static String MPITableView_ARTIFACT;
	public static String MPITableView_ARTIFACTS;
	public static String MPITableView_CONSTRUCT;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
}
