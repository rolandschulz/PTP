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

public class ASTPointerStmtObjectListNode extends InteriorNode
{
    protected int count = -1;

    ASTPointerStmtObjectListNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTPointerStmtObjectListNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTPointerStmtObjectListNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTPointerStmtObjectListNode recurseToIndex(int listIndex)
    {
        ASTPointerStmtObjectListNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTPointerStmtObjectListNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTPointerStmtObjectListNode(this);
    }

    public ASTPointerStmtObjectNode getPointerStmtObject(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerStmtObjectListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_STMT_OBJECT_LIST_352)
            return (ASTPointerStmtObjectNode)node.getChild(0);
        else if (node.getProduction() == Production.POINTER_STMT_OBJECT_LIST_353)
            return (ASTPointerStmtObjectNode)node.getChild(2);
        else
            return null;
    }

    private ASTPointerStmtObjectListNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.POINTER_STMT_OBJECT_LIST_353)
            return (ASTPointerStmtObjectListNode)getChild(0);
        else
            return null;
    }

    public Token getTComma(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTPointerStmtObjectListNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.POINTER_STMT_OBJECT_LIST_353)
            return (Token)node.getChild(1);
        else
            return null;
    }
}
