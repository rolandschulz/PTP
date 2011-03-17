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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTIfndef extends PASTNode implements IASTPreprocessorIfndefStatement {
	protected IASTPreprocessorIfndefStatement ifndef_ = null;

	/**
	 * PASTIfndef - constructor
	 * 
	 * @param ifndef
	 */
	public PASTIfndef(IASTPreprocessorIfndefStatement ifndef) {
		super((ASTNode) ifndef);
		ifndef_ = ifndef;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return ifndef_.copy(style);
	}

	@Override
	public String getType() {
		return "#ifndef"; //$NON-NLS-1$
	}

	public boolean taken() {
		return ifndef_.taken();
	}

	public char[] getCondition() {
		return ifndef_.getCondition();
	}

	public IASTName getMacroReference() {
		return ifndef_.getMacroReference();
	}

	public boolean isPartOfTranslationUnitFile() {
		return ifndef_.isPartOfTranslationUnitFile();
	}

}
