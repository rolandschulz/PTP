package org.eclipse.ptp.pldt.mpi.core.actions;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.model.ILanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.cdt.internal.core.dom.parser.cpp.CPPASTTranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseBase;
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

	public ScanReturn doArtifactAnalysis(final ITranslationUnit tu,	final List includes) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		ILanguage lang;
		try {
			lang = tu.getLanguage();
            
			IASTTranslationUnit atu = tu.getAST();
			if (lang.getId().equals(GCCLanguage.ID)) {// cdt40
				atu.accept(new MpiCASTVisitor(includes, fileName, msr));
				// BRT FIXME  inconsistent way of accessing Language
			} else if (atu instanceof CPPASTTranslationUnit) {
				atu.accept(new MpiCPPASTVisitor(includes, fileName, msr));

			}

		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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