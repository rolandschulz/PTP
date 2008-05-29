/**********************************************************************
 * Copyright (c) 2005,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core;

/**
 * Misc. IDs for preferences, markers, etc.
 * 
 * @author tibbitts
 * 
 */
public interface MpiIDs
{
    public static final String P_RUN_ANALYSIS  = "runAnalysis";
    public static final String P_ECHO_FORCE    = "forceEcho";
    public static final String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.mpi.core.mpiErrorMarker";
    public static final String MARKER_ID       = "org.eclipse.ptp.pldt.mpi.core.mpiMarker";
    public static final String FILENAME        = "filename";
    public static final String NAME            = "name";
    public static final String DESCRIPTION     = "description";
    public static final String ID              = "uniqueID";

    // note built-in marker id too... not sure this is used,
    // compare with IMarker.LINE_NUMBER which is probably used instead.
    public static final String LINE            = "theLineNo";
    public static final String NEWLINE         = "theNewLineNo";

    /** Preference page name for MPI Includes field label */
    static public final String MPI_INCLUDES    = "MPI Includes";
    /** Preference setting for default mpi build command */
	public static final String MPI_BUILD_CMD = "mpiBuildCommand";
	/** Preference setting for default c++ mpi build command */
	public static final String MPI_CPP_BUILD_CMD = "mpiCppBuildCommand";
	/** Preference setting for whether or not to prompt the user when MPI apis are found in other locations */
	public static final String MPI_PROMPT_FOR_OTHER_INCLUDES="mpiPromptForOtherIncludes";

    /** Marker attribute for the extra info saved for each marker/artifact */
    public static final String CONSTRUCT_TYPE  = "constructType";
    
    /** id for MPI Artifact view */
    public static final String MPI_VIEW_ID = "org.eclipse.ptp.pldt.mpi.core.views.MPITableView";

}
