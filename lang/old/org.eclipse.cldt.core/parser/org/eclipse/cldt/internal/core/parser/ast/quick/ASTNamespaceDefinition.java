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
package org.eclipse.cldt.internal.core.parser.ast.quick;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cldt.core.parser.ISourceElementRequestor;
import org.eclipse.cldt.core.parser.ast.IASTDeclaration;
import org.eclipse.cldt.core.parser.ast.IASTNamespaceDefinition;
import org.eclipse.cldt.core.parser.ast.IASTScope;
import org.eclipse.cldt.internal.core.parser.ast.ASTQualifiedNamedElement;

/**
 * @author jcamelon
 *
 */
public class ASTNamespaceDefinition extends ASTDeclaration implements IASTNamespaceDefinition, IASTQScope {

	private final char[] name;
	private final ASTQualifiedNamedElement qualifiedNameElement; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

	public ASTNamespaceDefinition( IASTScope scope, char[] name, int startOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, char[] filename )
	{
		super( scope );
		qualifiedNameElement = new ASTQualifiedNamedElement( scope, name );
		this.name = name; 
		setStartingOffsetAndLineNumber(startOffset, startingLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber(nameEndOffset, nameEndOffset);
		fn = filename;
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		return String.valueOf(name);
	}

	private List declarations = new ArrayList(); 
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTScope#getDeclarations()
	 */
	public Iterator getDeclarations() {
		return declarations.iterator();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.internal.core.parser.ast.quick.IASTQScope#addDeclaration(org.eclipse.cdt.core.parser.ast.IASTDeclaration)
	 */
	public void addDeclaration(IASTDeclaration declaration) {
		declarations.add( declaration );
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTQualifiedNameElement#getFullyQualifiedName()
     */
    public String[] getFullyQualifiedName()
    {
        return qualifiedNameElement.getFullyQualifiedName();
    }
	public char[][] getFullyQualifiedNameCharArrays(){
	    return qualifiedNameElement.getFullyQualifiedNameCharArrays();
	}
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.enterNamespaceDefinition(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    	try
        {
            requestor.exitNamespaceDefinition(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        }
    }
    
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public final int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public final int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public final int getNameLineNumber() {
    	return nameLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public final void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public final void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public final int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public final int getEndingOffset()
    {
        return endingOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public final int getNameOffset()
    {
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public final void setNameOffset(int o)
    {
        nameStartOffset = o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
     */
    public final int getNameEndOffset()
    {
        return nameEndOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
     */
    public final void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    	nameLineNumber = lineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
     */
    public char[] getNameCharArray() {
        return name;
    }
}
