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

import org.eclipse.cdt.core.dom.ast.IASTPreprocessorErrorStatement;
import org.eclipse.cdt.internal.core.parser.scanner2.LocationMap.ASTError;

/**
 * 
 * @author pazel
 *
 */
public class PASTError extends PASTNode implements IASTPreprocessorErrorStatement
{
	protected ASTError error_ = null;
	
    /**
     * PASTError - Error preprocessor statment (Constructor)
     * @param error: ASTError
     */
	public PASTError(ASTError error)
	{
		super(error);
		error_ = error;
	}
    
    public String getType()
    {
        return "#error";
    }

	public char[] getMessage() {
		return error_.getMessage();
	}


}
