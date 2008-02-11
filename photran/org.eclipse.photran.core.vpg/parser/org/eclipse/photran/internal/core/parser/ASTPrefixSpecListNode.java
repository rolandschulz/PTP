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

public class ASTPrefixSpecListNode extends InteriorNode implements  Iterable<ASTPrefixSpecNode>
{
    protected int count = -1;

    ASTPrefixSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTPrefixSpecListNode
               && grandparent instanceof ASTPrefixSpecListNode
               && ((ASTPrefixSpecListNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTPrefixSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPrefixSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPrefixSpecListNode recurseToIndex(int listIndex)
    {
        ASTPrefixSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPrefixSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPrefixSpecListNode(this);
    }

    public Iterator<ASTPrefixSpecNode> iterator()
    {
        final int listSize = size();
        
        ASTPrefixSpecListNode node = this;
        for (int depth = listSize-1, i = 0; i < depth; i++)
            node = (ASTPrefixSpecListNode)node.getRecursiveNode();

        final ASTPrefixSpecListNode baseNode = node;
        
        return new Iterator<ASTPrefixSpecNode>()
        {
            private ASTPrefixSpecListNode node = baseNode;
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < listSize;
            }

            public ASTPrefixSpecNode next()
            {
                int child = (index == 0 ? 0 : 1);
                ASTPrefixSpecNode result = (ASTPrefixSpecNode)node.getChild(child);
                node = (index == listSize-1 ? null : (ASTPrefixSpecListNode)node.parent);
                index++;
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ASTPrefixSpecNode getPrefixSpec(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPrefixSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.PREFIX_SPEC_LIST_987)
            return (ASTPrefixSpecNode)node.getChild(0);
        else if (node.getProduction() == Production.PREFIX_SPEC_LIST_988)
            return (ASTPrefixSpecNode)node.getChild(1);
        else
            return null;
    }

    private ASTPrefixSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.PREFIX_SPEC_LIST_988)
            return (ASTPrefixSpecListNode)getChild(0);
        else
            return null;
    }
}
