/**********************************************************************
 * Copyright (c) 2005,2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.common;

/**
 * Misc. IDs for preferences, markers, etc.
 * 
 * @author tibbitts
 * 
 */
public interface IDs
{
    String P_RUN_ANALYSIS  = "runAnalysis"; //$NON-NLS-1$
    String P_ECHO_FORCE    = "forceEcho"; //$NON-NLS-1$
//    String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.mpi.core.mpiErrorMarker";
//    String MARKER_ID       = "org.eclipse.ptp.pldt.mpi.core.mpiMarker";
    String FILENAME        = "filename"; //$NON-NLS-1$
    String NAME            = "name"; //$NON-NLS-1$
    String DESCRIPTION     = "description"; //$NON-NLS-1$
    String ID              = "uniqueID"; //$NON-NLS-1$

    // note built-in marker id too... not sure this is used,
    // compare with IMarker.LINE_NUMBER which is probably used instead.
    String LINE            = "theLineNo"; //$NON-NLS-1$
    String NEWLINE         = "theNewLineNo"; //$NON-NLS-1$

    // preference page name for MPI Includes field label
//    static public final String MPI_INCLUDES    = "MPI Includes";

    // marker attribute for the extra info saved for each marker/artifact
    String CONSTRUCT_TYPE  = "constructType"; //$NON-NLS-1$
    /**
	 * Key for storing preference of whether or not to show popup confirmation
	 * dialog when analysis is complete.
	 */
	String SHOW_ANALYSIS_CONFIRMATION = "showAnalysisConfirmation"; //$NON-NLS-1$
	String UNIQUE_ID = "uniqueID"; //$NON-NLS-1$
    
    // id for MPI Artifact view
//    String MPI_VIEW_ID = "org.eclipse.ptp.pldt.mpi.core.views.MPITableView";
}
