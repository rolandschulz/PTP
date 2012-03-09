/**********************************************************************
 * Copyright (c) 2007, 2011 IBM Corporation and University of Illinois.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     Jeff Overbey - adaptation to OpenACC
 *******************************************************************************/
package org.eclipse.ptp.pldt.openacc.internal.actions;

import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.cdt.core.dom.ast.IASTFileLocation;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorStatement;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.pldt.common.Artifact;
import org.eclipse.ptp.pldt.common.ArtifactMarkingVisitor;
import org.eclipse.ptp.pldt.common.ScanReturn;
import org.eclipse.ptp.pldt.common.actions.RunAnalyseHandlerBase;
import org.eclipse.ptp.pldt.common.util.SourceInfo;
import org.eclipse.ptp.pldt.common.util.ViewActivator;
import org.eclipse.ptp.pldt.openacc.internal.Activator;
import org.eclipse.ptp.pldt.openacc.internal.IDs;
import org.eclipse.ptp.pldt.openacc.internal.messages.Messages;

/**
 * Handler for the &quot;Show OpenACC Artifacts&quot; command.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class RunAnalyseOpenACCcommandHandler extends RunAnalyseHandlerBase {
	private static final String VIEW_ID = "org.eclipse.ptp.pldt.openacc.views.OpenACCArtifactView"; //$NON-NLS-1$

	private static final Pattern ACC_PRAGMA_PATTERN = Pattern
			.compile("acc[ \t]*(parallel|kernels|data|host_data|loop|cache|declare|update|wait).*"); //$NON-NLS-1$

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
		final ScanReturn msr = new ScanReturn();
		final boolean allowPrefixOnlyMatch = Activator.getDefault().getPreferenceStore()
				.getBoolean(IDs.PREF_RECOGNIZE_APIS_BY_PREFIX_ALONE);
		if (traceOn)
		{
			System.out.println("RALCH:OpenACC allowPrefixOnlyMatch=" + allowPrefixOnlyMatch); //$NON-NLS-1$
		}
		try {
			final IASTTranslationUnit atu = tu.getAST();
			final String languageID = tu.getLanguage().getId();
			if (languageID.equals(GCCLanguage.ID) || languageID.equals(GPPLanguage.ID)) {
				findOpenACCFunctionCalls(includes, msr, tu.getElementName(), atu, allowPrefixOnlyMatch);
				findOpenACCPragmas(atu, msr);
			}
		} catch (final CoreException e) {
			Activator.log(e);
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

	@Override
	protected List<String> getIncludePath() {
		return Activator.getDefault().getOpenACCIncludeDirs();
	}

	@Override
	protected void activateArtifactView() {
		ViewActivator.activateView(VIEW_ID);
	}
}
