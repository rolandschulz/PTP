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

public class ASTOutputItemListNode extends InteriorNode
{
    ASTOutputItemListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
        visitor.visitASTOutputItemListNode(this);
    }

    public ASTExpressionNode getSingleExpr()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_809)
            return (ASTExpressionNode)getChild(0);
        else
            return null;
    }

    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return (int)((ASTOutputItemList1Node)getChild(0)).size();
        else
            return 0;
    }

    public ASTExpressionNode getExpr1(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return (ASTExpressionNode)((ASTOutputItemList1Node)getChild(0)).getExpr1(listIndex1);
        else
            return null;
    }

    public boolean hasExpr1(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return ((ASTOutputItemList1Node)getChild(0)).hasExpr1(listIndex1);
        else
            return false;
    }

    public ASTExpressionNode getExpr2(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return (ASTExpressionNode)((ASTOutputItemList1Node)getChild(0)).getExpr2(listIndex1);
        else
            return null;
    }

    public boolean hasExpr2(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return ((ASTOutputItemList1Node)getChild(0)).hasExpr2(listIndex1);
        else
            return false;
    }

    public ASTOutputImpliedDoNode getOutputImpliedDo(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return (ASTOutputImpliedDoNode)((ASTOutputItemList1Node)getChild(0)).getOutputImpliedDo(listIndex1);
        else
            return null;
    }

    public boolean hasOutputImpliedDo(int listIndex1)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_810)
            return ((ASTOutputItemList1Node)getChild(0)).hasOutputImpliedDo(listIndex1);
        else
            return false;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.OUTPUT_ITEM_LIST_810 && index == 0)
            return true;
        else
            return false;
    }
}
