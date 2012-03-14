/*******************************************************************************
 * Copyright (c) 2007, 2011, 2012 IBM Corporation, University of Illinois, and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey (UIUC) - modified to use extension point
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.core.actions;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPError;
import org.eclipse.ptp.pldt.openmp.core.OpenMPArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.openmp.core.OpenMPPlugin;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.core.internal.OpenMPIDs;
import org.eclipse.ptp.pldt.openmp.ui.pv.internal.IDs;
import org.eclipse.ptp.pldt.openmp.ui.pv.views.ProblemMarkerAttrIds;
import org.eclipse.ui.texteditor.MarkerUtilities;

/**
 * @author tibbitts
 * @since 4.0
 * 
 */
public class RunAnalyseOpenMPcommandHandler extends RunAnalyseHandlerBase {
	/**
	 * ID for the extension point which allows plug-ins to contribute OpenMP artifact analyses based on language IDs.
	 */
	private static final String EXTENSION_POINT_ID = "org.eclipse.ptp.pldt.openmp.core.artifactAnalysis";

	private static final boolean traceOn = false;

	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseOpenMPcommandHandler() {
		super("OpenMP", new OpenMPArtifactMarkingVisitor(OpenMPIDs.MARKER_ID), OpenMPIDs.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns OpenMP analysis artifacts for file
	 * 
	 * @param file
	 * @param includes
	 *            OpenMP include paths
	 * @return
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		boolean allowPrefixOnlyMatch = OpenMPPlugin.getDefault().getPreferenceStore().getBoolean(OpenMPIDs.OPENMP_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		return runArtifactAnalysisFromExtensionPoint(EXTENSION_POINT_ID, tu, includes, allowPrefixOnlyMatch);
	}

	/**
	 * processResults - override from RunAnalyse base, to process both pragma artifacts and problems
	 */
	@Override
	protected void processResults(ScanReturn results, IResource resource) {
		assert (results instanceof OpenMPScanReturn);

		OpenMPScanReturn osr = (OpenMPScanReturn) results;

		// This is for the openmp pragma view
		List<Artifact> artifacts = osr.getOpenMPList();
		visitor.visitFile(resource, artifacts);

		// remove problems
		removeProblemMarkers(resource);

		// DPP - put in stuff for problems view
		// Just subclass scanreturn and create markers for problems view here
		List<OpenMPError> problems = osr.getProblems();
		if (traceOn) {
			System.out.println("RunAnalyseOpenMP.processResults, have " + problems.size() + " problems."); //$NON-NLS-1$ //$NON-NLS-2$
		}
		try {
			for (Iterator<OpenMPError> i = problems.iterator(); i.hasNext();) {
				processProblem(i.next(), resource);
			}
		} catch (CoreException e) {
			System.out.println("RunAnalysisOpenMP.processResults exception: " //$NON-NLS-1$
					+ e);
			e.printStackTrace();
		}
	}

	/**
	 * Create problem marker which will put a problem on the OpenMP problems view
	 * 
	 * @param problem
	 *            - OpenMPError
	 * @param resource
	 *            - IResource
	 * @throws CoreException
	 */
	private void processProblem(OpenMPError problem, IResource resource) throws CoreException {
		// build all the attributes
		Map attrs = new HashMap();
		attrs.put(ProblemMarkerAttrIds.DESCRIPTION, problem.getDescription());
		attrs.put(ProblemMarkerAttrIds.RESOURCE, problem.getFilename());
		attrs.put(ProblemMarkerAttrIds.INFOLDER, problem.getPath());
		attrs.put(ProblemMarkerAttrIds.LOCATION, new Integer(problem.getLineno()));
		// used to reference problem if need
		attrs.put(ProblemMarkerAttrIds.PROBLEMOBJECT, problem);

		// create the marker all at once, so get ONLY a single resourceChange
		// event.
		MarkerUtilities.createMarker(resource, attrs, ProblemMarkerAttrIds.MARKER_ERROR_ID);

	}

	/**
	 * Remove the OpenMP problem markers currently set on a resource.
	 * 
	 * @param resource
	 *            - IResource
	 */
	private void removeProblemMarkers(IResource resource) {
		try {
			resource.deleteMarkers(ProblemMarkerAttrIds.MARKER_ERROR_ID, false, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			System.out.println(e);
			System.out.println(e.toString());
			System.out.println("Problem deleting markers on OMP Problems: " //$NON-NLS-1$
					+ resource.getProjectRelativePath());
		}
	}

	@Override
	protected List<String> getIncludePath() {
		return OpenMPPlugin.getDefault().getIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(OpenMPIDs.VIEW_ID);
	}

	@Override
	protected void activateProblemsView() {
		ViewActivator.activateView(IDs.VIEW_ID);
	}

}
