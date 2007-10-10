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

package org.eclipse.ptp.pldt.mpi.core.actions;

import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.mpi.core.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCASTVisitor;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCPPASTVisitor;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * @author tibbitts
 *
 */
public class RunAnalyseMPIcommandHandler extends RunAnalyseHandlerBase
{
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
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu,	final List includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		try {
			lang = tu.getLanguage();
            
			IASTTranslationUnit atu = tu.getAST();
			if (lang.getId().equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new MpiCASTVisitor(includes, fileName, msr));
				// BRT FIXME  inconsistent way of accessing Language;
				// clean up when Fortran is integrated here
			} else if (atu instanceof CPPASTTranslationUnit) {
				atu.accept(new MpiCPPASTVisitor(includes, fileName, msr));

			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return msr;
	}


	@Override
	protected List getIncludePath() {
		return MpiPlugin.getDefault().getMpiIncludeDirs();
	}
    @Override
	protected void activateArtifactView() {
		ViewActivater.activateView(MpiIDs.MPI_VIEW_ID);
	}

	
	 

}
