/**********************************************************************
 * Copyright (c) 2007, 2011, 2012 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey - adaptation to OpenACC, modified to use extension point
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.actions;

import java.util.List;

import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.IDs;

/**
 * Handler for the &quot;Show OpenACC Artifacts&quot; command.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class RunAnalyseOpenACCcommandHandler extends RunAnalyseHandlerBase {
	/**
	 * ID for the OpenACC Artifacts view, contributed in plugin.xml.
	 */
	private static final String VIEW_ID = "org.eclipse.ptp.pldt.openacc.views.OpenACCArtifactView"; //$NON-NLS-1$

	/**
	 * ID for the extension point which allows plug-ins to contribute OpenMP artifact analyses based on language IDs.
	 */
	private static final String EXTENSION_POINT_ID = "org.eclipse.ptp.pldt.openacc.artifactAnalysis"; //$NON-NLS-1$

	/**
	 * Constructor.  Invoked dynamically due to class reference in plugin.xml.
	 */
	public RunAnalyseOpenACCcommandHandler() {
		super("OpenACC", new ArtifactMarkingVisitor(IDs.MARKER_ID), IDs.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns OpenACC analysis artifacts for file
	 * 
	 * @param tu
	 *            the translation unit representing the file to be analyzed
	 * @param includes
	 *            list of OpenACC include paths
	 * @return analysis results
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		final boolean allowPrefixOnlyMatch = Activator.getDefault().getPreferenceStore().getBoolean(IDs.PREF_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		return runArtifactAnalysisFromExtensionPoint(EXTENSION_POINT_ID, tu, includes, allowPrefixOnlyMatch);
	}

	@Override
	protected List<String> getIncludePath() {
		return Activator.getDefault().getOpenACCIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(VIEW_ID);
	}
}
