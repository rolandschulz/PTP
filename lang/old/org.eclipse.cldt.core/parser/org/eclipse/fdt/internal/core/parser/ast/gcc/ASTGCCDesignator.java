/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation */

package org.eclipse.fdt.internal.core.parser.ast.gcc;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTDesignator;
import org.eclipse.fdt.core.parser.ast.IASTExpression;
import org.eclipse.fdt.core.parser.ast.gcc.IASTGCCDesignator;
import org.eclipse.fdt.internal.core.parser.ast.ASTDesignator;

/**
 * @author jcamelon
 *
 */
public class ASTGCCDesignator extends ASTDesignator
		implements
			IASTGCCDesignator {
	private final IASTExpression secondExpression;

	/**
	 * @param kind
	 * @param constantExpression
	 * @param fieldName
	 * @param fieldOffset
	 */
	public ASTGCCDesignator(IASTDesignator.DesignatorKind kind, IASTExpression constantExpression, char[] fieldName, int fieldOffset, IASTExpression secondSubscriptExpression) {
		super(kind, constantExpression, fieldName, fieldOffset);
		secondExpression = secondSubscriptExpression;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.gcc.IASTGCCDesignator#arraySubscriptExpression2()
	 */
	public IASTExpression arraySubscriptExpression2() {
		return secondExpression;
	}
	
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	super.acceptElement( requestor );
        if( secondExpression != null )
        	secondExpression.acceptElement(requestor);
    }
}
