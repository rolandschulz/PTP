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

public class ASTDataRefNode extends InteriorNode
{
    protected int count = -1;

    ASTDataRefNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTDataRefNode
               && grandparent instanceof ASTDataRefNode
               && ((ASTDataRefNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTDataRefNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDataRefNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDataRefNode recurseToIndex(int listIndex)
    {
        ASTDataRefNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDataRefNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDataRefNode(this);
    }

    private ASTDataRefNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATA_REF_429)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.DATA_REF_430)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.SFDATA_REF_433)
            return (ASTDataRefNode)getChild(0);
        else if (getProduction() == Production.SFDATA_REF_434)
            return (ASTDataRefNode)getChild(0);
        else
            return null;
    }

    public boolean hasDerivedTypeComponentName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_429)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.DATA_REF_430)
            return node.getChild(4) != null;
        else if (node.getProduction() == Production.SFDATA_REF_431)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.SFDATA_REF_433)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return node.getChild(4) != null;
        else
            return false;
    }

    public ASTSectionSubscriptListNode getPrimarySectionSubscriptList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_430)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else if (node.getProduction() == Production.SFDATA_REF_432)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else
            return null;
    }

    public boolean hasPrimarySectionSubscriptList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_430)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFDATA_REF_432)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return node.getChild(2) != null;
        else
            return false;
    }

    public Token getName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_428)
            return (Token)((ASTNameNode)node.getChild(0)).getName();
        else if (node.getProduction() == Production.SFDATA_REF_431)
            return (Token)((ASTNameNode)node.getChild(0)).getName();
        else if (node.getProduction() == Production.SFDATA_REF_432)
            return (Token)((ASTNameNode)node.getChild(0)).getName();
        else
            return null;
    }

    public Token getComponentName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATA_REF_429)
            return (Token)((ASTNameNode)node.getChild(2)).getName();
        else if (node.getProduction() == Production.DATA_REF_430)
            return (Token)((ASTNameNode)node.getChild(5)).getName();
        else if (node.getProduction() == Production.SFDATA_REF_431)
            return (Token)((ASTNameNode)node.getChild(2)).getName();
        else if (node.getProduction() == Production.SFDATA_REF_433)
            return (Token)((ASTNameNode)node.getChild(2)).getName();
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (Token)((ASTNameNode)node.getChild(5)).getName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.SFDATA_REF_432 && index == 1)
            return false;
        else if (getProduction() == Production.SFDATA_REF_432 && index == 3)
            return false;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 1)
            return false;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 3)
            return false;
        else if (getProduction() == Production.DATA_REF_430 && index == 1)
            return false;
        else if (getProduction() == Production.DATA_REF_430 && index == 3)
            return false;
        else if (getProduction() == Production.SFDATA_REF_432 && index == 1)
            return false;
        else if (getProduction() == Production.SFDATA_REF_432 && index == 3)
            return false;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 1)
            return false;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 3)
            return false;
        else if (getProduction() == Production.DATA_REF_430 && index == 1)
            return false;
        else if (getProduction() == Production.DATA_REF_430 && index == 3)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.SFDATA_REF_431 && index == 0)
            return true;
        else if (getProduction() == Production.SFDATA_REF_431 && index == 2)
            return true;
        else if (getProduction() == Production.SFDATA_REF_432 && index == 0)
            return true;
        else if (getProduction() == Production.SFDATA_REF_433 && index == 2)
            return true;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 5)
            return true;
        else if (getProduction() == Production.DATA_REF_428 && index == 0)
            return true;
        else if (getProduction() == Production.DATA_REF_429 && index == 2)
            return true;
        else if (getProduction() == Production.DATA_REF_430 && index == 5)
            return true;
        else if (getProduction() == Production.SFDATA_REF_431 && index == 0)
            return true;
        else if (getProduction() == Production.SFDATA_REF_431 && index == 2)
            return true;
        else if (getProduction() == Production.SFDATA_REF_432 && index == 0)
            return true;
        else if (getProduction() == Production.SFDATA_REF_433 && index == 2)
            return true;
        else if (getProduction() == Production.SFDATA_REF_434 && index == 5)
            return true;
        else if (getProduction() == Production.DATA_REF_428 && index == 0)
            return true;
        else if (getProduction() == Production.DATA_REF_429 && index == 2)
            return true;
        else if (getProduction() == Production.DATA_REF_430 && index == 5)
            return true;
        else
            return false;
    }
}
