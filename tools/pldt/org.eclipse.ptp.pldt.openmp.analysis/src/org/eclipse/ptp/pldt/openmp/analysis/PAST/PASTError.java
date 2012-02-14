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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
@SuppressWarnings("restriction")
public class PASTError extends PASTNode implements IASTPreprocessorErrorStatement {
	protected IASTPreprocessorErrorStatement error_ = null;

	/**
	 * PASTError - Error preprocessor statment (Constructor)
	 * 
	 * @param error
	 *            : ASTError
	 */
	public PASTError(IASTPreprocessorErrorStatement error) {
		super((ASTNode) error);
		error_ = error;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return error_.copy(style);
	}

	@Override
	public String getType() {
		return "#error"; //$NON-NLS-1$
	}

	public char[] getMessage() {
		return error_.getMessage();
	}

	public boolean isPartOfTranslationUnitFile() {
		return error_.isPartOfTranslationUnitFile();
	}

	@Override
	public IASTNode getOriginalNode() {
		return error_.getOriginalNode();
	}

}
