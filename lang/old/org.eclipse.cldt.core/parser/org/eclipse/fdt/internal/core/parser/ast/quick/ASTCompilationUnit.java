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
package org.eclipse.fdt.internal.core.parser.ast.quick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.IASTCompilationUnit;
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTCompilationUnit extends ASTNode implements IASTCompilationUnit, IASTQScope {

	private List declarations = new ArrayList(); 
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() {
		return declarations.iterator();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.internal.core.parser.ast.quick.IASTQScope#addDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
	 */
	public void addDeclaration(IASTDeclaration declaration) {
		declarations.add( declaration );
	}

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
        try
        {
            requestor.enterCompilationUnit(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
        
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.exitCompilationUnit(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
}
