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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorEndifStatement;
import org.eclipse.cdt.internal.core.dom.parser.ASTNode;

/**
 * 
 * @author pazel
 *
 */
public class PASTEndif extends PASTNode implements IASTPreprocessorEndifStatement
{
	private IASTPreprocessorEndifStatement   endif_ = null;
	
    /**
     * PASTEndif - Endif proprocessor statment (Constructor)
     * @param endif: ASTEndif
     */
    public PASTEndif(IASTPreprocessorEndifStatement endif)
    {
    	super((ASTNode)endif);
    	endif_ = endif;
    }
     
    public String getType()
    {
        return "#endif";
    }

	public boolean isPartOfTranslationUnitFile() {
		return endif_.isPartOfTranslationUnitFile();
	}

    
}
