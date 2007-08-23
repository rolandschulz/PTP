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

public class ASTModuleBodyNode extends InteriorNode
{
    protected int count = -1;

    ASTModuleBodyNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTModuleBodyNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTModuleBodyNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTModuleBodyNode recurseToIndex(int listIndex)
    {
        ASTModuleBodyNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTModuleBodyNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTModuleBodyNode(this);
    }

    public ASTSpecificationPartConstructNode getSpecificationPartConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTModuleBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.MODULE_BODY_28)
            return (ASTSpecificationPartConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.MODULE_BODY_30)
            return (ASTSpecificationPartConstructNode)node.getChild(1);
        else
            return null;
    }

    public ASTModuleSubprogramPartConstructNode getModuleSubprogramPartConstruct(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTModuleBodyNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.MODULE_BODY_29)
            return (ASTModuleSubprogramPartConstructNode)node.getChild(0);
        else if (node.getProduction() == Production.MODULE_BODY_31)
            return (ASTModuleSubprogramPartConstructNode)node.getChild(1);
        else
            return null;
    }

    private ASTModuleBodyNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.MODULE_BODY_30)
            return (ASTModuleBodyNode)getChild(0);
        else if (getProduction() == Production.MODULE_BODY_31)
            return (ASTModuleBodyNode)getChild(0);
        else
            return null;
    }
}
