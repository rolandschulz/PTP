/**********************************************************************
 * Copyright (c) 2007,2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.pldt.mpi.core.actions;

import java.lang.reflect.Method;
import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.pldt.common.CommonPlugin;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.mpi.core.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCASTVisitor;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCPPASTVisitor;

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
	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu,	final List<String> includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		boolean allowPrefixOnlyMatch=MpiPlugin.getDefault().getPreferenceStore().getBoolean(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		try {
			lang = tu.getLanguage(); 
			//System.out.println("RunAnalyseMPICommandHandler: attempting to to build AST for "+tu);
			//long startTime = System.currentTimeMillis();
			IASTTranslationUnit atu = getAST(tu); // hangs w/ RemoteTools deadlock bug here sometimes
			//long endTime = System.currentTimeMillis();
			//System.out.println("RunAnalyseMPICommandHandler: time to build AST for "+tu+": "+(endTime-startTime)/1000.0+" sec");
			String languageID=lang.getId();
			if (languageID.equals(GCCLanguage.ID)) {// C
				atu.accept(new MpiCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
			}
			else if (languageID.equals(GPPLanguage.ID)) { // C++
			  atu.accept(new MpiCPPASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
			}
			else {
				// Attempt to handle Fortran
				// Instantiate using reflection to avoid static Photran dependencies
				try {
					Class<?> c = Class.forName("org.eclipse.ptp.pldt.mpi.core.actions.AnalyseMPIFortranHandler");
					Method method = c.getMethod("run", String.class, ITranslationUnit.class, String.class, ScanReturn.class);
					method.invoke(c.newInstance(), languageID, tu, fileName, msr);
				} catch (Exception e) {
					System.err.println("RunAnalyseMPIcommandHandler.doArtifactAnalysis: Photran not installed");
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
			CommonPlugin.log(IStatus.ERROR,"RunAnalyseMPICommandHandler.getAST():Error setting up visitor for project "+tu.getCProject()+" error="+e.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return msr;
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
    	boolean allowPrefixOnlyMatch= MpiPlugin.getDefault().getPreferenceStore().getBoolean(MpiIDs.MPI_RECOGNIZE_APIS_BY_PREFIX_ALONE);
    	return !allowPrefixOnlyMatch;
    }

	
	 

}
