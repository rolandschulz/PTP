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

public class ASTNamelistGroupsNode extends InteriorNode
{
    protected int count = -1;

    ASTNamelistGroupsNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTNamelistGroupsNode
               && grandparent instanceof ASTNamelistGroupsNode
               && ((ASTNamelistGroupsNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTNamelistGroupsNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTNamelistGroupsNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTNamelistGroupsNode recurseToIndex(int listIndex)
    {
        ASTNamelistGroupsNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTNamelistGroupsNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTNamelistGroupsNode(this);
    }

    private ASTNamelistGroupsNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.NAMELIST_GROUPS_397)
            return (ASTNamelistGroupsNode)getChild(0);
        else if (getProduction() == Production.NAMELIST_GROUPS_398)
            return (ASTNamelistGroupsNode)getChild(0);
        else if (getProduction() == Production.NAMELIST_GROUPS_399)
            return (ASTNamelistGroupsNode)getChild(0);
        else
            return null;
    }

    public Token getNamelistGroupName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_396)
            return (Token)((ASTNamelistGroupNameNode)node.getChild(1)).getNamelistGroupName();
        else if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (Token)((ASTNamelistGroupNameNode)node.getChild(2)).getNamelistGroupName();
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (Token)((ASTNamelistGroupNameNode)node.getChild(3)).getNamelistGroupName();
        else
            return null;
    }

    public Token getNamelistGroupObjectVariableName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTNamelistGroupsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.NAMELIST_GROUPS_396)
            return (Token)((ASTNamelistGroupObjectNode)node.getChild(3)).getVariableName();
        else if (node.getProduction() == Production.NAMELIST_GROUPS_397)
            return (Token)((ASTNamelistGroupObjectNode)node.getChild(4)).getVariableName();
        else if (node.getProduction() == Production.NAMELIST_GROUPS_398)
            return (Token)((ASTNamelistGroupObjectNode)node.getChild(5)).getVariableName();
        else if (node.getProduction() == Production.NAMELIST_GROUPS_399)
            return (Token)((ASTNamelistGroupObjectNode)node.getChild(2)).getVariableName();
        else
            return null;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.NAMELIST_GROUPS_396 && index == 0)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_396 && index == 2)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_397 && index == 1)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_397 && index == 3)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_398 && index == 1)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_398 && index == 2)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_398 && index == 4)
            return false;
        else if (getProduction() == Production.NAMELIST_GROUPS_399 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.NAMELIST_GROUPS_396 && index == 1)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_396 && index == 3)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_397 && index == 2)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_397 && index == 4)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_398 && index == 3)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_398 && index == 5)
            return true;
        else if (getProduction() == Production.NAMELIST_GROUPS_399 && index == 2)
            return true;
        else
            return false;
    }
}
