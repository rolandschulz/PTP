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

public class ASTMultOperandNode extends InteriorNode
{
    ASTMultOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTMultOperandNode(this);
    }

    public ASTLevel1ExprNode getLevel1Expr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_521)
            return (ASTLevel1ExprNode)getChild(0);
        else if (getProduction() == Production.MULT_OPERAND_522)
            return (ASTLevel1ExprNode)getChild(0);
        else
            return null;
    }

    public ASTPowerOpNode getPowerOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return (ASTPowerOpNode)getChild(1);
        else
            return null;
    }

    public ASTMultOperandNode getMultOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MULT_OPERAND_522)
            return (ASTMultOperandNode)getChild(2);
        else
            return null;
    }
}
