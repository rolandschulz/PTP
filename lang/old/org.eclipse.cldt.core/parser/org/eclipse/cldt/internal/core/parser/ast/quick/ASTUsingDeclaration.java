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

import java.util.Iterator;

import org.eclipse.cldt.core.parser.ISourceElementRequestor;
import org.eclipse.cldt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.cldt.core.parser.ast.IASTScope;
import org.eclipse.cldt.core.parser.ast.IASTUsingDeclaration;

/**
 * @author jcamelon
 *
 */
public class ASTUsingDeclaration
	extends ASTDeclaration
	implements IASTUsingDeclaration {

	private final boolean isTypename; 
	private final char []  mappingName; 
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

	
	public ASTUsingDeclaration( IASTScope scope, boolean isTypeName, char[] mappingName, int startingOffset, int startingLine, int endingOffset, int endingLine, char[] filename, int nStart, int nEnd, int nLine )
	{
		super( scope );
		isTypename = isTypeName;
		this.mappingName = mappingName;
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
		fn = filename;
		setNameOffset( nStart );
		setNameEndOffsetAndLineNumber( nEnd, nLine );
	}
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#isTypename()
	 */
	public boolean isTypename() {
		return isTypename;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#usingTypeName()
	 */
	public String usingTypeName() {
		return String.valueOf(mappingName);
	}
	public char[] usingTypeNameCharArray(){
	    return mappingName;
	}

	/* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#accept(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {
		try
        {
            requestor.acceptUsingDeclaration(this);
        }
        catch (Exception e)
        {
            /* do nothing */
        } 
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#enter(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void enterScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ISourceElementCallbackDelegate#exit(org.eclipse.cdt.core.parser.ISourceElementRequestor)
     */
    public void exitScope(ISourceElementRequestor requestor)
    {
    }
    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.parser.ast.IASTUsingDeclaration#getUsingType()
     */
    public Iterator getUsingTypes() throws ASTNotImplementedException
    {
    	throw new ASTNotImplementedException();
    }
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset;
	private int nameStartOffset;
	private int nameEndOffset;
	private int nameLine;
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
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getName()
	 */
	public String getName() {
		return new String( mappingName );
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameCharArray()
	 */
	public char[] getNameCharArray() {
		return mappingName;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
	 */
	public int getNameOffset() {
		return nameStartOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
	 */
	public void setNameOffset(int o) {
		nameStartOffset = o;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
	 */
	public int getNameEndOffset() {
		return nameEndOffset;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffsetAndLineNumber(int, int)
	 */
	public void setNameEndOffsetAndLineNumber(int offset, int lineNumber) {
		nameEndOffset = offset;
		nameLine = lineNumber;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
	 */
	public int getNameLineNumber() {
		return nameLine;
	}

}
