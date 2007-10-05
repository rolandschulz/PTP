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

public class ASTAcValueList1Node extends InteriorNode
{
    protected int count = -1;

    ASTAcValueList1Node(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
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
               && parent instanceof ASTAcValueList1Node
               && grandparent instanceof ASTAcValueList1Node
               && ((ASTAcValueList1Node)grandparent).getRecursiveNode() == parent)
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
     * @return the number of ASTAcValueList1Node nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTAcValueList1Node node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTAcValueList1Node recurseToIndex(int listIndex)
    {
        ASTAcValueList1Node node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTAcValueList1Node)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTAcValueList1Node(this);
    }

    public ASTExprNode getExpr(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (ASTExprNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (ASTExprNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_223)
            return (ASTExprNode)node.getChild(2);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_223)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_224)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTExprNode getExpr2(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_220)
            return (ASTExprNode)node.getChild(2);
        else
            return null;
    }

    public ASTAcImpliedDoNode getAcImpliedDo(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTAcValueList1Node node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.AC_VALUE_LIST_1_221)
            return (ASTAcImpliedDoNode)node.getChild(2);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_222)
            return (ASTAcImpliedDoNode)node.getChild(0);
        else if (node.getProduction() == Production.AC_VALUE_LIST_1_224)
            return (ASTAcImpliedDoNode)node.getChild(2);
        else
            return null;
    }

    private ASTAcValueList1Node getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.AC_VALUE_LIST_1_223)
            return (ASTAcValueList1Node)getChild(0);
        else if (getProduction() == Production.AC_VALUE_LIST_1_224)
            return (ASTAcValueList1Node)getChild(0);
        else
            return null;
    }
}
