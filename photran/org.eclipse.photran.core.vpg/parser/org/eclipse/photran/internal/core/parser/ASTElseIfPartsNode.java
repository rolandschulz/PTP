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
import java.util.Iterator;
import java.util.List;

public class ASTElseIfPartsNode extends InteriorNode implements  Iterable<ASTElseIfConstructNode>
{
    protected int count = -1;

    ASTElseIfPartsNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTElseIfPartsNode
               && grandparent instanceof ASTElseIfPartsNode
               && ((ASTElseIfPartsNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTElseIfPartsNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTElseIfPartsNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTElseIfPartsNode recurseToIndex(int listIndex)
    {
        ASTElseIfPartsNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTElseIfPartsNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTElseIfPartsNode(this);
    }

    public Iterator<ASTElseIfConstructNode> iterator()
    {
        final int listSize = size();
        
        ASTElseIfPartsNode node = this;
        for (int depth = listSize-1, i = 0; i < depth; i++)
            node = (ASTElseIfPartsNode)node.getRecursiveNode();

        final ASTElseIfPartsNode baseNode = node;
        
        return new Iterator<ASTElseIfConstructNode>()
        {
            private ASTElseIfPartsNode node = baseNode;
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < listSize;
            }

            public ASTElseIfConstructNode next()
            {
                int child = (index == 0 ? 0 : 1);
                ASTElseIfConstructNode result = (ASTElseIfConstructNode)node.getChild(child);
                node = (index == listSize-1 ? null : (ASTElseIfPartsNode)node.parent);
                index++;
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ASTElseIfConstructNode getElseIfConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTElseIfPartsNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ELSE_IF_PARTS_658)
            return (ASTElseIfConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.ELSE_IF_PARTS_659)
            return (ASTElseIfConstructNode)node.getChild(1);
        else
            return null;
    }

    private ASTElseIfPartsNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ELSE_IF_PARTS_659)
            return (ASTElseIfPartsNode)getChild(0);
        else
            return null;
    }
}
