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
package org.eclipse.ptp.pldt.mpi.core.actions;

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.mpi.core.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.internal.core.MpiIDs;

/**
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class RunAnalyseMPIcommandHandler extends RunAnalyseHandlerBase {
	/**
	 * ID for the extension point which allows plug-ins to contribute MPI artifact analyses based on language IDs.
	 */
	private static final String EXTENSION_POINT_ID = "org.eclipse.ptp.pldt.mpi.core.artifactAnalysis";

	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseMPIcommandHandler() {
		super("MPI", new MPIArtifactMarkingVisitor(MpiIDs.MARKER_ID), MpiIDs.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns MPI analysis artifacts for file
	 * 
	 * @param file
	 * @param includes
	 *            MPI include paths
	 * @return
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		final boolean allowPrefixOnlyMatch = MpiPlugin.getDefault().getPreferenceStore().getBoolean(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		return runArtifactAnalysisFromExtensionPoint(EXTENSION_POINT_ID, tu, includes, allowPrefixOnlyMatch);
	}

	@Override
	protected List<String> getIncludePath() {
		return MpiPlugin.getDefault().getMpiIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(MpiIDs.MPI_VIEW_ID);
	}

	@Override
	public boolean areIncludePathsNeeded() {
		boolean allowPrefixOnlyMatch = MpiPlugin.getDefault().getPreferenceStore()
				.getBoolean(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		return !allowPrefixOnlyMatch;
	}

}
