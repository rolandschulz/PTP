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
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching.ErrorMessage;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.MPIBarrierMatching.PathNode;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;
import org.eclipse.ptp.pldt.mpi.analysis.messages.Messages;

/**
 * For the list of ErrorMessage objects given to this class,
 * iterate through them and create parent and child node groups of
 * markers for each error.
 * 
 */
public class ShowErrors {
	protected List<ErrorMessage> errors_;
	protected int counter = 0;

	public ShowErrors(List<ErrorMessage> errors) {
		this.errors_ = errors;
	}

	/**
	 * 
	 * @return true if errors found
	 */

	public boolean run() {
		boolean foundErrors = false;
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			int depth = IResource.DEPTH_INFINITE;
			wsResource.deleteMarkers(IDs.errorMarkerID, false, depth);

		} catch (CoreException e) {
			System.out.println("RM: exception deleting markers."); //$NON-NLS-1$
			e.printStackTrace();
		}

		// TODO: change this visitor
		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(IDs.errorMarkerID);

		/*
		 * Two kinds of Artifacts:
		 * (1) errorous conditions
		 * (2) barriers in counter examples
		 */

		for (Iterator<ErrorMessage> i = errors_.iterator(); i.hasNext();) {
			ErrorMessage err = i.next();

			/* Error condition */
			counter++;
			int condID = counter;
			ScanReturn sr = new ScanReturn();
			String funcName = err.getFuncName();
			String fileName = err.getFileName();
			SourceInfo sourceInfo = err.getSourceInfo();
			ArtifactWithParent ea = new ArtifactWithParent(fileName,
					sourceInfo.getStartingLine(), 1,
					funcName, "Erroneous Condition", sourceInfo, 0, condID, "Error", 0); //$NON-NLS-1$ //$NON-NLS-2$
			sr.addArtifact(ea);
			visitor.visitFile(err.getResource(), sr.getArtifactList());

			/* Path 1 (and 2) */
			counter++;
			int path1ID = counter;
			String path1name = (String) null;
			if (err.getLength1() == -1) {
				if (err.getPath2() != null)
					path1name = Messages.ShowErrors_path1DynamicNumberOfBarriers;
				else
					path1name = Messages.ShowErrors_loopDynamicNumberOfBarriers;
			} else {
				path1name = Messages.ShowErrors_path1 + err.getLength1() + Messages.ShowErrors_barriers;
			}
			sr = new ScanReturn();
			ea = new ArtifactWithParent("", 0, 0, "", Messages.ShowErrors_counterExample, //$NON-NLS-1$ //$NON-NLS-2$
					err.getPath1SourceInfo(), condID, path1ID, path1name, 0);
			sr.addArtifact(ea);

			int path2ID = 0;
			if (err.getPath2() != null) {
				counter++;
				path2ID = counter;
				String path2name = (String) null;
				if (err.getLength2() == -1) {
					path2name = Messages.ShowErrors_path2dynamicNumberOfBarriers;
				} else {
					path2name = Messages.ShowErrors_path2 + err.getLength2() + Messages.ShowErrors_barriers;
				}
				ea = new ArtifactWithParent("", 0, 0, "", "Counter Example", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						err.getPath2SourceInfo(), condID, path2ID, path2name, 0);
				sr.addArtifact(ea);
			}
			visitor.visitFile(err.getResource(), sr.getArtifactList());

			/* Counter example content */
			for (Iterator<PathNode> ii = err.getPath1().iterator(); ii.hasNext();) {
				PathNode pn = ii.next();
				BarrierInfo barrier = pn.getBarrier();
				counter++;
				sr = new ScanReturn();
				sourceInfo = barrier.getSourceInfo();
				fileName = barrier.getFileName();
				funcName = barrier.getEnclosingFunc();
				String barrierName = (String) null;
				// BRT Note! This is where the barrier matching set labels are.
				// Consider changing the parent node to "Barrier Set"
				if (pn.isRepeat())
					barrierName = Messages.ShowErrors_barrier_ + (barrier.getID() - 4) + "(*)"; //$NON-NLS-2$ //$NON-NLS-1$
				else
					barrierName = Messages.ShowErrors_barrier_ + (barrier.getID() - 4);
				ArtifactWithParent a = new ArtifactWithParent(fileName,
						sourceInfo.getStartingLine(), 1, funcName,
						Messages.ShowErrors_barrier, sourceInfo, path1ID, counter, barrierName,
						barrier.getID() - 4);
				sr.addArtifact(a);
				visitor.visitFile(barrier.getResource(), sr.getArtifactList());
			}

			if (err.getPath2() == null)
				continue;
			for (Iterator<PathNode> ii = err.getPath2().iterator(); ii.hasNext();) {
				PathNode pn = ii.next();
				BarrierInfo barrier = pn.getBarrier();
				counter++;
				sr = new ScanReturn();
				sourceInfo = barrier.getSourceInfo();
				fileName = barrier.getFileName();
				funcName = barrier.getEnclosingFunc();
				String barrierName = (String) null;
				if (pn.isRepeat())
					barrierName = Messages.ShowErrors_barrier_ + (barrier.getID() - 4) + "(*)"; //$NON-NLS-2$ //$NON-NLS-1$
				else
					barrierName = Messages.ShowErrors_barrier_ + (barrier.getID() - 4);
				ArtifactWithParent a = new ArtifactWithParent(fileName,
						sourceInfo.getStartingLine(), 1, funcName,
						Messages.ShowErrors_barrier, sourceInfo, path2ID, counter, barrierName,
						barrier.getID() - 4);
				sr.addArtifact(a);
				visitor.visitFile(barrier.getResource(), sr.getArtifactList());
			}
		}

		// Done creating markers, now show the views
		ViewActivator.activateView(IDs.matchingSetViewID);
		if (errors_.size() > 0) {
			ViewActivator.activateView(IDs.errorViewID);
			foundErrors = true;
		}
		return foundErrors;

	}

}
