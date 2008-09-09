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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.photran.internal.core.parser.ASTBinaryExprNode;
import org.eclipse.photran.internal.core.parser.ASTIntConstNode;
import org.eclipse.photran.internal.core.parser.IExpr;
import org.eclipse.photran.internal.core.parser.Parser.ASTVisitor;

/**
 * Fortran type checker
 * 
 * @author Stoyan Gaydarov
 * @author Jeff Overbey
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
        protected Map<IExpr, Type> types = new HashMap<IExpr, Type>();
        
        public Type getTypeOf(IExpr expression)
        {
            if (!types.containsKey(expression))
                expression.accept(this);
            
            Type result = types.get(expression);
            return result == null ? Type.UNKNOWN : result;
        }
            
        @Override
        public void visitASTBinaryExprNode(ASTBinaryExprNode node)
        {
            Type lhsType = getTypeOf(node.getLhsExpr());
            Type rhsType = getTypeOf(node.getRhsExpr());
            
            if (lhsType.equals(Type.INTEGER) && rhsType.equals(Type.INTEGER))
                types.put(node, Type.INTEGER);
            else
                types.put(node, Type.TYPE_ERROR);
        }

        @Override
        public void visitASTIntConstNode(ASTIntConstNode node)
        {
            types.put(node, Type.INTEGER);
        }
    }
}
