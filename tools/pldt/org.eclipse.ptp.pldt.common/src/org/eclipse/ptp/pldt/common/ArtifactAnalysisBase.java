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
package org.eclipse.ptp.pldt.common;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.dom.ast.IASTTranslationUnit;
import org.eclipse.cdt.core.index.IIndex;
import org.eclipse.cdt.core.model.ITranslationUnit;
import org.eclipse.core.runtime.IStatus;

/**
 * Base class for implementations of {@link IArtifactAnalysis} that handle C and
 * C++.
 * <p>
 * This class contains the {@link #getAST(ITranslationUnit)} utility method, which returns an {@link IASTTranslationUnit} for an
 * {@link ITranslationUnit}.
 * 
 * @author Beth Tibbitts
 * @author Jeff Overbey
 * 
 * @since 6.0
 */
public abstract class ArtifactAnalysisBase implements IArtifactAnalysis {
	/**
	 * Get AST from index, not full translation unti
	 * 
	 * @param tu
	 *            translation unit from which to get the AST
	 * @return {@link IASTTranslationUnit} or <code>null</code>
	 */
	protected IASTTranslationUnit getAST(ITranslationUnit tu) {
		try {
			IIndex index = CCorePlugin.getIndexManager().getIndex(tu.getCProject());
			return tu.getAST(index, ITranslationUnit.AST_SKIP_ALL_HEADERS);
		} catch (Exception e) {
			CommonPlugin
					.log(IStatus.ERROR,
							"ArtifactAnalysisBase.getAST(): Error getting AST (from index) for project " + tu.getCProject() + " " + e.getMessage()); //$NON-NLS-1$
			return null;
		}
	}
}
