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

public class ASTEquivOperandNode extends InteriorNode
{
    ASTEquivOperandNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTEquivOperandNode(this);
    }

    public ASTOrOperandNode getOrOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EQUIV_OPERAND_546)
            return (ASTOrOperandNode)getChild(0);
        else if (getProduction() == Production.EQUIV_OPERAND_547)
            return (ASTOrOperandNode)getChild(2);
        else
            return null;
    }

    public ASTEquivOperandNode getEquivOperand()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EQUIV_OPERAND_547)
            return (ASTEquivOperandNode)getChild(0);
        else
            return null;
    }

    public ASTOrOpNode getOrOp()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EQUIV_OPERAND_547)
            return (ASTOrOpNode)getChild(1);
        else
            return null;
    }
}
