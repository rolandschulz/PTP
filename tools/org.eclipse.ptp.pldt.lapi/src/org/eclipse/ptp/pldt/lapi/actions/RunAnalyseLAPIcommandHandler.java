/**********************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.lapi.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.lapi.Activator;
import org.eclipse.ptp.pldt.lapi.IDs;
import org.eclipse.ptp.pldt.lapi.LAPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.lapi.analysis.LapiCASTVisitor;

/**
 * @author tibbitts
 * @since 4.0
 * 
 */
public class RunAnalyseLAPIcommandHandler extends RunAnalyseHandlerBase {
	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseLAPIcommandHandler() {
		super(IDs.API_NAME, new LAPIArtifactMarkingVisitor(IDs.MARKER_ID), IDs.MARKER_ID); //$NON-NLS-1$
	}

	/**
	 * Returns LAPI analysis artifacts for file
	 * 
	 * @param tu
	 *            the translation unit representing the file to be analyzed
	 * @param includes
	 *            list of Lapi include paths
	 * @return analysis results
	 */
	@Override
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu, final List<String> includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		boolean allowPrefixOnlyMatch = Activator.getDefault().getPreferenceStore().getBoolean(IDs.RECOGNIZE_APIS_BY_PREFIX_ALONE);
		if (traceOn)
			System.out.println("RALCH:LAPI allowPrefixOnlyMatch=" + allowPrefixOnlyMatch);
		try {
			lang = tu.getLanguage();

			IASTTranslationUnit atu = tu.getAST();
			if (lang.getId().equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new LapiCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
				// atu.accept(new LapiCASTVisitor(includes, fileName, msr));
			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msr;
	}

	@Override
	protected List<String> getIncludePath() {
		return Activator.getDefault().getLapiIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(IDs.VIEW_ID);
	}

}
