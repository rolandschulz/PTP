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

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTIfStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class FortranASTIfStatement extends FortranASTNode implements IASTIfStatement, IASTAmbiguityParent {

    private IASTExpression condition;
    private IASTStatement thenClause;
    private IASTStatement elseClause;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getCondition()
     */
    public IASTExpression getConditionExpression() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setConditionExpression(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getThenClause()
     */
    public IASTStatement getThenClause() {
        return thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setThenClause(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setThenClause(IASTStatement thenClause) {
        this.thenClause = thenClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#getElseClause()
     */
    public IASTStatement getElseClause() {
        return elseClause;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTIfStatement#setElseClause(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setElseClause(IASTStatement elseClause) {
        this.elseClause = elseClause;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( thenClause != null ) if( !thenClause.accept( action ) ) return false;
        if( elseClause != null ) if( !elseClause.accept( action ) ) return false;

        if( action.shouldVisitStatements ){
		    switch( action.leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( thenClause == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            thenClause = (IASTStatement) other;
        }
        if( elseClause == child )
        {
            other.setParent( child.getParent() );
            other.setPropertyInParent( child.getPropertyInParent() );
            elseClause = (IASTStatement) other;            
        }
        if( child == condition )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition  = (IASTExpression) other;
        }
    }
}
