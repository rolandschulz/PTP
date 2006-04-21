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
import org.eclipse.cdt.core.dom.ast.IASTPreprocessorUndefStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTUndef;

/**
 * 
 * @author pazel
 *
 */
public class PASTUndef extends PASTNode implements IASTPreprocessorUndefStatement
{
	protected ASTUndef undef_ = null;
	
    /**
     * PASTUndef - constructor
     * @param undef
     */
	public PASTUndef(ASTUndef undef)
	{
		super(undef);
		undef_ = undef;
	}
    
    public String getType()
    {
        return "#undef";
    }

    /**
     * Delegate methods for IASTPreprocessorUndefStatement 
     */
	public IASTName getMacroName() {
		return undef_.getMacroName();
	}

}
