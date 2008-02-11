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

import org.eclipse.photran.internal.core.lexer.*;                   import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;

import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTOutputImpliedDoNode extends InteriorNode
{
    ASTOutputImpliedDoNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTOutputImpliedDoNode(this);
    }

    public ASTExpressionNode getExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818)
            return (ASTExpressionNode)getChild(1);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return (ASTExpressionNode)getChild(1);
        else
            return null;
    }

    public ASTExpressionNode getLb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820)
            return (ASTExpressionNode)getChild(5);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return (ASTExpressionNode)getChild(5);
        else
            return null;
    }

    public ASTExpressionNode getUb()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820)
            return (ASTExpressionNode)getChild(7);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return (ASTExpressionNode)getChild(7);
        else
            return null;
    }

    public ASTExpressionNode getStep()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return (ASTExpressionNode)getChild(9);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return (ASTExpressionNode)getChild(9);
        else
            return null;
    }

    public boolean hasStep()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return getChild(9) != null;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return getChild(9) != null;
        else
            return false;
    }

    public ASTOutputItemList1Node getOutputItemList1()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_820)
            return (ASTOutputItemList1Node)getChild(1);
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return (ASTOutputItemList1Node)getChild(1);
        else
            return null;
    }

    public Token getImpliedDoVariable()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818)
            return (Token)((ASTImpliedDoVariableNode)getChild(3)).getImpliedDoVariable();
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819)
            return (Token)((ASTImpliedDoVariableNode)getChild(3)).getImpliedDoVariable();
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820)
            return (Token)((ASTImpliedDoVariableNode)getChild(3)).getImpliedDoVariable();
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821)
            return (Token)((ASTImpliedDoVariableNode)getChild(3)).getImpliedDoVariable();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 0)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 2)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 4)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 6)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 8)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 0)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 2)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 4)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 6)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 8)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 10)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 0)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 2)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 4)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 6)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 8)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 0)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 2)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 4)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 6)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 8)
            return false;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 10)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.OUTPUT_IMPLIED_DO_818 && index == 3)
            return true;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_819 && index == 3)
            return true;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_820 && index == 3)
            return true;
        else if (getProduction() == Production.OUTPUT_IMPLIED_DO_821 && index == 3)
            return true;
        else
            return false;
    }
}
