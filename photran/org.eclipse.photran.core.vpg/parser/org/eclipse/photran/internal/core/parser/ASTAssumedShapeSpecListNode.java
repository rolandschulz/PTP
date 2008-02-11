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

public class ASTAssumedShapeSpecListNode extends InteriorNode
{
    protected int count = -1;

    ASTAssumedShapeSpecListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTAssumedShapeSpecListNode
               && grandparent instanceof ASTAssumedShapeSpecListNode
               && ((ASTAssumedShapeSpecListNode)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTAssumedShapeSpecListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAssumedShapeSpecListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAssumedShapeSpecListNode recurseToIndex(int listIndex)
    {
        ASTAssumedShapeSpecListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAssumedShapeSpecListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAssumedShapeSpecListNode(this);
    }

    public ASTDeferredShapeSpecListNode getDeferredShapeSpecList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return (ASTDeferredShapeSpecListNode)node.getChild(0);
        else
            return null;
    }

    public boolean hasDeferredShapeSpecList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return node.getChild(0) != null;
        else
            return false;
    }

    private ASTAssumedShapeSpecListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (ASTAssumedShapeSpecListNode)getChild(0);
        else
            return null;
    }

    public ASTExpressionNode getLb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_295)
            return (ASTExpressionNode)((ASTLowerBoundNode)node.getChild(0)).getLb();
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return (ASTExpressionNode)((ASTLowerBoundNode)node.getChild(2)).getLb();
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return (ASTExpressionNode)((ASTAssumedShapeSpecNode)node.getChild(2)).getLb();
        else
            return null;
    }

    public boolean hasLb(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAssumedShapeSpecListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_295)
            return ((ASTLowerBoundNode)node.getChild(0)).hasLb();
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296)
            return ((ASTLowerBoundNode)node.getChild(2)).hasLb();
        else if (node.getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297)
            return ((ASTAssumedShapeSpecNode)node.getChild(2)).hasLb();
        else
            return false;
    }

    @Override protected boolean shouldVisitChild(int index)
    {
        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_295 && index == 1)
            return false;
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296 && index == 1)
            return false;
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296 && index == 3)
            return false;
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297 && index == 1)
            return false;
        else
            return true;
    }

    @Override protected boolean childIsPulledUp(int index)
    {
        if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_295 && index == 0)
            return true;
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_296 && index == 2)
            return true;
        else if (getProduction() == Production.ASSUMED_SHAPE_SPEC_LIST_297 && index == 2)
            return true;
        else
            return false;
    }
}
