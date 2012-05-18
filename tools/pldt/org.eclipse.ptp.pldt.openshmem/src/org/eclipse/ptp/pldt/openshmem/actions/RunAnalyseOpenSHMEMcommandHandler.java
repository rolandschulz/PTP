/**********************************************************************
 * Copyright (c) 2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openshmem.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.openshmem.Activator;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.openshmem.OpenSHMEMIDs;
import org.eclipse.ptp.pldt.openshmem.analysis.OpenSHMEMCASTVisitor;

public class RunAnalyseOpenSHMEMcommandHandler extends RunAnalyseHandlerBase {
	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseOpenSHMEMcommandHandler() {
		super("OpenSHMEM", new OpenSHMEMArtifactMarkingVisitor(OpenSHMEMIDs.MARKER_ID), OpenSHMEMIDs.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns OpenSHMEM analysis artifacts for file
	 * 
	 * @param tu
	 *            the translation unit representing the file to be analyzed
	 * @param includes
	 *            list of openshmem include paths
	 * @return analysis results
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		boolean allowPrefixOnlyMatch = Activator.getDefault().getPreferenceStore()
				.getBoolean(OpenSHMEMIDs.OpenSHMEM_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		if (traceOn)
			System.out.println("RALCH:OpenSHMEM allowPrefixOnlyMatch=" + allowPrefixOnlyMatch);
		try {
			lang = tu.getLanguage();
			String langID = lang.getId();

			IASTTranslationUnit atu = tu.getAST();
			if (langID.equals(GCCLanguage.ID) || (langID.equals(GPPLanguage.ID))) {// cdt40
				atu.accept(new OpenSHMEMCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
				// atu.accept(new OpenSHMEMCASTVisitor(includes, fileName,
				// msr));
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msr;
	}

	@Override
	protected List<String> getIncludePath() {
		return Activator.getDefault().getIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(OpenSHMEMIDs.OpenSHMEM_VIEW_ID);
	}

	/**
	 * OpenSHMEM doesn't have a problems view (only OpenMP analysis does)
	 */
	@Override
	protected void activateProblemsView() {

	}
	
	/**
	 * returns true if include paths must be set for this implementation.
	 */
	public boolean areIncludePathsNeeded() {
		return false;
	}

}
