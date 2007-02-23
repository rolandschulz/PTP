/*******************************************************************************
 * Copyright (c) 2005, 2006 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy. The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.ptp.lang.fortran.internal.core.dom.parser;

import org.eclipse.cdt.core.dom.ast.ASTVisitor;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IASTStatement;
import org.eclipse.cdt.core.dom.ast.IASTSwitchStatement;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author crasmussen
 */
public class FortranASTCaseConstruct extends FortranASTNode implements
        IASTSwitchStatement, IASTAmbiguityParent {

    private IASTExpression controller;
    private IASTStatement body;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#getController()
     */
    public IASTExpression getControllerExpression() {
        return controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#setController(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setControllerExpression(IASTExpression controller) {
        this.controller = controller;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#getBody()
     */
    public IASTStatement getBody() {
        return body;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.IASTSwitchStatement#setBody(org.eclipse.cdt.core.dom.ast.IASTStatement)
     */
    public void setBody(IASTStatement body) {
        this.body = body;
    }

    public boolean accept( ASTVisitor action ){
        if( action.shouldVisitStatements ){
		    switch( action.visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( controller != null ) if( !controller.accept( action ) ) return false;
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
        if( child == controller )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            controller  = (IASTExpression) other;
        }
    }
}
