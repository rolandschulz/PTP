package org.eclipse.ptp.pldt.fortran.mpi.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.resources.IFile;

//import org.eclipse.ptp.mptools.mpi.core.analysis.MpiFortranASTVisitor;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseBase;
import org.eclipse.ptp.pldt.common.util.AnalysisUtil;
import org.eclipse.ptp.pldt.common.util.ViewActivater;


import org.eclipse.ptp.pldt.fortran.mpi.Activator;
import org.eclipse.ptp.pldt.fortran.mpi.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.fortran.mpi.MpiIDs;
import org.eclipse.ptp.pldt.fortran.mpi.analysis.MpiFortranASTVisitor;

/**
 * 
 * Run analysis to create MPI artifact markers. <br>
 * The analysis is done in the doMpiCallAnalysis() method
 * 
 * 
 * IObjectActionDelegate enables popup menu selection IWindowActionDelegate
 * enables toolbar(or menu) selection
 */
public class RunAnalyseFortranMPI extends RunAnalyseBase {
	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseFortranMPI() {
		super("MPI", new MPIArtifactMarkingVisitor(MpiIDs.MARKER_ID), MpiIDs.MARKER_ID);
	}


	public ScanReturn doArtifactAnalysisOriginal(final IFile file, final List /*
																				 * of
																				 * String
																				 */includes_) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = file.getName();
		ParserLanguage lang = AnalysisUtil.getLanguageFromFile(file);
		System.out.println("lang=" + lang);
		try {
			IASTTranslationUnit astTransUnit = CDOM.getInstance().getASTService().getTranslationUnit(file,
					CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
//			if (lang == ParserLanguage.C) {
//				astTransUnit.accept(new MpiCASTVisitor(includes_, fileName, msr));
//			} else if (lang == ParserLanguage.CPP) {
//				astTransUnit.accept(new MpiCPPASTVisitor(includes_, fileName, msr));
//
//			}
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
		}

		return msr;
	}
	/**
	 * Returns MPI analysis artifacts for file
	 * 
	 * @param file
	 * @param includes
	 *            MPI include paths
	 * @return
	 */
	public ScanReturn doArtifactAnalysis(final IFile file, final List /*
																		 * of
																		 * String
																		 */includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = file.getName();
		ParserLanguage lang = AnalysisUtil.getLanguageFromFile(file);
		System.out.println("lang=" + lang);
		boolean isFortran = true;// how to determine?
		if (!isFortran) {

		}

		else { // if fortran file
			try {
				MpiFortranASTVisitor fv = new MpiFortranASTVisitor(null, fileName, msr);
				fv.doit(file);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return msr;
	}

	protected List getIncludePath() {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.pldt.common.actions.RunAnalyseBase#activateArtifactView()
	 */
	protected void activateArtifactView() {
		ViewActivater.activateView(MpiIDs.MPI_VIEW_ID);
	}

	protected void activateProblemsView() {

	}


	/**
	 * Include paths are not needed for Fortran
	 * @see org.eclipse.ptp.pldt.common.actions.RunAnalyseBase#areIncludePathsNeeded()
	 */
	@Override
	public boolean areIncludePathsNeeded() {
		return false;
	}

}