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

public class ASTSFExprListNode extends InteriorNode
{
    protected int count = -1;

    ASTSFExprListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTSFExprListNode
               && grandparent instanceof ASTSFExprListNode
               && ((ASTSFExprListNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTSFExprListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTSFExprListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTSFExprListNode recurseToIndex(int listIndex)
    {
        ASTSFExprListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTSFExprListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSFExprListNode(this);
    }

    public ASTExpressionNode getLb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_553)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_571)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_572)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_573)
            return (ASTExpressionNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_577)
            return (ASTExpressionNode)node.getChild(2);
        else if (node.getProduction() == Production.SFEXPR_LIST_578)
            return (ASTExpressionNode)node.getChild(2);
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return (ASTExpressionNode)node.getChild(2);
        else
            return null;
    }

    public boolean hasLb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_553)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_571)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_572)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_573)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_577)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_578)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return node.getChild(2) != null;
        else
            return false;
    }

    public ASTExpressionNode getUb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return (ASTExpressionNode)node.getChild(2);
        else if (node.getProduction() == Production.SFEXPR_LIST_554)
            return (ASTExpressionNode)node.getChild(1);
        else if (node.getProduction() == Production.SFEXPR_LIST_570)
            return (ASTExpressionNode)node.getChild(1);
        else if (node.getProduction() == Production.SFEXPR_LIST_573)
            return (ASTExpressionNode)node.getChild(2);
        else if (node.getProduction() == Production.SFEXPR_LIST_576)
            return (ASTExpressionNode)node.getChild(3);
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return (ASTExpressionNode)node.getChild(4);
        else
            return null;
    }

    public boolean hasUb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_554)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_570)
            return node.getChild(1) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_573)
            return node.getChild(2) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_576)
            return node.getChild(3) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return node.getChild(4) != null;
        else
            return false;
    }

    public ASTExpressionNode getStep(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return (ASTExpressionNode)node.getChild(4);
        else if (node.getProduction() == Production.SFEXPR_LIST_553)
            return (ASTExpressionNode)node.getChild(3);
        else if (node.getProduction() == Production.SFEXPR_LIST_554)
            return (ASTExpressionNode)node.getChild(3);
        else if (node.getProduction() == Production.SFEXPR_LIST_555)
            return (ASTExpressionNode)node.getChild(2);
        else
            return null;
    }

    public boolean hasStep(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_552)
            return node.getChild(4) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_553)
            return node.getChild(3) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_554)
            return node.getChild(3) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_555)
            return node.getChild(2) != null;
        else
            return false;
    }

    private ASTSFExprListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFEXPR_LIST_574)
            return (ASTSFExprListNode)getChild(0);
        else
            return null;
    }

    public ASTSectionSubscriptNode getSectionSubscript(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_574)
            return (ASTSectionSubscriptNode)node.getChild(2);
        else
            return null;
    }

    public ASTSFDummyArgNameListNode getSFDummyArgNameList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_575)
            return (ASTSFDummyArgNameListNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_576)
            return (ASTSFDummyArgNameListNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_577)
            return (ASTSFDummyArgNameListNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_578)
            return (ASTSFDummyArgNameListNode)node.getChild(0);
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return (ASTSFDummyArgNameListNode)node.getChild(0);
        else
            return null;
    }

    public boolean hasSFDummyArgNameList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFExprListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFEXPR_LIST_575)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_576)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_577)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_578)
            return node.getChild(0) != null;
        else if (node.getProduction() == Production.SFEXPR_LIST_579)
            return node.getChild(0) != null;
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.SFEXPR_LIST_552 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_552 && index == 3)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_553 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_553 && index == 2)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_554 && index == 0)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_554 && index == 2)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_555 && index == 0)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_555 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_569 && index == 0)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_570 && index == 0)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_572 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_573 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_574 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_575 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_575 && index == 2)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_576 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_576 && index == 2)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_577 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_578 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_578 && index == 3)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_579 && index == 1)
            return false;
        else if (getProduction() == Production.SFEXPR_LIST_579 && index == 3)
            return false;
        else
            return true;
    }
}
