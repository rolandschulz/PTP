/**********************************************************************
 * Copyright (c) 2007,2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.internal;

/**
 * ID values shared by several classes in this plug-in
 * 
 * @author Beth Tibbitts
 * 
 */
public class IDs {
	public static final String barrierMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.mpiBarrierMarker"; //$NON-NLS-1$

	/**
	 * The marker id for barrier matching sets
	 */
	public static final String matchingSetMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.mpiBarrierMatchingSetMarker"; //$NON-NLS-1$

	public static final String matchingSetViewID = "org.eclipse.ptp.pldt.mpi.analysis.view.MPIBarrierMatchingSetTableView"; //$NON-NLS-1$
	public static final String errorViewID = "org.eclipse.ptp.pldt.mpi.analysis.view.MPIErrorView"; //$NON-NLS-1$
	public static final String barrierViewID = "org.eclipse.ptp.pldt.mpi.analysis.view.MPIBarrierTableView"; //$NON-NLS-1$

	/* For error counter examples */
	public static final String errorMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.errorMarker"; //$NON-NLS-1$
	/**
	 * The marker attribute name for a parent node. All markers with the same
	 * parent value may be considered children of the same parent.
	 * 
	 */
	public static final String parentIDAttr = "parentID"; //$NON-NLS-1$
	public static final String myIDAttr = "myID"; //$NON-NLS-1$
	public static final String myNameAttr = "myName"; //$NON-NLS-1$
	public static final String myIndexAttr = "myIndex"; //$NON-NLS-1$
}
