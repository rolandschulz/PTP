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

public class ASTExecutableProgramNode extends ScopingNode implements Iterable<ASTProgramUnitNode>
{
    protected int count = -1;

    ASTExecutableProgramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTExecutableProgramNode
               && grandparent instanceof ASTExecutableProgramNode
               && ((ASTExecutableProgramNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTExecutableProgramNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTExecutableProgramNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTExecutableProgramNode recurseToIndex(int listIndex)
    {
        ASTExecutableProgramNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTExecutableProgramNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTExecutableProgramNode(this);
    }

    public Iterator<ASTProgramUnitNode> iterator()
    {
        final int listSize = size();
        
        ASTExecutableProgramNode node = this;
        for (int depth = listSize-1, i = 0; i < depth; i++)
            node = (ASTExecutableProgramNode)node.getRecursiveNode();

        final ASTExecutableProgramNode baseNode = node;
        
        return new Iterator<ASTProgramUnitNode>()
        {
            private ASTExecutableProgramNode node = baseNode;
            private int index = 0;
            
            public boolean hasNext()
            {
                return index < listSize;
            }

            public ASTProgramUnitNode next()
            {
                ASTProgramUnitNode result = (ASTProgramUnitNode)node.getChild(1);
                node = (ASTExecutableProgramNode)node.parent;
                index++;
                return result;
            }

            public void remove()
            {
                throw new UnsupportedOperationException();
            }
        };
    }

    public ASTProgramUnitNode getProgramUnit(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTExecutableProgramNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.EXECUTABLE_PROGRAM_1)
            return (ASTProgramUnitNode)node.getChild(0);
        else if (node.getProduction() == Production.EXECUTABLE_PROGRAM_2)
            return (ASTProgramUnitNode)node.getChild(1);
        else
            return null;
    }

    private ASTExecutableProgramNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.EXECUTABLE_PROGRAM_2)
            return (ASTExecutableProgramNode)getChild(0);
        else
            return null;
    }
}
