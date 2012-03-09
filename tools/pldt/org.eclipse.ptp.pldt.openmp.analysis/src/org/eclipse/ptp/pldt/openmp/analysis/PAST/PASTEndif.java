/**********************************************************************
 * Copyright (c) 2006,2012 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.pldt.openmp.analysis.PAST;

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
@SuppressWarnings("restriction")
public class PASTEndif extends PASTNode implements IASTPreprocessorEndifStatement {
	private IASTPreprocessorEndifStatement endif_ = null;

	/**
	 * PASTEndif - Endif proprocessor statment (Constructor)
	 * 
	 * @param endif
	 *            : ASTEndif
	 */

	public PASTEndif(IASTPreprocessorEndifStatement endif) {
		super((ASTNode) endif);
		endif_ = endif;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return endif_.copy(style);
	}

	@Override
	public String getType() {
		return "#endif"; //$NON-NLS-1$
	}

	public boolean isPartOfTranslationUnitFile() {
		return endif_.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode getOriginalNode() {
		return endif_.getOriginalNode();
	}

}
