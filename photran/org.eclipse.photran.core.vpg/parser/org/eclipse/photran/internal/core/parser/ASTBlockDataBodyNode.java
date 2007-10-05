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

public class ASTBlockDataBodyNode extends InteriorNode implements Iterable<ASTBlockDataBodyConstructNode>
{
    protected int count = -1;

    ASTBlockDataBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTBlockDataBodyNode
               && grandparent instanceof ASTBlockDataBodyNode
               && ((ASTBlockDataBodyNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTBlockDataBodyNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTBlockDataBodyNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTBlockDataBodyNode recurseToIndex(int listIndex)
    {
        ASTBlockDataBodyNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTBlockDataBodyNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTBlockDataBodyNode(this);
    }

    public Iterator<ASTBlockDataBodyConstructNode> iterator()
    {
        final int listSize = size();
        
        ASTBlockDataBodyNode node = this;
        for (int depth = listSize-1, i = 0; i < depth; i++)
            node = (ASTBlockDataBodyNode)node.getRecursiveNode();

        final ASTBlockDataBodyNode baseNode = node;
        
        return new Iterator<ASTBlockDataBodyConstructNode>()
        {
            private ASTBlockDataBodyNode node = baseNode;
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < listSize;
            }

            public ASTBlockDataBodyConstructNode next()
            {
                ASTBlockDataBodyConstructNode result = (ASTBlockDataBodyConstructNode)node.getChild(1);
                node = (ASTBlockDataBodyNode)node.parent;
                index++;
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ASTBlockDataBodyConstructNode getBlockDataBodyConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTBlockDataBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.BLOCK_DATA_BODY_34)
            return (ASTBlockDataBodyConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.BLOCK_DATA_BODY_35)
            return (ASTBlockDataBodyConstructNode)node.getChild(1);
        else
            return null;
    }

    private ASTBlockDataBodyNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_BODY_35)
            return (ASTBlockDataBodyNode)getChild(0);
        else
            return null;
    }
}
