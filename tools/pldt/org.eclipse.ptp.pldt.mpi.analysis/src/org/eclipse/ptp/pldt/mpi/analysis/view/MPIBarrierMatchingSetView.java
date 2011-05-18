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

package org.eclipse.ptp.pldt.mpi.analysis.view;

//import org.eclipse.ptp.pldt.common.views.SimpleTreeTableMarkerView;
import org.eclipse.ptp.pldt.mpi.analysis.Activator;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.messages.Messages;

/**
 * Show the members of a barrier matching set, in a tree table view.
 * 
 * TODO: try to use class from 'common' package instead
 */
public class MPIBarrierMatchingSetView extends SimpleTreeTableMarkerView {
	public MPIBarrierMatchingSetView() {

		super(Activator.getDefault(), "FunctionName", Messages.MPIBarrierMatchingSetView_matchingSetArtifacts, //$NON-NLS-1$
				Messages.MPIBarrierMatchingSetView_indexNum, IDs.matchingSetMarkerID /* , common ver needs:"parent" */);
	}
}
