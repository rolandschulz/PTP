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

public class ASTOutputItemList1Node extends InteriorNode
{
    protected int count = -1;

    ASTOutputItemList1Node(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        // This is a recursive node in a list, so its logical parent node
        // is the parent of the first node in the list
    
        InteriorNode parent = super.getParent();
        InteriorNode grandparent = parent == null ? null : parent.getParent();
        InteriorNode logicalParent = parent;
        
        while (parent != null && grandparent != null
               && parent instanceof ASTOutputItemList1Node
               && grandparent instanceof ASTOutputItemList1Node
               && ((ASTOutputItemList1Node)grandparent).getRecursiveNode() == parent)
        {
            logicalParent = grandparent;
            parent = grandparent;
            grandparent = grandparent.getParent() == null ? null : grandparent.getParent();
        }
        
        InteriorNode logicalGrandparent = logicalParent.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (logicalGrandparent != null && logicalGrandparent.childIsPulledUp(logicalGrandparent.findChild(logicalParent)))
            return logicalParent.getASTParent();
        else 
            return logicalParent;
    }

    /**
     * @return the number of ASTOutputItemList1Node nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTOutputItemList1Node node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTOutputItemList1Node recurseToIndex(int listIndex)
    {
        ASTOutputItemList1Node node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTOutputItemList1Node)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTOutputItemList1Node(this);
    }

    public ASTExpressionNode getExpr1(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_811)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_812)
            return (ASTExpressionNode)node.getChild(0);
        else
            return null;
    }

    public boolean hasExpr1(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_811)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_812)
            return node.getChild(0) != null;
        else
            return false;
    }

    public ASTExpressionNode getExpr2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_811)
            return (ASTExpressionNode)node.getChild(2);
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_814)
            return (ASTExpressionNode)node.getChild(2);
        else
            return null;
    }

    public boolean hasExpr2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_811)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_814)
            return node.getChild(2) != null;
        else
            return false;
    }

    public ASTOutputImpliedDoNode getOutputImpliedDo(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_812)
            return (ASTOutputImpliedDoNode)node.getChild(2);
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_813)
            return (ASTOutputImpliedDoNode)node.getChild(0);
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_815)
            return (ASTOutputImpliedDoNode)node.getChild(2);
        else
            return null;
    }

    public boolean hasOutputImpliedDo(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTOutputItemList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_812)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_813)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.OUTPUT_ITEM_LIST_1_815)
            return node.getChild(2) != null;
        else
            return false;
    }

    private ASTOutputItemList1Node getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.OUTPUT_ITEM_LIST_1_814)
            return (ASTOutputItemList1Node)getChild(0);
        else if (getProduction() == Production.OUTPUT_ITEM_LIST_1_815)
            return (ASTOutputItemList1Node)getChild(0);
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.OUTPUT_ITEM_LIST_1_811 && index == 1)
            return false;
        else if (getProduction() == Production.OUTPUT_ITEM_LIST_1_812 && index == 1)
            return false;
        else if (getProduction() == Production.OUTPUT_ITEM_LIST_1_814 && index == 1)
            return false;
        else if (getProduction() == Production.OUTPUT_ITEM_LIST_1_815 && index == 1)
            return false;
        else
            return true;
    }
}
