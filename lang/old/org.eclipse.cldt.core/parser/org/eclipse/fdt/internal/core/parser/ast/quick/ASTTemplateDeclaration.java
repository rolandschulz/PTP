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
import org.eclipse.fdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.fdt.core.parser.ast.IASTDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTScope;
import org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration;
import org.eclipse.fdt.internal.core.parser.ast.EmptyIterator;

/**
 * @author jcamelon
 *
 */
public class ASTTemplateDeclaration extends ASTDeclaration implements IASTTemplateDeclaration
{
    private IASTDeclaration ownedDeclaration;
    private List templateParameters;
    private final boolean isExported; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param templateParameters
     * @param filename
     */
    public ASTTemplateDeclaration(IASTScope scope, List templateParameters, int startingOffset, int startingLine, boolean isExported, char[] filename)
    {
        super( scope );
        this.templateParameters = templateParameters;
        setStartingOffsetAndLineNumber(startingOffset, startingLine);
        this.isExported = isExported;
        fn = filename;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration#getTemplateParameters()
     */
    public Iterator getTemplateParameters()
    {
        return ( templateParameters != null ) ? templateParameters.iterator() : EmptyIterator.EMPTY_ITERATOR;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration#getOwnedDeclaration()
     */
    public IASTDeclaration getOwnedDeclaration()
    {
        return ownedDeclaration;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTTemplate#setOwnedDeclaration(org.eclipse.fdt.core.parser.ast.IASTDeclaration)
     */
    public void setOwnedDeclaration(IASTDeclaration declaration)
    {
        ownedDeclaration = declaration;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTTemplateDeclaration#isExported()
     */
    public boolean isExported()
    {
        return isExported;
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
            requestor.enterTemplateDeclaration(this);
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
            requestor.exitTemplateDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    
    /* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() throws ASTNotImplementedException {
		List decls = new ArrayList(1);
		decls.add( getOwnedDeclaration() );
		return decls.iterator();
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
