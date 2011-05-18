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

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable;
import org.eclipse.ptp.pldt.mpi.analysis.analysis.BarrierTable.BarrierInfo;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;

public class MatchingSet {
	protected BarrierTable btable_ = null;

	public MatchingSet(BarrierTable table) {
		btable_ = table;
	}

	public void run() {
		IWorkspaceRoot wsResource = ResourcesPlugin.getWorkspace().getRoot();
		try {
			int depth = IResource.DEPTH_INFINITE;
			wsResource.deleteMarkers(IDs.matchingSetMarkerID, false, depth);

		} catch (CoreException e) {
			//System.out.println("MatchingSet: exception deleting markers."); //$NON-NLS-1$
			// e.printStackTrace();
		}

		MPIArtifactMarkingVisitor visitor = new MPIArtifactMarkingVisitor(IDs.matchingSetMarkerID);
		int count = 0;

		// create the markers for all the matched barriers
		for (Enumeration e = btable_.getTable().elements(); e.hasMoreElements();) {
			ArrayList barrierList = (ArrayList) e.nextElement();
			for (Iterator ii = barrierList.iterator(); ii.hasNext();) {
				BarrierInfo barrier = (BarrierInfo) ii.next();
				count++;
				int parentID = count;
				String name = "Barrier " + (barrier.getID() - 4) + " (" + barrier.getMatchingSet().size() + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				// create a marker for each parent(barrier)
				ScanReturn sr = new ScanReturn();
				SourceInfo sourceInfo = barrier.getSourceInfo();
				String filename = barrier.getFileName();
				int line = sourceInfo.getStartingLine();
				String fn = barrier.getEnclosingFunc();
				ArtifactWithParent a = new ArtifactWithParent(filename, line, 1, fn,
						"Artifact Call", sourceInfo, 0, parentID, name, barrier.getID() - 4); //$NON-NLS-1$
				sr.addArtifact(a);
				visitor.visitFile(barrier.getResource(), sr.getArtifactList());

				for (Iterator i = barrier.getMatchingSet().iterator(); i.hasNext();) {
					BarrierInfo matchedBar = (BarrierInfo) i.next();
					count++;
					int myID = count;
					name = "Barrier " + (matchedBar.getID() - 4); //$NON-NLS-1$
					sr = new ScanReturn();
					sourceInfo = matchedBar.getSourceInfo();
					filename = matchedBar.getFileName();
					line = sourceInfo.getStartingLine();
					fn = matchedBar.getEnclosingFunc();
					a = new ArtifactWithParent(filename, line, 1, fn, "Artifact Call", //$NON-NLS-1$
							sourceInfo, parentID, myID, name, matchedBar.getID() - 4);
					sr.addArtifact(a);
					visitor.visitFile(matchedBar.getResource(), sr.getArtifactList());
				}

			}
		}
		// Done creating markers, now show the view
		ViewActivater.activateView(IDs.matchingSetViewID);
	}

	/*
	 * protected void showNode(IASTNode node, String markerType)
	 * {
	 * Utility.Location l = Utility.getLocation(node);
	 * 
	 * IAnnotationModel am = editor_.getDocumentProvider().getAnnotationModel(editor_.getEditorInput());
	 * 
	 * // We need to add an annotation type to the annotation painter (see SourceViewerDecorationSupport)
	 * Annotation a = new Annotation(markerType, true, "Hi");
	 * int end = l.high_-l.low_+1;
	 * Position p = new Position(l.low_, end);
	 * am.addAnnotation(a, p);
	 * }
	 */

}
