/**********************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTName;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTIfdef extends PASTNode implements IASTPreprocessorIfdefStatement {
	protected IASTPreprocessorIfdefStatement ifdef_ = null;

	/**
	 * PASTIfdef - constructor
	 * 
	 * @param ifdef
	 */
	public PASTIfdef(IASTPreprocessorIfdefStatement ifdef) {
		super((ASTNode) ifdef);
		ifdef_ = ifdef;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return ifdef_.copy(style);
	}

	@Override
	public String getType() {
		return "#ifdef"; //$NON-NLS-1$
	}

	public boolean taken() {
		return ifdef_.taken();
	}

	public char[] getCondition() {
		return ifdef_.getCondition();
	}

	public IASTName getMacroReference() {
		return ifdef_.getMacroReference();
	}

	public boolean isPartOfTranslationUnitFile() {
		return ifdef_.isPartOfTranslationUnitFile();
	}

}
