/*******************************************************************************
 * Copyright (c) 2007 University of Illinois at Urbana-Champaign and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     UIUC - Initial API and implementation
 *******************************************************************************/
package org.eclipse.photran.internal.core.analysis.types;

import java.util.HashMap;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.ASTAcValueList1Node;
import org.eclipse.photran.internal.core.parser.ASTAcValueListNode;
import org.eclipse.photran.internal.core.parser.ASTAddOperandNode;
import org.eclipse.photran.internal.core.parser.ASTAndOperandNode;
import org.eclipse.photran.internal.core.parser.ASTArrayConstructorNode;
import org.eclipse.photran.internal.core.parser.ASTDataRefNode;
import org.eclipse.photran.internal.core.parser.ASTEquivOperandNode;
import org.eclipse.photran.internal.core.parser.ASTExprNode;
import org.eclipse.photran.internal.core.parser.ASTFunctionReferenceNode;
import org.eclipse.photran.internal.core.parser.ASTLevel1ExprNode;
import org.eclipse.photran.internal.core.parser.ASTLevel2ExprNode;
import org.eclipse.photran.internal.core.parser.ASTLevel3ExprNode;
import org.eclipse.photran.internal.core.parser.ASTLevel4ExprNode;
import org.eclipse.photran.internal.core.parser.ASTLevel5ExprNode;
import org.eclipse.photran.internal.core.parser.ASTMultOperandNode;
import org.eclipse.photran.internal.core.parser.ASTNameNode;
import org.eclipse.photran.internal.core.parser.ASTOrOperandNode;
import org.eclipse.photran.internal.core.parser.ASTPrimaryNode;
import org.eclipse.photran.internal.core.parser.ASTUnsignedArithmeticConstantNode;
import org.eclipse.photran.internal.core.parser.ASTVisitor;
import org.eclipse.photran.internal.core.parser.Parser.InteriorNode;
import org.eclipse.photran.internal.core.parser.Parser.Nonterminal;

/**
 * *** INCOMPLETE ***
 * 
 * Determines the type of a (sub-)expression (an &lt;Expr&gt; node or any of its children) in a Fortran program.
 * 
 * It is assumed that the program type checks correctly.  For example, "a // b" will type as an integer
 * if a and b are integers, even though this is semantically incorrect.
 * 
 * Defined operators are ignored.
 * 
 * Ignores kinds and character lengths.
 * 
 * Ignores function parameters, array subscripts, and substring ranges.
 * 
 * TODO: The <code>Binder</code> assigns <code>Type.UNKNOWN</code> to INTERFACE blocks, function ENTRYs,
 * INTRINSICs, and EXTERNALs.  They are not handled correctly here.
 * 
 * @author Jeff Overbey
 */
public class TypeChecker
{
    private TypeChecker() {;}
    
    /**
     * Returns the type of the given node, or throws a <code>TypeError</code> if the node does not type-check.
     * The node must be in the subtree of an &lt;Expr&gt; node (or it may be an &lt;Expr&gt; node itself). 
     * @param node
     * @return Type (non-null)
     * @throws TypeError
     */
    public static Type getTypeOf(InteriorNode node) throws TypeError
    {
        if (!isSubexpression(node)) throw new IllegalArgumentException("INTERNAL ERROR: Can only type-check nodes under an <Expr> node.");

        try
        {
            TypeCheckingVisitor visitor = new TypeCheckingVisitor();
            node.visitBottomUpUsing(visitor);
            return visitor.getType(node);
        }
        catch (TypeErrorInVisitor e)
        {
            throw new TypeError(e.getMessage());
        }
    }
    
    private static boolean isSubexpression(InteriorNode node)
    {
        for (InteriorNode n = node; n != null; n = n.getParent())
            if (n.getNonterminal() == Nonterminal.EXPR)
                return true;
        return false;
    }

    private static class TypeCheckingVisitor extends ASTVisitor
    {
        private TypeCheckingVisitor() {;}
        
        private HashMap<InteriorNode, Type> types = new HashMap<InteriorNode, Type>();
        
        private Type getType(InteriorNode node)
        {
            if (types.containsKey(node))
                return types.get(node);
            else
                throw new Error("INTERNAL ERROR: Type not set for " + node.getNonterminal());
        }
        
        private void setType(InteriorNode node, Type type)
        {
            types.put(node, type);
        }
        
        private Type getCommonType(InteriorNode node1, InteriorNode node2)
        {
            return getCommonType(getType(node1), getType(node2));
        }
        
        private Type getCommonType(Type type1, Type type2)
        {
            if (type1 != null && type2 != null && type1.getCommonType(type2) == null)
                throw new TypeErrorInVisitor(type1 + " and " + type2 + " are incompatible types");
            else
                return type1.getCommonType(type2);
        }
        
        private void propagate(InteriorNode to, InteriorNode from)
        {
            if (from != null) setType(to, getType(from));
        }
        
        private void propagate(InteriorNode to, InteriorNode from1, InteriorNode from2)
        {
            if (from1 != null && from2 != null)
                setType(to, getCommonType(from1, from2));
            else if (from1 != null)
                propagate(to, from1);
            else if (from2 != null)
                propagate(to, from2);
        }
        
        private Type lookupType(Token identifier)
        {
        	// TODO:
        	throw new IllegalStateException();
//            Type result = identifier.getDefinition().getType();
//            if (result == Type.UNKNOWN)
//                throw new TypeErrorInVisitor("The type of \"" + identifier.getText() + "\" is not known.");
//            else if (result == Type.VOID)
//                throw new TypeErrorInVisitor("\"" + identifier.getText() + "\" cannot appear in an expression: it does not of a numeric type.");
//            else
//                return result;
        }
        
        private Type lookupType(ASTNameNode variableNameNode)
        {
            return lookupType(variableNameNode.getTIdent());
        }

        private Type lookupDerivedTypeComponentType(Type derivedType, ASTDataRefNode dataRefNode)
        {
            // TODO
            return null;
        }
        
        private Type lookupReturnType(ASTFunctionReferenceNode fnRefNode)
        {
            // TODO
            return null;
        }
    
        // --VISITOR METHODS-------------------------------------------------
    
        @Override
        public void visitASTExprNode(ASTExprNode node)
        {
            propagate(node, node.getExpr(), node.getLevel5Expr());
        }
    
        @Override
        public void visitASTLevel5ExprNode(ASTLevel5ExprNode node)
        {
            propagate(node, node.getLevel5Expr(), node.getEquivOperand());
        }
        
        @Override
        public void visitASTEquivOperandNode(ASTEquivOperandNode node)
        {
            propagate(node, node.getEquivOperand(), node.getOrOperand());
        }
        
        @Override
        public void visitASTOrOperandNode(ASTOrOperandNode node)
        {
            propagate(node, node.getOrOperand(), node.getAndOperand());
        }
        
        @Override
        public void visitASTAndOperandNode(ASTAndOperandNode node)
        {
            propagate(node, node.getLevel4Expr());
        }
        
        @Override
        public void visitASTLevel4ExprNode(ASTLevel4ExprNode node)
        {
            propagate(node, node.getLevel3Expr(), node.getLevel3Expr2());
        }
        
        @Override
        public void visitASTLevel3ExprNode(ASTLevel3ExprNode node)
        {
            propagate(node, node.getLevel3Expr(), node.getLevel2Expr());
        }
        
        @Override
        public void visitASTLevel2ExprNode(ASTLevel2ExprNode node)
        {
            propagate(node, node.getLevel2Expr(), node.getAddOperand());
        }
        
        @Override
        public void visitASTAddOperandNode(ASTAddOperandNode node)
        {
            propagate(node, node.getAddOperand(), node.getMultOperand());
        }
        
        @Override
        public void visitASTMultOperandNode(ASTMultOperandNode node)
        {
            propagate(node, node.getLevel1Expr(), node.getMultOperand());
        }
        
        @Override
        public void visitASTLevel1ExprNode(ASTLevel1ExprNode node)
        {
            propagate(node, node.getPrimary());
        }
        
        @Override
        public void visitASTPrimaryNode(ASTPrimaryNode n)
        {
            if (n.getLogicalConstant() != null)
                setType(n, Type.LOGICAL);
            else if (n.getTScon() != null)
                setType(n, Type.CHARACTER);
            else if (n.getUnsignedArithmeticConstant() != null)
                propagate(n, n.getUnsignedArithmeticConstant());
            else if (n.getArrayConstructor() != null)
                propagate(n, n.getArrayConstructor());
            else if (n.getName() != null && n.getTPercent() != null)
                setType(n, lookupDerivedTypeComponentType(lookupType(n.getName()), n.getDataRef()));
            else if (n.getName() != null)
                setType(n, lookupType(n.getName()));
            else if (n.getFunctionReference() != null && n.getTPercent() != null)
                setType(n, lookupDerivedTypeComponentType(lookupReturnType(n.getFunctionReference()), n.getDataRef()));
            else if (n.getFunctionReference() != null)
                setType(n, lookupReturnType(n.getFunctionReference()));
            else if (n.getExpr() != null)
                propagate(n, n.getExpr());
            else if (n.getSubstrConst() != null)
                setType(n, Type.CHARACTER);
        }
    
        @Override
        public void visitASTUnsignedArithmeticConstantNode(ASTUnsignedArithmeticConstantNode node)
        {
            if (node.getTIcon() != null)
                setType(node, Type.INTEGER);
            else if (node.getTRcon() != null)
                setType(node, Type.REAL);
            else if (node.getTDcon() != null)
                setType(node, Type.DOUBLEPRECISION);
            else if (node.getComplexConst() != null)
                setType(node, Type.COMPLEX);
        }
        
        @Override
        public void visitASTArrayConstructorNode(ASTArrayConstructorNode node)
        {
            propagate(node, node.getAcValueList());
        }
        
        @Override
        public void visitASTAcValueListNode(ASTAcValueListNode node)
        {
            propagate(node, node.getExpr(), node.getAcValueList1());
        }
        
        @Override
        public void visitASTAcValueList1Node(ASTAcValueList1Node node)
        {
            Type type = null;
            for (int i = 0; i < node.size(); i++)
                type = getCommonType(type, getCommonType(node.getExpr(i), node.getExpr2(i)));
            setType(node, type);
        }
    }

    /**
     * Since <code>Exception</code>s can't be thrown in our visitor methods, this is
     * thrown instead, caught by the <code>getTypeOf</code> method, and turned into
     * a <code>TypeError</code>, which is the real exception that should be thrown.
     * 
     * @author joverbey
     */
    private static class TypeErrorInVisitor extends Error
    {
        private static final long serialVersionUID = 1L;

        public TypeErrorInVisitor(String msg)
        {
            super(msg);
        }
    }
}