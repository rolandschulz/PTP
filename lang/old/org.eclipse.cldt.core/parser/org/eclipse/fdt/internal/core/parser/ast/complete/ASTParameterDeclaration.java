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

import java.util.Iterator;
import java.util.List;

import org.eclipse.fdt.core.parser.ISourceElementRequestor;
import org.eclipse.fdt.core.parser.ast.ASTNotImplementedException;
import org.eclipse.fdt.core.parser.ast.ASTPointerOperator;
import org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTFunction;
import org.eclipse.fdt.core.parser.ast.IASTInitializerClause;
import org.eclipse.fdt.core.parser.ast.IASTParameterDeclaration;
import org.eclipse.fdt.core.parser.ast.IASTTypeSpecifier;
import org.eclipse.fdt.internal.core.parser.ast.ASTAbstractDeclaration;
import org.eclipse.fdt.internal.core.parser.pst.ISymbol;

/**
 * @author jcamelon
 *
 */
public class ASTParameterDeclaration extends ASTSymbol implements IASTParameterDeclaration
{
	private final ASTAbstractDeclaration abstractDeclaration;
    private final char[] parameterName; 
	private final IASTInitializerClause initializerClause;
    private final char [] fn;
	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getFilename()
	 */
	public char[] getFilename() {
		return fn;
	}

    /**
     * @param isConst
     * @param typeSpecifier
     * @param pointerOperators
     * @param arrayModifiers
     * @param parameterName
     * @param initializerClause
     * @param filename
     */
    public ASTParameterDeclaration(ISymbol symbol, boolean isConst, boolean isVolatile, IASTTypeSpecifier typeSpecifier, List pointerOperators, List arrayModifiers, List parameters, ASTPointerOperator pointerOp, char[] parameterName, IASTInitializerClause initializerClause, int startingOffset, int startingLine, int nameOffset, int nameEndOffset, int nameLine, int endingOffset, int endingLine, char[] filename )
    {
    	super( symbol );
    	abstractDeclaration = new ASTAbstractDeclaration( isConst, isVolatile, typeSpecifier, pointerOperators, arrayModifiers, parameters, pointerOp );
		this.parameterName = parameterName;
		this.initializerClause = initializerClause;
		setStartingOffsetAndLineNumber(startingOffset, startingLine);
		setEndingOffsetAndLineNumber(endingOffset, endingLine);
		setNameOffset(nameOffset);
		setNameEndOffsetAndLineNumber( nameEndOffset, nameLine );
		fn = filename;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTParameterDeclaration#getName()
     */
    public String getName()
    {
        return String.valueOf(parameterName);
    }
    public char[] getNameCharArray(){
        return parameterName;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTParameterDeclaration#getDefaultValue()
     */
    public IASTInitializerClause getDefaultValue()
    {
        return initializerClause;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#isConst()
     */
    public boolean isConst()
    {
        return abstractDeclaration.isConst();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#isVolatile()
     */
    public boolean isVolatile()
    {
        return abstractDeclaration.isVolatile();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#getPointerOperators()
     */
    public Iterator getPointerOperators()
    {
        return abstractDeclaration.getPointerOperators();
    }
    public List getPointerOperatorsList(){
        return abstractDeclaration.getPointerOperatorsList();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#getArrayModifiers()
     */
    public Iterator getArrayModifiers()
    {
        return abstractDeclaration.getArrayModifiers();
    }
    public List getArrayModifiersList(){
        return abstractDeclaration.getArrayModifiersList();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#getParameters()
     */
    public Iterator getParameters()
    {
        return abstractDeclaration.getParameters();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTAbstractDeclaration#getPointerToFunctionOperator()
     */
    public ASTPointerOperator getPointerToFunctionOperator()
    {
        return abstractDeclaration.getPointerToFunctionOperator();
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTTypeSpecifierOwner#getTypeSpecifier()
     */
    public IASTTypeSpecifier getTypeSpecifier()
    {
        return abstractDeclaration.getTypeSpecifier();
    }
    
    public IASTAbstractDeclaration getAbstractDeclaration(){
    	return abstractDeclaration;
    }
    
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ISourceElementCallbackDelegate#acceptElement(org.eclipse.fdt.core.parser.ISourceElementRequestor)
     */
    public void acceptElement(ISourceElementRequestor requestor)
    {   
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
    /* (non-Javadoc)
	 * @see org.eclipse.fdt.core.parser.ast.IASTParameterDeclaration#getOwnerFunctionDeclaration()
	 */
	public IASTFunction getOwnerFunctionDeclaration() throws ASTNotImplementedException {
		return (IASTFunction) getSymbol().getContainingSymbol().getASTExtension().getPrimaryDeclaration();
	}
	
	private int startingLineNumber, startingOffset, endingLineNumber, endingOffset, nameStartOffset, nameEndOffset, nameLineNumber;
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getStartingLine()
     */
    public int getStartingLine() {
    	return startingLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getEndingLine()
     */
    public int getEndingLine() {
    	return endingLineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement#getNameLineNumber()
     */
    public int getNameLineNumber() {
    	return nameLineNumber;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#setStartingOffset(int)
     */
    public void setStartingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	startingOffset = offset;
    	startingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#setEndingOffset(int)
     */
    public void setEndingOffsetAndLineNumber(int offset, int lineNumber)
    {
    	endingOffset = offset;
    	endingLineNumber = lineNumber;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getStartingOffset()
     */
    public int getStartingOffset()
    {
        return startingOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableElement#getEndingOffset()
     */
    public int getEndingOffset()
    {
        return endingOffset;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement#getNameOffset()
     */
    public int getNameOffset()
    {
    	return nameStartOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement#setNameOffset(int)
     */
    public void setNameOffset(int o)
    {
        nameStartOffset = o;
    }

    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement#getNameEndOffset()
     */
    public int getNameEndOffset()
    {
        return nameEndOffset;
    }
    /* (non-Javadoc)
     * @see org.eclipse.fdt.core.parser.ast.IASTOffsetableNamedElement#setNameEndOffset(int)
     */
    public void setNameEndOffsetAndLineNumber(int offset, int lineNumber)
    {
    	nameEndOffset = offset;
    	nameLineNumber = lineNumber;
    }
}
