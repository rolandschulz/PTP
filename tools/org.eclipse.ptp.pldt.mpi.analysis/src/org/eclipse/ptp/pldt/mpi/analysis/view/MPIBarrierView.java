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

//import org.eclipse.ptp.pldt.common.views.SimpleTableView;
import org.eclipse.ptp.pldt.mpi.analysis.Activator;
import org.eclipse.ptp.pldt.mpi.analysis.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.messages.Messages;

/**
 * View to show list of barriers
 * 
 * TODO: this should use the SimpleTableView from the 'common' package,
 * which I'm hoping the other view can eventually do as well (reuse view
 * classes from that package)
 * TODO: replace column header from 'common' package view class
 */
// public class MPIBarrierView extends SimpleTableView {// use common class
public class MPIBarrierView extends SimpleTableBarrierView {// original
	public MPIBarrierView() {

		super(Activator.getDefault(), Messages.MPIBarrierView_function, Messages.MPIBarrierView_barrierArtifacts,
				Messages.MPIBarrierView_indexNo, IDs.barrierMarkerID);
	}

}
