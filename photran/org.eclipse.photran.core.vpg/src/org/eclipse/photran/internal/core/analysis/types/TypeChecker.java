/*******************************************************************************
 * Copyright (c) 2008 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.types;

import org.eclipse.photran.internal.core.parser.ASTBinaryExprNode;
import org.eclipse.photran.internal.core.parser.ASTComplexConstNode;
import org.eclipse.photran.internal.core.parser.ASTDblConstNode;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.ASTLogicalConstNode;
import org.eclipse.photran.internal.core.parser.ASTNestedExprNode;
import org.eclipse.photran.internal.core.parser.ASTOperatorNode;
import org.eclipse.photran.internal.core.parser.ASTRealConstNode;
import org.eclipse.photran.internal.core.parser.ASTStringConstNode;
import org.eclipse.photran.internal.core.parser.ASTUnaryExprNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.IExpr;

/**
 * A type checker for Fortran programs.
 * <p>
 * <b>This is incomplete. It can't type check expressions containing function calls, for
 * example.</b>
 * 
 * @author Stoyan Gaydarov
 */
public class TypeChecker
{
    private TypeChecker() {}
    
    public static Type getTypeOf(IExpr expression)
    {
        return new TypingVisitor().getTypeOf(expression);
    }
    
    protected static class TypingVisitor extends ASTVisitor
    {
        protected Type topType;
        
        public Type getTypeOf(IExpr expression)
        {
            expression.accept(this);
            
            return topType == null ? Type.UNKNOWN : topType;
        }
            
        @Override
        public void visitASTBinaryExprNode(ASTBinaryExprNode node)
        {
            Type lhsType = getTypeOf(node.getLhsExpr());
            Type rhsType = getTypeOf(node.getRhsExpr());
            ASTOperatorNode op = node.getOperator();
            
            //there is no binary operation we can do
            //unknown and anything = unknown
            //error and anything = error
            if( lhsType.equals(Type.UNKNOWN) || lhsType.equals(Type.TYPE_ERROR) ||
                rhsType.equals(Type.UNKNOWN) || rhsType.equals(Type.TYPE_ERROR) ||
                rhsType.equals(Type.VOID)    || lhsType.equals(Type.VOID)         )
            {
                topType = Type.TYPE_ERROR;
                return;
            }
            
            // This is for + - * / and **
            if(op.hasPowerOp() || op.hasDivideOp() || 
               op.hasMinusOp() || op.hasPlusOp()   || op.hasTimesOp())
            {
                topType = checkNumericOperations(lhsType, rhsType);
            }
            //This is for an AND, OR, EQV, and NEQV of logical operators
            else if(op.hasAndOp() || op.hasOrOp() || op.hasEqvOp() || op.hasNeqvOp())
            {
                topType = checkLogicalComparisons(lhsType, rhsType);
            }
            else if(op.hasGeOp() || op.hasGtOp()  ||
                    op.hasLeOp() || op.hasLtOp()  ||  //SlashOp?
                    op.hasEqOp() || op.hasNeOp()  || op.hasEqEqOp() )
            {
                topType = checkNumericComparison(lhsType, rhsType, op);
            }
            else if( op.hasConcatOp())
            {
                if(lhsType.equals(Type.CHARACTER) && lhsType.equals(rhsType))
                    topType = Type.CHARACTER;
                else
                    topType = Type.UNKNOWN;
            }
            else //TODO anything else
                topType = Type.UNKNOWN;
        }

        /**
         * @param node
         * @param lhsType
         * @param rhsType
         */
        private Type checkNumericOperations(Type lhsType, Type rhsType)
        {
            //if the left or right side are any of these then there is a type error
            if ( lhsType.equals(Type.LOGICAL) || lhsType.equals(Type.CHARACTER) ||
                 rhsType.equals(Type.LOGICAL) || rhsType.equals(Type.CHARACTER)   )
            {
                return Type.TYPE_ERROR;
            }
            
            //When the types are the same then the result is that type
            if(lhsType.equals(rhsType))
            {
                return lhsType;
            }
            
            //When the first is an Integer
            if (lhsType.equals(Type.INTEGER) )
            {
                return checkIntegerOperations(rhsType);
            }
            //When the first is a Real
            else if ( lhsType.equals(Type.REAL) )
            {
                return checkRealOperations(rhsType);
            }
            //When the first is a Complex
            else if ( lhsType.equals(Type.COMPLEX) )
            {
                return checkComplexComparison(rhsType);
            }
            //When the first is a Double Precision
            else if ( lhsType.equals(Type.DOUBLEPRECISION ))
            {
                return checkDoubleComparison(rhsType);
            }
            else
                return Type.UNKNOWN;
        }

        /**
         * @param node
         * @param rhsType
         */
        private Type checkComplexComparison(Type rhsType)
        {
            if(rhsType.equals(Type.INTEGER) || rhsType.equals(Type.REAL) || rhsType.equals(Type.DOUBLEPRECISION))
                return Type.COMPLEX;
            else
                return Type.UNKNOWN;
        }

        /**
         * @param node
         * @param rhsType
         */
        private Type checkDoubleComparison(Type rhsType)
        {
            if (rhsType.equals(Type.INTEGER) || rhsType.equals(Type.REAL))
                return Type.DOUBLEPRECISION;
            else if(rhsType.equals(Type.COMPLEX))
                return Type.COMPLEX;
            else
                return Type.UNKNOWN;
        }

        /**
         * @param node
         * @param rhsType
         */
        private Type checkRealOperations(Type rhsType)
        {
            if (rhsType.equals(Type.INTEGER))
                return Type.REAL;
            else if(rhsType.equals(Type.DOUBLEPRECISION))
                return Type.DOUBLEPRECISION;
            else if(rhsType.equals(Type.COMPLEX))
                return Type.COMPLEX;
            else
                return Type.UNKNOWN;
        }

        /**
         * @param node
         * @param rhsType
         */
        private Type checkIntegerOperations(Type rhsType)
        {
            if(rhsType.equals(Type.REAL))
                return Type.REAL;
            else if(rhsType.equals(Type.COMPLEX))
                return Type.COMPLEX;
            else if(rhsType.equals(Type.DOUBLEPRECISION))
                return Type.DOUBLEPRECISION;
            else
                return Type.UNKNOWN;
        }

        /**
         * @param node
         * @param lhsType
         * @param rhsType
         */
        private Type checkLogicalComparisons(Type lhsType, Type rhsType)
        {
            if( lhsType.equals(Type.LOGICAL) && rhsType.equals(Type.LOGICAL))
                return Type.LOGICAL;
            else
                return Type.TYPE_ERROR;
        }

        /**
         * @param node
         * @param lhsType
         * @param rhsType
         */
        private Type checkNumericComparison(Type lhsType, Type rhsType, ASTOperatorNode op)
        {
            //you can't do this kind of comparison on logical and character types
            if ( lhsType.equals(Type.LOGICAL) || lhsType.equals(Type.CHARACTER) || 
                 rhsType.equals(Type.LOGICAL) || rhsType.equals(Type.CHARACTER)   )
            {
                //System.out.println(lhsType + "\t::\t" + rhsType);
                return Type.TYPE_ERROR;
            }
            //Complex only has == and /=
            else if(lhsType.equals(Type.COMPLEX) || rhsType.equals(Type.COMPLEX))
            {
                if(op.hasEqEqOp() || op.hasEqOp() || op.hasNeOp()) //remove EqEq
                    return Type.LOGICAL;
                else
                    return Type.TYPE_ERROR;
            }
            //All the remaining cases are ok
            else
                return Type.LOGICAL;
        }

        @Override
        public void visitASTIntConstNode(ASTIntConstNode node)
        {
            topType = Type.INTEGER;
        }
        
        @Override
        public void visitASTDblConstNode(ASTDblConstNode node)
        {
            topType = Type.DOUBLEPRECISION;
        }

        @Override
        public void visitASTLogicalConstNode(ASTLogicalConstNode node)
        {
            topType = Type.LOGICAL;
        }

        @Override
        public void visitASTNestedExprNode(ASTNestedExprNode node)
        {
            topType = getTypeOf(node.getExpr());
        }

        @Override
        public void visitASTRealConstNode(ASTRealConstNode node)
        {
            topType = Type.REAL;
        }

        @Override
        public void visitASTStringConstNode(ASTStringConstNode node)
        {
            topType = Type.CHARACTER;
        }
        
        @Override
        public void visitASTComplexConstNode(ASTComplexConstNode node)
        {
            topType = Type.COMPLEX;
        }

        @Override
        public void visitASTUnaryExprNode(ASTUnaryExprNode node)
        {
            Type operand = getTypeOf(node.getOperand());
            ASTOperatorNode operator = node.getOperator();

            //character has no unary operations
            if(operand.equals(Type.CHARACTER))
                topType = Type.TYPE_ERROR;
            // .NOT. only applies to logical
            else if(operator != null && operator.hasNotOp() )
            {
                if(operand.equals(Type.LOGICAL))
                    topType = Type.LOGICAL;
                else
                    topType = Type.TYPE_ERROR;
            }
            //logical has no other unary operations
            else if(operand.equals(Type.LOGICAL))
                topType = Type.TYPE_ERROR;
            //the type of the expression is the same as the operand
            else
                topType = operand;
        }
    }
}
