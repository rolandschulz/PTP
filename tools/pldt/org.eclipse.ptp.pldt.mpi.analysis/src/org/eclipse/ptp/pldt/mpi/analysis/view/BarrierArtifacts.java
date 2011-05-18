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

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraph;
import org.eclipse.ptp.pldt.mpi.analysis.cdt.graphs.ICallGraphNode;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;

public class BarrierArtifacts {
	private ICallGraph CG_;
	private ScanReturn scanReturn_;
	private MPIArtifactMarkingVisitor visitor_;
	private String markerID = IDs.barrierMarkerID;

	public BarrierArtifacts(ICallGraph cg, MPIArtifactMarkingVisitor visitor) {
		CG_ = cg;
		visitor_ = visitor;
	}

	public void run() {
		/* first clear all existing markers */
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			int depth = IResource.DEPTH_INFINITE;
			wsResource.deleteMarkers(markerID, false, depth);

		} catch (CoreException e) {
			System.out.println("RM: exception deleting markers."); //$NON-NLS-1$
			e.printStackTrace();
		}

		for (ICallGraphNode n = CG_.topEntry(); n != null; n = n.topNext()) {
			MPICallGraphNode node = (MPICallGraphNode) n;
			List<BarrierInfo> barriers = node.getAllBarriers();
			scanReturn_ = new ScanReturn();
			for (Iterator<BarrierInfo> i = barriers.iterator(); i.hasNext();) {
				BarrierInfo bar = i.next();
				SourceInfo sourceInfo = bar.getSourceInfo();
				ArtifactWithParent awp = new ArtifactWithParent(node.getFileName(),
						sourceInfo.getStartingLine(), 1, node.getFuncName(), "Artifact Call", sourceInfo, 0, 0, "", //$NON-NLS-1$ //$NON-NLS-2$
						bar.getID() - 4);
				scanReturn_.addArtifact(awp);
			}
			visitor_.visitFile(node.getResource(), scanReturn_.getArtifactList());
		}
	}
}
