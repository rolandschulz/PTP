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

import org.eclipse.photran.internal.core.lexer.Token;
import org.eclipse.photran.internal.core.parser.Parser.*;
import java.util.List;

public class ASTSFDataRefNode extends InteriorNode
{
    protected int count = -1;

    ASTSFDataRefNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTSFDataRefNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTSFDataRefNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTSFDataRefNode recurseToIndex(int listIndex)
    {
        ASTSFDataRefNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTSFDataRefNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTSFDataRefNode(this);
    }

    public ASTNameNode getVarName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_431)
            return (ASTNameNode)node.getChild(0);
        else if (node.getProduction() == Production.SFDATA_REF_432)
            return (ASTNameNode)node.getChild(0);
        else
            return null;
    }

    public Token getTPercent(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_431)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.SFDATA_REF_433)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (Token)node.getChild(4);
        else
            return null;
    }

    public ASTNameNode getComponentName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_431)
            return (ASTNameNode)node.getChild(2);
        else if (node.getProduction() == Production.SFDATA_REF_433)
            return (ASTNameNode)node.getChild(2);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (ASTNameNode)node.getChild(5);
        else
            return null;
    }

    public Token getTLparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_432)
            return (Token)node.getChild(1);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (Token)node.getChild(1);
        else
            return null;
    }

    public ASTSectionSubscriptListNode getSectionSubscriptList(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_432)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (ASTSectionSubscriptListNode)node.getChild(2);
        else
            return null;
    }

    public Token getTRparen(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTSFDataRefNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.SFDATA_REF_432)
            return (Token)node.getChild(3);
        else if (node.getProduction() == Production.SFDATA_REF_434)
            return (Token)node.getChild(3);
        else
            return null;
    }

    private ASTSFDataRefNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.SFDATA_REF_433)
            return (ASTSFDataRefNode)getChild(0);
        else if (getProduction() == Production.SFDATA_REF_434)
            return (ASTSFDataRefNode)getChild(0);
        else
            return null;
    }
}
