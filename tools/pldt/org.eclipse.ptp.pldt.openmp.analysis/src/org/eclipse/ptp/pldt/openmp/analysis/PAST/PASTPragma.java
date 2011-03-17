/**********************************************************************
 * Copyright (c) 2006,2007 IBM Corporation.
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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorPragmaStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;
import org.eclipse.ptp.pldt.common.util.Utility;

/**
 * Pseudo node for IAST pragma nodes
 * 
 * @author pazel
 * 
 */
public class PASTPragma extends PASTNode implements IASTPreprocessorPragmaStatement {
	protected IASTPreprocessorPragmaStatement pragma_ = null;
	protected String content_ = "";

	/**
	 * PASTPragma - constructor
	 * 
	 * @param pragma
	 */
	public PASTPragma(IASTPreprocessorPragmaStatement pragma) {
		super((ASTNode) pragma);
		pragma_ = pragma;
	}

	/**
	 * PASTPragma - Copy constructor
	 * 
	 * @param pPragma
	 *            : PASTPragma
	 */
	public PASTPragma(PASTPragma pPragma) {
		super(pPragma);
		pragma_ = pPragma.getASTPragma();
		content_ = pPragma.getContent();
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return pragma_.copy(style);
	}

	protected IASTPreprocessorPragmaStatement getASTPragma() {
		return pragma_;
	}

	@Override
	public String getType() {
		return "#pragma"; //$NON-NLS-1$
	}

	/**
	 * Is this the pragma statement itself, or the state to which it implies?
	 * (immediately following, "scope" of pragma)
	 * 
	 * @return
	 */
	public String getContent() {
		// return pragma_.getContent();
		return content_;
	}

	/**
	 * getOffset - relative to the AST, i.e. include files factored into offset
	 * 
	 * @return int
	 */
	public int getOffset() {
		// return getStartLocation();
		// https://bugs.eclipse.org/bugs/show_bug.cgi?id=253200 fixed
		return astNode_.getOffset();
	}

	/**
	 * getLength - get the length of the pragma
	 * 
	 * @return int
	 */
	public int getLength() {
		return getEndLocation() - getStartLocation();
	}

	/**
	 * getLocalOffset - relative to containing file, i.e. include files factored
	 * out offset
	 * 
	 * @return int
	 */
	public int getLocalOffset() {
		Utility.Location l = Utility.getLocation(pragma_);
		return l.low_;
	}

	public void setContent(String content) {
		content_ = content;
	}

	public char[] getMessage() {
		return pragma_.getMessage();
	}

	public boolean isPartOfTranslationUnitFile() {
		return pragma_.isPartOfTranslationUnitFile();
	}

	/**
	 * @since 4.0
	 */
	public boolean isPragmaOperator() {
		return pragma_.isPragmaOperator();

	}

}
