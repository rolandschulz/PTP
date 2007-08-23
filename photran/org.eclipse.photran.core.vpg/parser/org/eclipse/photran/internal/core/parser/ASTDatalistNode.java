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

public class ASTDatalistNode extends InteriorNode
{
    protected int count = -1;

    ASTDatalistNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTDatalistNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTDatalistNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTDatalistNode recurseToIndex(int listIndex)
    {
        ASTDatalistNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTDatalistNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTDatalistNode(this);
    }

    public ASTDataStmtSetNode getDataStmtSet(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDatalistNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATALIST_369)
            return (ASTDataStmtSetNode)node.getChild(0);
        else if (node.getProduction() == Production.DATALIST_370)
            return (ASTDataStmtSetNode)node.getChild(1);
        else if (node.getProduction() == Production.DATALIST_371)
            return (ASTDataStmtSetNode)node.getChild(2);
        else
            return null;
    }

    private ASTDatalistNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.DATALIST_370)
            return (ASTDatalistNode)getChild(0);
        else if (getProduction() == Production.DATALIST_371)
            return (ASTDatalistNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTDatalistNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.DATALIST_371)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
