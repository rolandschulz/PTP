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
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification;
import org.eclipse.fdt.core.parser.ast.IASTScope;

/**
 * @author jcamelon
 *
 */
public class ASTLinkageSpecification
	extends ASTDeclaration
	implements IASTDeclaration, IASTLinkageSpecification, IASTQScope {

	private final char[] linkage; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

	public ASTLinkageSpecification( IASTScope scope, char[] linkage, int startingOffset, int startingLine, char[] filename )
	{
		super( scope );
		this.linkage = linkage;
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		fn = filename;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTLinkageSpecification#getLinkageString()
	 */
	public String getLinkageString() {
		return String.valueOf(linkage);
	}

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
            requestor.enterLinkageSpecification(this);
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
            requestor.exitLinkageSpecification(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset;
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }

}
