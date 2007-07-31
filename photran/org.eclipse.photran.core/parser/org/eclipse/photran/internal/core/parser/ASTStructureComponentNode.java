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

public class ASTStructureComponentNode extends InteriorNode
{
    protected int count = -1;

    ASTStructureComponentNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }

    /**
     * @return the number of ASTStructureComponentNode nodes in this list
     */
    public int size()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods, including size(), cannot be called on the nodes of a CST after it has been modified");
        
        if (count >= 0) return count;
        
        count = 0;
        ASTStructureComponentNode node = this;
        do
        {
            count++;
            node = node.getRecursiveNode();
        }
        while (node != null);
        
        return count;
    }
    
    ASTStructureComponentNode recurseToIndex(int listIndex)
    {
        ASTStructureComponentNode node = this;
        for (int depth = size()-listIndex-1, i = 0; i < depth; i++)
        {
            if (node == null) throw new IllegalArgumentException("Index " + listIndex + " out of bounds (size: " + size() + ")");
            node = (ASTStructureComponentNode)node.getRecursiveNode();
        }
        return node;
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTStructureComponentNode(this);
    }

    public ASTVariableNameNode getVariableName(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTStructureComponentNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.STRUCTURE_COMPONENT_435)
            return (ASTVariableNameNode)node.getChild(0);
        else
            return null;
    }

    public ASTFieldSelectorNode getFieldSelector(int listIndex)
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        ASTStructureComponentNode node = recurseToIndex(listIndex);
        if (node.getProduction() == Production.STRUCTURE_COMPONENT_435)
            return (ASTFieldSelectorNode)node.getChild(1);
        else if (node.getProduction() == Production.STRUCTURE_COMPONENT_436)
            return (ASTFieldSelectorNode)node.getChild(1);
        else
            return null;
    }

    private ASTStructureComponentNode getRecursiveNode()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.STRUCTURE_COMPONENT_436)
            return (ASTStructureComponentNode)getChild(0);
        else
            return null;
    }
}
