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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfdefStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIfdef;

/**
 * 
 * @author pazel
 *
 */
public class PASTIfdef extends PASTNode implements IASTPreprocessorIfdefStatement
{
	protected ASTIfdef ifdef_ = null;
	
    /**
     * PASTIfdef - constructor
     * @param ifdef
     */
	public PASTIfdef(ASTIfdef ifdef)
	{
		super(ifdef);
		ifdef_ = ifdef;
	}
    
    public String getType()
    {
        return "#ifdef";
    }


	public boolean taken() {
		return ifdef_.taken();
	}

}
