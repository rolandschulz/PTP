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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 * 
 */
public class PASTUndef extends PASTNode implements IASTPreprocessorUndefStatement {
	protected IASTPreprocessorUndefStatement undef_ = null;

	/**
	 * PASTUndef - constructor
	 * 
	 * @param undef
	 */
	public PASTUndef(IASTPreprocessorUndefStatement undef) {
		super((ASTNode) undef);
		undef_ = undef;
	}

	/**
	 * @since 4.1
	 */
	public IASTNode copy(CopyStyle style) {
		return undef_.copy(style);
	}

	@Override
	public String getType() {
		return "#undef"; //$NON-NLS-1$
	}

	/**
	 * Delegate methods for IASTPreprocessorUndefStatement
	 */
	public IASTName getMacroName() {
		return undef_.getMacroName();
	}

	public boolean isPartOfTranslationUnitFile() {
		return undef_.isPartOfTranslationUnitFile();
	}

}
