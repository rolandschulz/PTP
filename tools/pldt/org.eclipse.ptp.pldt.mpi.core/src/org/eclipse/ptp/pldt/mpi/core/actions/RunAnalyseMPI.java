package org.eclipse.ptp.pldt.mpi.core.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.CDOM;
import org.eclipse.cdt.core.dom.IASTServiceProvider;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.parser.ParserLanguage;
import org.eclipse.core.resources.IFile;

import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseBase;
import org.eclipse.ptp.pldt.common.util.AnalysisUtil;
import org.eclipse.ptp.pldt.common.util.ViewActivater;
import org.eclipse.ptp.pldt.mpi.core.MPIArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.mpi.core.MpiIDs;
import org.eclipse.ptp.pldt.mpi.core.MpiPlugin;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCASTVisitor;
import org.eclipse.ptp.pldt.mpi.core.analysis.MpiCPPASTVisitor;

/**
 * 
 * Run analysis to create MPI artifact markers. <br>
 * The analysis is done in the doMpiCallAnalysis() method
 * 
 * 
 * IObjectActionDelegate enables popup menu selection IWindowActionDelegate
 * enables toolbar(or menu) selection
 */
public class RunAnalyseMPI extends RunAnalyseBase {
	/**
	 * Constructor for the "Run Analysis" action
	 */
	public RunAnalyseMPI() {
		super("MPI", new MPIArtifactMarkingVisitor(MpiIDs.MARKER_ID), MpiIDs.MARKER_ID);
	}

	/**
	 * Returns MPI analysis artifacts for file
	 * 
	 * @param file
	 * @param includes
	 *            MPI include paths
	 * @return
	 */
	public ScanReturn doArtifactAnalysis(final IFile file, final List includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = file.getName();
		ParserLanguage lang = AnalysisUtil.getLanguageFromFile(file);
		//System.out.println("lang=" + lang);
		try {
			IASTTranslationUnit astTransUnit = CDOM.getInstance().getASTService().getTranslationUnit(file,
					CDOM.getInstance().getCodeReaderFactory(CDOM.PARSE_SAVED_RESOURCES));
			if (lang == ParserLanguage.C) {
				astTransUnit.accept(new MpiCASTVisitor(includes, fileName, msr));
			} else if (lang == ParserLanguage.CPP) {
				astTransUnit.accept(new MpiCPPASTVisitor(includes, fileName, msr));

			}
		} catch (IASTServiceProvider.UnsupportedDialectException e) {
			System.out.println("RunAnalyseMPI, UnsupportedDialectException "+e.getMessage());
		}

		return msr;
	}


	protected List getIncludePath() {
		return MpiPlugin.getDefault().getMpiIncludeDirs();
	}

	protected void activateArtifactView() {
		ViewActivater.activateView(MpiIDs.MPI_VIEW_ID);
	}

	protected void activateProblemsView() {

	}

}