/**********************************************************************
 * Copyright (c) 2005, 2011 IBM Corporation.
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
 * Artifact Visitor - Generic - for delta or full build
 * @author Beth Tibbitts
 * 
 */
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.internal.common.IDs;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * Processes an <code>IResource</code> or an <code>IResourceDelta</code> to set Artifact Markers on the files
 * and populate the data model used for the Artifact view(s).
 */
public class ArtifactMarkingVisitor implements IResourceDeltaVisitor, IResourceVisitor
{

	private static final boolean traceOn = false;
	protected String markerID_;
	protected ArtifactManager artifactManager_;
	private boolean removeMarkers;

	/**
	 * Construction that allows specification of whether or not to removed old
	 * markers on this resource before creating new ones
	 * 
	 * @param markerId
	 * @param removeMarkers
	 */
	public ArtifactMarkingVisitor(String markerId, boolean removeMarkers)
	{
		this.markerID_ = markerId;
		this.removeMarkers = removeMarkers;
		this.artifactManager_ = ArtifactManager.getManager(markerID_);
		if (artifactManager_ == null) {
			System.out.println("no manager yet!"); //$NON-NLS-1$
		}
	}

	/**
	 * Constructor that uses the default behavior that WILL remove old markers
	 * on the resource before creating new ones. (This is the former behavior,
	 * now the default behavior)
	 * 
	 * @param markerID
	 */
	public ArtifactMarkingVisitor(String markerID)
	{
		this(markerID, true);
	}

	/**
	 * Generic visiting of a file, to presumably add markers. This version is called from a build.
	 * 
	 * @param resource
	 * @return
	 * @author Beth Tibbitts
	 */
	public boolean visitFile(IResource resource)
	{
		// System.out.println("filename="+resource.getLocation().toString());
		Artifact[] artifacts = getPIs(resource);
		return visitFile(resource, artifacts);

	}

	/**
	 * Generic version to put markers on a file, given the file(resource) and the list of artifacts
	 * 
	 * @param resource
	 * @param artifacts
	 *            list of artifact objects
	 * @return
	 */
	public boolean visitFile(IResource resource, Artifact[] artifacts)
	{
		try {
			if (traceOn)
				System.out.println("ArtifactMarkingVisitor.visitFile: " + resource.getName()); //$NON-NLS-1$
			// first clear existing markers (not: not doing anything ArtifactManager now.)
			if (removeMarkers) {
				removeMarkers(resource, this.markerID_);
			}
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
	 * Put markers on a file where list of artifacts is a List<Artifact> instead of an array
	 *
	 * @param resource the resource, presumably a file, in which the artifacts (e.g. MPI calls) were found
	 * @param artifacts found in the file, with type and location (line number etc.)
	 * @return
	 */
	public boolean visitFile(IResource resource, List<Artifact> artifacts)
	{
		Artifact[] artifactArray = new Artifact[artifacts.size()];
		int i = 0;
		for (Iterator<Artifact> iter = artifacts.iterator(); iter.hasNext();) {
			Artifact element = (Artifact) iter.next();
			artifactArray[i] = element;
			i++;
		}
		return visitFile(resource, artifactArray);
	}

	/**
	 * Create markers for a file. This method creates a marker for each artifact. It also populates/refreshes the
	 * views.
	 * 
	 * @param resource
	 *            assumed to be a file, resource upon which to place the marker(s)
	 * @param fn
	 *            - filename
	 * @param artifacts
	 *            - array of Artifact objects for which to create markers.
	 * @throws CoreException
	 */
	protected void createMarkers(IResource resource, String fn, Artifact[] artifacts) throws CoreException
	{
		if (traceOn)
			System.out.println("ArtifactMarkingVisitor.createMarkers: " + resource.getName() + " #artifacts=" + artifacts.length); //$NON-NLS-1$ //$NON-NLS-2$

		for (int i = 0; i < artifacts.length; i++) {
			Artifact artifact = artifacts[i];
			// artifactMarker 'inherits' from textMarker(see plugin.xml)
			// If it also inherited from ProblemMarker, it would automagically show in more views (e.g. Problems view)
			if (artifact != null) {
				// 1. make marker for main Artifact

				// Get correct filename for include files
				// Actual file containing artifact may be an include file, so use it
				// instead of the original file being analyzed
				String filename = artifact.getFileName();
				IResource f = resource;
				// If artifact is not on same file being analyzed, correct it
				// and get the actual file. E.G. an include file
				// (don't create another resource object if we don't need to)
				// System.out.println("resLocn="+resource.getRawLocation().toString());
				// if (!resLocn.equals(filename))
				// f = MpiPlugin.getWorkspace().getRoot().getFileForLocation(
				// new Path(pifn));
				//
				int index = filename.lastIndexOf('/');
				if (index == -1) {
					index = filename.lastIndexOf('\\');
				}
				if (index != -1) {
					// BRT note: if contains both / and \ this won't work
					// trucate to last portion of path
					// this shd probably be a user preference, if this is truncated or not
					String tmp2 = filename.substring(index + 1);
					filename = tmp2;

				}
				createArtifactMarker(f, artifact, filename);

				// If making other views, e.g. tree view, add to model tree for view here..
			} else {
				if (traceOn)
					System.out.println("*** artifact for " + fn + "is null! ********"); //$NON-NLS-1$ //$NON-NLS-2$
				else {
				}
			}
			// now refresh the tree view!!
			// MPITree.getTree().refresh();
		}
	}

	/**
	 * create the marker for the artifact, and add it to
	 * the repository (ArtifactManager)
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
		Map<String, Object> attrs = createCommonMarkerAttrs(resource, artifact, fn);
		// message attribute will be used for hover over editor location
		attrs.put(IMarker.MESSAGE, artifact.getShortName());
		// create the marker all at once, so get ONLY a single resourceChange event.
		MarkerUtilities.createMarker(resource, attrs, this.markerID_); // 154
		if (traceOn)
			System.out.println("marker created: for " + fn + " - " + artifact.getShortName() + " line " //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
					+ artifact.getLine() + " start " + artifact.getSourceInfo().getStart() + " end " //$NON-NLS-1$ //$NON-NLS-2$
					+ artifact.getSourceInfo().getEnd());

		artifactManager_.addArtifactToHash(artifact);
	}

	/**
	 * Create marker attributes with common information shared by everyone
	 * 
	 * @param resource
	 *            File on which analysis was run
	 * @param artifact
	 *            the artifact object
	 * @param fn
	 *            project relative path of the resource
	 * @return
	 * 
	 *         Note we are not creating the marker here; we do it all at once after attributes are calculated, so that a single
	 *         resourceChange event gets triggered
	 */
	protected Map<String, Object> createCommonMarkerAttrs(IResource resource, Artifact artifact, String fn)
	{
		Map<String, Object> attrs = new HashMap<String, Object>();
		attrs.put(IMarker.PRIORITY, new Integer(IMarker.PRIORITY_NORMAL));
		attrs.put(IMarker.LINE_NUMBER, new Integer(artifact.getLine()));
		// use filename from the artifact, not the base file being analysed,
		// because the artifact may be a result of reporting on e.g. an include file
		attrs.put(IMarker.LOCATION, artifact.getFileName());
		attrs.put(IDs.FILENAME, fn); // fn being processed
		attrs.put(IDs.NAME, artifact.getShortName());
		attrs.put(IDs.DESCRIPTION, artifact.getDescription());

		// java5
		// // trying setting the marker to more precise location
		// attrs.put(IMarker.CHAR_START, artifact.getSourceInfo_().getStart());
		// attrs.put(IMarker.CHAR_END, artifact.getSourceInfo_().getEnd());
		//
		// // adding construct type, i.e. func call, constants, ect.
		// attrs.put(IDs.CONSTRUCT_TYPE, artifact.getSourceInfo_().getConstructType());

		// trying setting the marker to more precise location
		attrs.put(IMarker.CHAR_START, new Integer(artifact.getSourceInfo().getStart()));
		attrs.put(IMarker.CHAR_END, new Integer(artifact.getSourceInfo().getEnd()));

		// adding construct type, i.e. func call, constants, ect.
		attrs.put(IDs.CONSTRUCT_TYPE, new Integer(artifact.getSourceInfo().getConstructType()));

		String id = ((Artifact) artifact).getId();
		attrs.put(IDs.ID, id); // used to look up to get artifact from marker later
		attrs.put(IDs.LINE, Integer.toString(artifact.getLine()));
		if (traceOn)
			System.out.println("marker created: for " + fn + " - " + artifact.getDescription() + " line " + artifact.getLine()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		return attrs;
	}

	/**
	 * Get Artifacts for a given resource. e.g. could reads XML file that is presumed to be in same folder as
	 * resource file.
	 * 
	 * @param r
	 *            The resource (file assumed) for which to look for artifacts
	 * @return a list of artifacts
	 */
	public static Artifact[] getPIs(IResource r)
	{
		System.out.println("*UNIMPLEMENTED** ArtifactMarkingVisitor.getPIs: " + r.getName()); //$NON-NLS-1$
		return null;
	}

	/**
	 * Remove the markers currently set on a resource.
	 * 
	 * @param resource
	 */
	private void removeMarkers(IResource resource, String markerID)
	{
		if (traceOn)
			System.out.println("ArtifactMarkingVisitor.removeMarkers: " + resource.getName()); //$NON-NLS-1$

		try {
			resource.deleteMarkers(markerID, false, IResource.DEPTH_INFINITE);
			// also sync up tree view
			// MPITree.getTree().removeIssues(resource);
		} catch (CoreException e) {
			System.out.println(e);
			System.out.println(e.toString());
			System.out.println("Problem deleting markers on " + resource.getProjectRelativePath()); //$NON-NLS-1$
		}
	}

	/**
	 * Implemented for IResourceDeltaVisitor Required implementation of the visit method. The <code>IResourceDelta</code> is
	 * processed by providing it a visitor using the <code>accept()</code> method and
	 * using this method in the visitor to process any events of interest.
	 * 
	 * Processing continues as long as this method returns true or when the end of the <code>IResourceDelta</code> has
	 * been reached.
	 */
	public boolean visit(IResourceDelta delta) throws CoreException
	{
		// if (traceOn)System.out.println("ArtifactMarkingVisitor.visit(resourceDelta)...");
		IResource resource = delta.getResource();
		int type = resource.getType();
		if ((delta.getKind() != IResourceDelta.REMOVED) && (type == IResource.FILE)) {
			// System.out.println("delta visitor visits FILE: " +
			// resource.getProjectRelativePath());
			visitFile(resource);
		}
		return true; // carry on
	}


	/* (non-Javadoc)
	 * @see org.eclipse.core.resources.IResourceVisitor#visit(org.eclipse.core.resources.IResource)
	 */
	public boolean visit(IResource resource)
	{
		if (resource.getType() == IResource.FILE) {
			// if (traceOn)System.out.println("FULL visitor visits FILE: " +
			// resource.getProjectRelativePath());
			visitFile(resource);
		}
		return true; // carry on
	}

	/**
	 * Show an artifact object - print for debugging/tracing purposes
	 * 
	 * @param artifact
	 *            the Artifact Object
	 */
	public static void showArtifact(Artifact artifact)
	{
		System.out.println("Artifact name: " + artifact.getShortName()); //$NON-NLS-1$
		System.out.println("   Description: " + artifact.getDescription()); //$NON-NLS-1$
		System.out.println("   Filename:    " + artifact.getFileName()); //$NON-NLS-1$

	}

}
