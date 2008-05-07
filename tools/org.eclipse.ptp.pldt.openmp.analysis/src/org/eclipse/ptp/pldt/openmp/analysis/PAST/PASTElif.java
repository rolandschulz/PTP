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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorElifStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * @author pazel
 */
public class PASTElif extends PASTNode  implements IASTPreprocessorElifStatement
{
	protected IASTPreprocessorElifStatement elif_ = null;
	
    /**
     * PASTElif - Elif preprocessor statement (constructor)
     * @param elif: ASTElif
     */
	public PASTElif(IASTPreprocessorElifStatement elif)
	{
		super((ASTNode)elif);
		elif_ = elif;
	}
    
    public String getType()
    {
        return "#elif";
    }

    /**
     * taken - reflects ASTElif choice
     */
	public boolean taken() {
		return elif_.taken();
	}

	public char[] getCondition() {
		return elif_.getCondition();
	}

	public boolean isPartOfTranslationUnitFile() {
		return elif_.isPartOfTranslationUnitFile();
	}

}
