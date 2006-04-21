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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorIfStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTIf;

/**
 * 
 * @author pazel
 *
 */
public class PASTIf extends PASTNode implements IASTPreprocessorIfStatement
{
	protected ASTIf if_ = null;
	
    /**
     * PASTIf - constructor
     * @param ifx
     */
	public PASTIf(ASTIf ifx)
	{
		super(ifx);
		if_ = ifx;
	}
    
    public String getType()
    {
        return "#if";
    }

	public boolean taken() {
		return if_.taken();
	}

}
