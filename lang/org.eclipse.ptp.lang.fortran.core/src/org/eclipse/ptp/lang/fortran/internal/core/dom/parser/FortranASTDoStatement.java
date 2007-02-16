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
import org.eclipse.cdt.core.dom.ast.IASTForStatement;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IScope;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author crasmussen
 * 
 * TODO - rename to FortranASTDoStatement?
 */
public class FortranASTDoStatement extends FortranASTNode implements IASTForStatement, IASTAmbiguityParent {
    private IScope scope = null;
    
    private IASTExpression condition;
    private IASTExpression iterationExpression;
    private IASTStatement body, init;


    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getCondition()
     */
    public IASTExpression getConditionExpression() {
        return condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setCondition(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setConditionExpression(IASTExpression condition) {
        this.condition = condition;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getIterationExpression()
     */
    public IASTExpression getIterationExpression() {
        return iterationExpression;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setIterationExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setIterationExpression(IASTExpression iterator) {
        this.iterationExpression = iterator;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement statement) {
        body = statement;

    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTForStatement#getScope()
     */
    public IScope getScope() {
        if( scope == null )
            scope = new FortranScope( this );
        return scope;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( init != null ) if( !init.accept( action ) ) return false;
        if( condition != null ) if( !condition.accept( action ) ) return false;
        if( iterationExpression != null ) if( !iterationExpression.accept( action ) ) return false;
        if( body != null ) if( !body.accept( action ) ) return false;

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
        if( body == child )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            body = (IASTStatement) other;
        }
        if( child == init )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            init  = (IASTStatement) other;
        }
        if( child == iterationExpression)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            iterationExpression = (IASTExpression) other;
        }
        if( child == condition)
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            condition = (IASTExpression) other;
        }
        
    }

    public IASTStatement getInitializerStatement() {
        return init;
    }

    public void setInitializerStatement(IASTStatement statement) {
        init = statement;
    }


}
