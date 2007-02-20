/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Rational Software - Initial API and implementation
 * Yuan Zhang / Beth Tibbitts (IBM Research)
 *******************************************************************************/
package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.antlr.runtime.Token;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTLiteralExpression;
import org.eclipse.cdt.core.dom.ast.IType;

import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction.KindParam;
import org.eclipse.ptp.lang.fortran.core.parser.IFortranParserAction.LiteralConstant;

/**
 * @author crasmussen
 */
public class FortranASTLiteralExpression extends FortranASTNode implements
        IASTLiteralExpression {

    private int kind;
    private String value = ""; //$NON-NLS-1$
    
	/* Fortran specific variables */

    private Token token;
    private Token ktToken;
    private int kindType = -1;
    private KindParam kindTokenType = KindParam.none;
    
    FortranASTLiteralExpression(LiteralConstant fKind, Token cToken, KindParam kindTokenType, Token ktToken) {
    	super();
    	
    	this.token = cToken;
    	this.ktToken = ktToken;
    	this.kindTokenType = kindTokenType;
    	if (kindTokenType == KindParam.literal) {
    		kindType = Integer.parseInt(ktToken.getText());
    	} else {
    		// TODO - get kind type from identifier
    	}
    	
		setValue(cToken.getText());
		setOffsetAndLength(FortranParserAction.offset(cToken), FortranParserAction.length(cToken));
    	
    	switch (fKind) {
		case int_literal_constant:
			setKind(IASTLiteralExpression.lk_integer_constant);		break;
		case real_literal_constant:
			setKind(IASTLiteralExpression.lk_float_constant);		break;
		case complex_literal_constant:
			setKind(IASTLiteralExpression.lk_last + 1);				break;
		case logical_literal_constant:
			setKind(IASTLiteralExpression.lk_last + 2);				break;
		case char_literal_constant:
			setKind(IASTLiteralExpression.lk_string_literal);		break;
		case boz_literal_constant:
			setKind(IASTLiteralExpression.lk_last + 3);				break;
		}
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int value) {
        kind = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /* Fortran specific methods */

    public int getKindTypeParameter() {
    	return kindType;
	}
    
    public String toString() {
        return value;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitExpressions ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( action.shouldVisitExpressions ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}      
        return true;
    }
    
    public IType getExpressionType() {
    	return FortranVisitor.getExpressionType(this);
    }
    
}
