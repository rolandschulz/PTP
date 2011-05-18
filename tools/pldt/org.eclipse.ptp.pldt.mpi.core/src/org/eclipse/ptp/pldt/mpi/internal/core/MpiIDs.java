/**********************************************************************
 * Copyright (c) 2005,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.internal.core;

import org.eclipse.ptp.pldt.mpi.core.messages.Messages;

/**
 * Misc. IDs for preferences, markers, etc.
 * 
 * @author tibbitts
 * 
 */
public interface MpiIDs
{
	public static final String P_RUN_ANALYSIS = "runAnalysis"; //$NON-NLS-1$
	public static final String P_ECHO_FORCE = "forceEcho"; //$NON-NLS-1$
	public static final String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.mpi.core.mpiErrorMarker"; //$NON-NLS-1$
	public static final String MARKER_ID = "org.eclipse.ptp.pldt.mpi.core.mpiMarker"; //$NON-NLS-1$
	public static final String FILENAME = "filename"; //$NON-NLS-1$
	public static final String NAME = "name"; //$NON-NLS-1$
	public static final String DESCRIPTION = "description"; //$NON-NLS-1$
	public static final String ID = "uniqueID"; //$NON-NLS-1$

	// note built-in marker id too... not sure this is used,
	// compare with IMarker.LINE_NUMBER which is probably used instead.
	public static final String LINE = "theLineNo"; //$NON-NLS-1$
	public static final String NEWLINE = "theNewLineNo"; //$NON-NLS-1$

	/** Preference page name for MPI Includes field label */
	static public final String MPI_INCLUDES = Messages.MpiIDs_MPI_INCLUDES;
	/** Preference setting for default mpi build command */
	public static final String MPI_BUILD_CMD = "mpiBuildCommand"; //$NON-NLS-1$
	/** Preference setting for default c++ mpi build command */
	public static final String MPI_CPP_BUILD_CMD = "mpiCppBuildCommand"; //$NON-NLS-1$
	/** Preference setting for whether or not to prompt the user when MPI apis are found in other locations */
	public static final String MPI_PROMPT_FOR_OTHER_INCLUDES = "mpiPromptForOtherIncludes"; //$NON-NLS-1$

	/** Marker attribute for the extra info saved for each marker/artifact */
	public static final String CONSTRUCT_TYPE = "constructType"; //$NON-NLS-1$

	/** id for MPI Artifact view */
	public static final String MPI_VIEW_ID = "org.eclipse.ptp.pldt.mpi.core.views.MPITableView"; //$NON-NLS-1$
	/**
	 * If we recognize MPI APIs by prefix (MPI_) alone, we don't need the include file location and the hassle
	 * that it causes for users to get it right.
	 */
	public static final String MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE = "mpiRecognizeAPIsByPrefixAlone"; //$NON-NLS-1$

}
