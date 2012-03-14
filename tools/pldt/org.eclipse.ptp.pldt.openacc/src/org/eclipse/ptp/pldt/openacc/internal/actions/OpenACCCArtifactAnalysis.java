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
package org.eclipse.ptp.pldt.openacc.internal.actions;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactAnalysisBase;
import org.eclipse.ptp.pldt.common.IArtifactAnalysis;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;

/**
 * OpenACC artifact analysis for C and C++.
 * <p>
 * Contributed to the <code>org.eclipse.ptp.pldt.openacc.artifactAnalysis</code> extension point.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class OpenACCCArtifactAnalysis extends ArtifactAnalysisBase implements IArtifactAnalysis {

	private static final Pattern ACC_PRAGMA_PATTERN = Pattern
			.compile("acc[ \t]*(parallel|kernels|data|host_data|loop|cache|declare|update|wait).*"); //$NON-NLS-1$

	@Override
	public ScanReturn runArtifactAnalysis(String languageID, ITranslationUnit tu, List<String> includes, boolean allowPrefixOnlyMatch) {
		final ScanReturn msr = new ScanReturn();
		final IASTTranslationUnit atu = getAST(tu); // use index; was tu.getAST();
		if (atu != null) {
			findOpenACCFunctionCalls(includes, msr, tu.getElementName(), atu, allowPrefixOnlyMatch);
			findOpenACCPragmas(atu, msr);
		}
		return msr;
	}

	private void findOpenACCFunctionCalls(final List<String> includes,
			final ScanReturn msr, final String fileName,
			final IASTTranslationUnit atu, boolean allowPrefixOnlyMatch) {
		atu.accept(new OpenACCCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
	}

	private void findOpenACCPragmas(final IASTTranslationUnit atu, final ScanReturn msr) {
		for (final IASTPreprocessorStatement preprocStmt : atu.getAllPreprocessorStatements()) {
			if (preprocStmt instanceof IASTPreprocessorPragmaStatement) {
				final String pragmaText = String.valueOf(((IASTPreprocessorPragmaStatement) preprocStmt).getMessage()).trim();
				if (preprocStmt.isPartOfTranslationUnitFile() && ACC_PRAGMA_PATTERN.matcher(pragmaText).matches()) {
					final IASTFileLocation astFileLocation = preprocStmt.getFileLocation();
					final SourceInfo sourceInfo = new SourceInfo();
					sourceInfo.setStartingLine(astFileLocation.getStartingLineNumber());
					sourceInfo.setStart(astFileLocation.getNodeOffset());
					sourceInfo.setEnd(astFileLocation.getNodeOffset() + astFileLocation.getNodeLength());
					sourceInfo.setConstructType(Artifact.PRAGMA);

					msr.addArtifact(
							new Artifact(
									preprocStmt.getContainingFilename(),
									astFileLocation.getStartingLineNumber(),
									1,
									"#pragma " + pragmaText, //$NON-NLS-1$
									Messages.RunAnalyseOpenACCcommandHandler_OpenACC_directive,
									sourceInfo,
									null));
				}
			}
		}
	}
}
