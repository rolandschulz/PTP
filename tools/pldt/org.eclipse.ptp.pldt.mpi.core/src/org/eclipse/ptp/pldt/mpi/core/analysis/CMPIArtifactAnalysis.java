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
package org.eclipse.ptp.pldt.mpi.core.analysis;

import java.util.List;

import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.dom.ast.gnu.c.GCCLanguage;
import org.eclipse.cdt.core.dom.ast.gnu.cpp.GPPLanguage;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.ptp.pldt.common.ArtifactAnalysisBase;
import org.eclipse.ptp.pldt.common.IArtifactAnalysis;
import org.eclipse.ptp.pldt.common.ScanReturn;

/**
 * MPI artifact analysis for C and C++.
 * <p>
 * Contributed to the <code>org.eclipse.ptp.pldt.mpi.core.artifactAnalysis</code> extension point.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 */
public class CMPIArtifactAnalysis extends ArtifactAnalysisBase implements IArtifactAnalysis {
	public ScanReturn runArtifactAnalysis(String languageID, ITranslationUnit tu, List<String> includes, boolean allowPrefixOnlyMatch) {
		final ScanReturn msr = new ScanReturn();
		final String fileName = tu.getElementName();
		final IASTTranslationUnit atu = getAST(tu); // use index; was tu.getAST();
		if (atu != null) {
			if (languageID.equals(GCCLanguage.ID)) { // C
				atu.accept(new MpiCASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
			} else if (languageID.equals(GPPLanguage.ID)) { // C++
				atu.accept(new MpiCPPASTVisitor(includes, fileName, allowPrefixOnlyMatch, msr));
			} else {
				throw new IllegalStateException("Unexpected language ID " + languageID);
			}
		}
		return msr;
	}
}
