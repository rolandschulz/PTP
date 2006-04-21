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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfndefStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIfndef;

/**
 * 
 * @author pazel
 *
 */
public class PASTIfndef extends PASTNode implements IASTPreprocessorIfndefStatement
{
	protected ASTIfndef ifndef_ = null;
	
    /**
     * PASTIfndef - constructor
     * @param ifndef
     */
	public PASTIfndef(ASTIfndef ifndef)
	{
		super(ifndef);
		ifndef_ = ifndef;
	}
    
    public String getType()
    {
        return "#ifndef";
    }


	public boolean taken() {
		return ifndef_.taken();
	}

}
