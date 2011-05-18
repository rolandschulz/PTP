/**********************************************************************
 * Copyright (c) 2007,2009 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.analysis.view;

import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.analysis.internal.IDs;
import org.eclipse.ui.texteditor.MarkerUtilities;

public class MPIArtifactMarkingVisitor extends ArtifactMarkingVisitor {

	protected static final boolean traceOn = false;

	public MPIArtifactMarkingVisitor(String markerID) {
		super(markerID);
	}

	public String getMarkerID() {
		return markerID_;
	}

	/**
	 * Visit file, but don't remove existing markers
	 * */
	public boolean visitFile(IResource resource, Artifact[] artifacts)
	{
		try {
			if (traceOn)
				System.out.println("ArtifactMarkingVisitor.visitFile: " + resource.getName()); //$NON-NLS-1$
			// removeMarkers(resource, this.markerID_);
			int numArtifacts = artifactManager_.getArtifacts().length;
			if (traceOn)
				System.out.println("numArtifacts: " + numArtifacts); //$NON-NLS-1$

			String fn = resource.getProjectRelativePath().toString();

			if (artifacts != null) {
				createMarkers(resource, fn, artifacts);
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return true;

	}

	/**
	 * Create the marker for the artifact; like the super class
	 * version of this, but also adds the parent name from ArtifactWithParent - for use in the tree view
	 * 
	 * @param resource
	 * @param artifact
	 * @param fn
	 * @throws CoreException
	 */
	protected void createArtifactMarker(IResource resource, Artifact artifact, String fn) throws CoreException
	{
		if (traceOn)
			System.out.println("Artifact desc=" + artifact.getShortName() + "  fn=" + fn); //$NON-NLS-1$ //$NON-NLS-2$
		Map attrs = createCommonMarkerAttrs(resource, artifact, fn);
		// message attribute will be used for hover over editor location
		attrs.put(IMarker.MESSAGE, artifact.getShortName());

		// Get the parent name out of the artifact, to place in the marker
		ArtifactWithParent awp = (ArtifactWithParent) artifact;

		attrs.put(IDs.parentIDAttr, new Integer(awp.getParentID()));
		attrs.put(IDs.myIDAttr, new Integer(awp.getMyID()));
		attrs.put(IDs.myNameAttr, awp.getMyName());
		attrs.put(IDs.myIndexAttr, new Integer(awp.getIndex()));

		// create the marker all at once, so get ONLY a single resourceChange event.
		MarkerUtilities.createMarker(resource, attrs, this.markerID_); // 154
		if (traceOn)
			System.out.println("marker created: for " + fn + " - " + artifact.getShortName() + " line " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ artifact.getLine() + " start " + artifact.getSourceInfo().getStart() + " end " //$NON-NLS-1$ //$NON-NLS-2$
					+ artifact.getSourceInfo().getEnd());

		artifactManager_.addArtifactToHash(artifact);
	}
}
