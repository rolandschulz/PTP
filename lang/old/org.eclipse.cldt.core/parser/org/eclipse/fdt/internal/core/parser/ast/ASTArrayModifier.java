/**********************************************************************
 * Copyright (c) 2002,2003 Rational Software Corporation and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v0.5
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v05.html
 * 
 * Contributors: 
 * IBM Rational Software - Initial API and implementation
***********************************************************************/
package org.eclipse.fdt.internal.core.parser.ast;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTArrayModifier;
import org.eclipse.fdt.core.parser.ast.IASTExpression;

/**
 * @author jcamelon
 *
 */
public class ASTArrayModifier implements IASTArrayModifier
{
	private final IASTExpression expression;
    /**
     * @param exp
     */
    public ASTArrayModifier(IASTExpression exp)
    {
        expression = exp; 
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.internal.core.parser.ast.IASTArrayModifier#getExpression()
     */
    public IASTExpression getExpression()
    {
        return expression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	if( expression != null )
    		expression.acceptElement( requestor );
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#enterScope(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#exitScope(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
}
