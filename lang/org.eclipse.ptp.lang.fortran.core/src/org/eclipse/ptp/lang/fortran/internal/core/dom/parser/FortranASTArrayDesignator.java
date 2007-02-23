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
import org.eclipse.cdt.core.dom.ast.c.CASTVisitor;
import org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;

/**
 * @author jcamelon
 */
public class FortranASTArrayDesignator extends FortranASTNode implements
        ICASTArrayDesignator, IASTAmbiguityParent {

    private IASTExpression exp;

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator#getSubscriptExpression()
     */
    public IASTExpression getSubscriptExpression() {
        return exp;
    }

    /* (non-Javadoc)
     * @see org.eclipse.cdt.core.dom.ast.c.ICASTArrayDesignator#setSubscriptExpression(org.eclipse.cdt.core.dom.ast.IASTExpression)
     */
    public void setSubscriptExpression(IASTExpression value) {
        exp = value;
    }

    public boolean accept( ASTVisitor action ){
        if( action instanceof CASTVisitor && ((CASTVisitor)action).shouldVisitDesignators ){
		    switch( ((CASTVisitor)action).visit( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        if( exp != null ) if( !exp.accept( action ) ) return false;

        if( action instanceof CASTVisitor && ((CASTVisitor)action).shouldVisitDesignators ){
		    switch( ((CASTVisitor)action).leave( this ) ){
	            case ASTVisitor.PROCESS_ABORT : return false;
	            case ASTVisitor.PROCESS_SKIP  : return true;
	            default : break;
	        }
		}
        return true;
    }

    public void replace(IASTNode child, IASTNode other) {
        if( child == exp )
        {
            other.setPropertyInParent( child.getPropertyInParent() );
            other.setParent( child.getParent() );
            exp = (IASTExpression) other;
        }
    }
}
