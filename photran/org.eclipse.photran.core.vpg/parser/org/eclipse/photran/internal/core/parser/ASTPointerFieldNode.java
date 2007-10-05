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

public class ASTPointerFieldNode extends InteriorNode
{
    protected int count = -1;

    ASTPointerFieldNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTPointerFieldNode
               && grandparent instanceof ASTPointerFieldNode
               && ((ASTPointerFieldNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTPointerFieldNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPointerFieldNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPointerFieldNode recurseToIndex(int listIndex)
    {
        ASTPointerFieldNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPointerFieldNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPointerFieldNode(this);
    }

    public ASTNameNode getName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (ASTNameNode)node.getChild(0);
        else if (node.getProduction() == Production.POINTER_FIELD_471)
            return (ASTNameNode)node.getChild(0);
        else if (node.getProduction() == Production.POINTER_FIELD_472)
            return (ASTNameNode)node.getChild(0);
        else
            return null;
    }

    public Token getTLparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.POINTER_FIELD_471)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTSFExprListNode getSFExprList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (ASTSFExprListNode)node.getChild(2);
        else
            return null;
    }

    public Token getTRparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (Token)node.getChild(3);
        else if (node.getProduction() == Production.POINTER_FIELD_471)
            return (Token)node.getChild(3);
        else
            return null;
    }

    public Token getTPercent(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (Token)node.getChild(4);
        else if (node.getProduction() == Production.POINTER_FIELD_471)
            return (Token)node.getChild(4);
        else if (node.getProduction() == Production.POINTER_FIELD_472)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTNameNode getName2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_470)
            return (ASTNameNode)node.getChild(5);
        else if (node.getProduction() == Production.POINTER_FIELD_471)
            return (ASTNameNode)node.getChild(5);
        else if (node.getProduction() == Production.POINTER_FIELD_472)
            return (ASTNameNode)node.getChild(2);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_471)
            return (ASTSFDummyArgNameListNode)node.getChild(2);
        else
            return null;
    }

    private ASTPointerFieldNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_FIELD_473)
            return (ASTPointerFieldNode)getChild(0);
        else
            return null;
    }

    public ASTFieldSelectorNode getFieldSelector(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerFieldNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_FIELD_473)
            return (ASTFieldSelectorNode)node.getChild(1);
        else
            return null;
    }
}
