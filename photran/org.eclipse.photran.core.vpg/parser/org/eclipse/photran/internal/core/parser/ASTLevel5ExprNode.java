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
package org.eclipse.photran.internal.core.parser;

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTLevel5ExprNode extends InteriorNode
{
    ASTLevel5ExprNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTLevel5ExprNode(this);
    }

    public ASTEquivOperandNode getEquivOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_5_EXPR_548)
            return (ASTEquivOperandNode)getChild(0);
        else if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (ASTEquivOperandNode)getChild(2);
        else
            return null;
    }

    public ASTLevel5ExprNode getLevel5Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (ASTLevel5ExprNode)getChild(0);
        else
            return null;
    }

    public ASTEquivOpNode getEquivOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.LEVEL_5_EXPR_549)
            return (ASTEquivOpNode)getChild(1);
        else
            return null;
    }
}
