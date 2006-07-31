/**********************************************************************
 * Copyright (c) 2005 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.fortran.mpi;

/**
 * Misc. IDs for preferences, markers, etc.
 * Copied here from ptp.pldt.mpi.core, to avoid cyclic dependency
 * 
 * @author tibbitts
 * 
 */
public interface MpiIDs
{
    String MARKER_ID       = "org.eclipse.ptp.pldt.mpi.core.mpiMarker";
    String MARKER_ERROR_ID = "org.eclipse.ptp.pldt.mpi.core.mpiErrorMarker";
    // id for MPI Artifact view
    String MPI_VIEW_ID = "org.eclipse.ptp.pldt.mpi.core.views.MPITableView";
}
