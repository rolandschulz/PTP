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
package org.eclipse.ptp.pldt.openmp.core.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTNodeLocation;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.resources.IFile;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactAnalysisBase;
import org.eclipse.ptp.pldt.common.IArtifactAnalysis;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPAnalysisManager;
import org.eclipse.ptp.pldt.openmp.analysis.OpenMPErrorManager;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTNode;
import org.eclipse.ptp.pldt.openmp.analysis.PAST.PASTPragma;
import org.eclipse.ptp.pldt.openmp.core.OpenMPScanReturn;
import org.eclipse.ptp.pldt.openmp.core.messages.Messages;

/**
 * OpenMP artifact analysis for C.
 * <p>
 * Contributed to the <code>org.eclipse.ptp.pldt.openmp.core.artifactAnalysis</code> extension point.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class OpenMPCArtifactAnalysis extends ArtifactAnalysisBase implements IArtifactAnalysis {

	private static final String OPENMP_DIRECTIVE = Messages.RunAnalyseOpenMPcommandHandler_OpenMP_directive;

	public ScanReturn runArtifactAnalysis(String languageID, ITranslationUnit tu, List<String> includes, boolean allowPrefixOnlyMatch) {
		final OpenMPScanReturn msr = new OpenMPScanReturn();
		final String fileName = tu.getElementName();
		final IASTTranslationUnit atu = getAST(tu); // use index; was tu.getAST()
		if (atu != null) {
			atu.accept(new OpenMPCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));

			// Find the OpenMP #pragmas
			if (tu.getResource() instanceof IFile) {
				processOpenMPPragmas(msr, atu, (IFile) tu.getResource());
			}
		}
		return msr;
	}


	/**
	 * Special processing to find #pragmas, since the CDT AST does not normally include them.<br>
	 * Also adds the "OpenMP Problems"
	 * 
	 * @param msr
	 * @param astTransUnit
	 * @param iFile
	 */
	protected void processOpenMPPragmas(OpenMPScanReturn msr, IASTTranslationUnit astTransUnit, IFile iFile) {
		OpenMPAnalysisManager omgr = new OpenMPAnalysisManager(astTransUnit, iFile);
		PASTNode[] pList = omgr.getPAST();

		for (int i = 0; i < pList.length; i++) {// length local=3271; remote 4 (!!)
			PASTNode temp = pList[i];
			String tempStr = temp.getRawSignature();
			// local: will be a PASTOMPPragma node; remote: will be a PASTPragma node.
			// So workaround is to accept a PASTPragma node here so we can handle remote files.
			// Need to investigate what this does to further analysis e.g. concurrency analysis.
			if (pList[i] instanceof PASTPragma) {// was PASTOMPPragma

				PASTPragma pop = (PASTPragma) pList[i];
				//if (traceOn) {
				//	System.out.println("found #pragma, line " + pop.getStartingLine()); //$NON-NLS-1$
				//}
				SourceInfo si = getSourceInfo(pop, Artifact.PRAGMA);
				String shortName = pop.getContent();
				if (shortName.length() == 0) {
					shortName = "#pragma"; // HACK: workaround for remote files where getContent() is always empty.
					// The same reason why this is empty is also (I think) why it's not a PASTOMPPragma node.
					// PASTOMPFactory.parse() always finds empty token first on a remote file, so aborts.
				}
				Artifact a = new Artifact(pop.getFilename(), pop.getStartingLine(), pop.getStartLocation(), shortName,
						OPENMP_DIRECTIVE, si, pop);
				msr.addArtifact(a);
			}
		}

		msr.addProblems(OpenMPErrorManager.getCurrentErrorManager().getErrors());
	}


	/**
	 * Get exact source locational info for a function call
	 * 
	 * @param pastNode
	 * @param constructType
	 * @return
	 */
	private SourceInfo getSourceInfo(PASTNode pastNode, int constructType) {
		SourceInfo sourceInfo = null;
		IASTNodeLocation[] locations = pastNode.getNodeLocations();
		if (locations.length == 1) {
			IASTFileLocation astFileLocation = null;
			if (locations[0] instanceof IASTFileLocation) {
				astFileLocation = (IASTFileLocation) locations[0];
				sourceInfo = new SourceInfo();
				sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
				sourceInfo.setStart(astFileLocation.getNodeOffset());
				sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
				sourceInfo.setConstructType(constructType);
			}
		}
		return sourceInfo;
	}
}
