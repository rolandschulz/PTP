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

public class ASTDerivedTypeBodyNode extends InteriorNode implements  Iterable<ASTDerivedTypeBodyConstructNode>
{
    protected int count = -1;

    ASTDerivedTypeBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTDerivedTypeBodyNode
               && grandparent instanceof ASTDerivedTypeBodyNode
               && ((ASTDerivedTypeBodyNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTDerivedTypeBodyNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDerivedTypeBodyNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDerivedTypeBodyNode recurseToIndex(int listIndex)
    {
        ASTDerivedTypeBodyNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDerivedTypeBodyNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDerivedTypeBodyNode(this);
    }

    public Iterator<ASTDerivedTypeBodyConstructNode> iterator()
    {
        final int listSize = size();
        
        ASTDerivedTypeBodyNode node = this;
        for (int depth = listSize-1, i = 0; i < depth; i++)
            node = (ASTDerivedTypeBodyNode)node.getRecursiveNode();

        final ASTDerivedTypeBodyNode baseNode = node;
        
        return new Iterator<ASTDerivedTypeBodyConstructNode>()
        {
            private ASTDerivedTypeBodyNode node = baseNode;
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < listSize;
            }

            public ASTDerivedTypeBodyConstructNode next()
            {
                int child = (index == 0 ? 0 : 1);
                ASTDerivedTypeBodyConstructNode result = (ASTDerivedTypeBodyConstructNode)node.getChild(child);
                node = (index == listSize-1 ? null : (ASTDerivedTypeBodyNode)node.parent);
                index++;
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ASTDerivedTypeBodyConstructNode getDerivedTypeBodyConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDerivedTypeBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DERIVED_TYPE_BODY_180)
            return (ASTDerivedTypeBodyConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.DERIVED_TYPE_BODY_181)
            return (ASTDerivedTypeBodyConstructNode)node.getChild(1);
        else
            return null;
    }

    private ASTDerivedTypeBodyNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DERIVED_TYPE_BODY_181)
            return (ASTDerivedTypeBodyNode)getChild(0);
        else
            return null;
    }
}
