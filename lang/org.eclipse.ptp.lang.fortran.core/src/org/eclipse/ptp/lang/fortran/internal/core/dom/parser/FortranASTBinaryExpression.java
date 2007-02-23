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
import org.eclipse.cdt.core.dom.ast.IASTBinaryExpression;
import org.eclipse.cdt.core.dom.ast.IASTExpression;
import org.eclipse.cdt.core.dom.ast.IASTNode;
import org.eclipse.cdt.core.dom.ast.IType;
import org.eclipse.cdt.internal.core.dom.parser.IASTAmbiguityParent;
import org.eclipse.cdt.internal.core.dom.parser.c.CVisitor;

/**
 * @author crasmussen
 */
public class FortranASTBinaryExpression extends FortranASTNode implements
    IASTBinaryExpression, IASTAmbiguityParent {

        private int op;
        private IASTExpression operand1;
        private IASTExpression operand2;

        public int getOperator() {
            return op;
        }

        public IASTExpression getOperand1() {
            return operand1;
        }

        public IASTExpression getOperand2() {
            return operand2;
        }

        public void setOperator(int op) {
            this.op = op;
        }

        public void setOperand1(IASTExpression expression) {
            operand1 = expression;   
        }

        public void setOperand2(IASTExpression expression) {
            operand2 = expression;
        }

        public boolean accept( ASTVisitor action ){
            if( action.shouldVisitExpressions ){
    		    switch( action.visit( this ) ){
    	            case ASTVisitor.PROCESS_ABORT : return false;
    	            case ASTVisitor.PROCESS_SKIP  : return true;
    	            default : break;
    	        }
    		}
            
            if( operand1 != null ) if( !operand1.accept( action ) ) return false;
            if( operand2 != null ) if( !operand2.accept( action ) ) return false;
            
            if(action.shouldVisitExpressions ){
            	switch( action.leave( this ) ){
            		case ASTVisitor.PROCESS_ABORT : return false;
            		case ASTVisitor.PROCESS_SKIP  : return true;
            		default : break;
            	}
            }
            return true;
        }
        
        public void replace(IASTNode child, IASTNode other) {
            if( child == operand1 )
            {
                other.setPropertyInParent( child.getPropertyInParent() );
                other.setParent( child.getParent() );
                operand1 = (IASTExpression) other;
            }
            if( child == operand2)
            {
                other.setPropertyInParent( child.getPropertyInParent() );
                other.setParent( child.getParent() );
                operand2 = (IASTExpression) other;
            }
        }
        
        public IType getExpressionType() {
        	return CVisitor.getExpressionType(this);
        }
        
    }
