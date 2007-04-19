/**********************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis;

/**
 * ID values shared by several classes in this plug-in
 * 
 * @author Beth Tibbitts
 *
 */
public class IDs {
	public static final String barrierMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.mpiBarrierMarker";

	/**
	 * The marker id for barrier matching sets
	 */
	public static final String matchingSetMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.mpiBarrierMatchingSetMarker";
	/**
	 * the marker attribute name for a parent node.
	 * All markers with the same parent value are considered part of
	 * the same Barrier matching set.
	 */
	public static final String parentAttribName="parent";
	public static final String parentName = "parentName";
	
    public static final String matchingSetViewID="org.eclipse.ptp.pldt.mpi.analysis.view.MPIBarrierMatchingSetTableView";

    /* For error counter examples */
    public static final String errorMarkerID = "org.eclipse.ptp.pldt.mpi.analysis.errorMarker";
    public static final String parentIDAttr = "parentID";
    public static final String myIDAttr = "myID";
    public static final String myNameAttr = "myName";
    public static final String myIndexAttr = "myIndex";
// test    
}
