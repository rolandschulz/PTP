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
package org.eclipse.fdt.internal.core.parser.ast.complete;

import java.util.List;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.ASTAccessVisibility;
import org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier;
import org.eclipse.fdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.fdt.internal.core.parser.Parser;
import org.eclipse.fdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTBaseSpecifier implements IASTBaseSpecifier
{
	private List references;
    private final boolean isVirtual; 
	private final ISymbol symbol;
	private final ASTAccessVisibility visibility;
	private final int offset;
    /**
     * @param symbol
     * @param b
     * @param visibility
     */
    public ASTBaseSpecifier(ISymbol symbol, boolean b, ASTAccessVisibility visibility, int nameOffset, List references)
    {
   		isVirtual = b; 
   		this.visibility = visibility;     
   		this.symbol = symbol; 
   		this.offset = nameOffset;
   		this.references = references; 
    }
   
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier#getAccess()
     */
    public ASTAccessVisibility getAccess()
    {
        return visibility;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier#isVirtual()
     */
    public boolean isVirtual()
    {
        return isVirtual;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier#getParentClassName()
     */
    public String getParentClassName()
    {
        return String.valueOf(symbol.getName());
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier#getParentClassSpecifier()
     */
    public IASTTypeSpecifier getParentClassSpecifier()
    {
        return (IASTTypeSpecifier)symbol.getASTExtension().getPrimaryDeclaration();
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTBaseSpecifier#getNameOffset()
     */
    public int getNameOffset()
    {
        return offset;
    }



    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    	Parser.processReferences( references, requestor );
    	references = null;
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
