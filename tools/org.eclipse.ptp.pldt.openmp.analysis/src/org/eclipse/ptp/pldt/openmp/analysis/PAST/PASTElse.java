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

import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElseStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTElse extends PASTNode implements IASTPreprocessorElseStatement {
	protected IASTPreprocessorElseStatement else_ = null;

	/**
	 * PASTElse - Else preprocessor statement (Constructor)
	 * 
	 * @param elsee
	 */
	public PASTElse(IASTPreprocessorElseStatement elsee) {
		super((ASTNode) elsee);
		else_ = elsee;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return else_.copy(style);
	}

	@Override
	public String getType() {
		return "#else"; //$NON-NLS-1$
	}

	/**
	 * taken - reflects ASTElse choice
	 */
	public boolean taken() {
		return else_.taken();
	}

	public boolean isPartOfTranslationUnitFile() {
		return else_.isPartOfTranslationUnitFile();
	}

}
