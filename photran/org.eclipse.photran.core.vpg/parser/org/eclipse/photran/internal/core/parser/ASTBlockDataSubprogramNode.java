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

import java.util.List;

import org.eclipse.photran.internal.core.analysis.binding.ScopingNode;
import org.eclipse.photran.internal.core.parser.Parser.CSTNode;
import org.eclipse.photran.internal.core.parser.Parser.Production;

public class ASTBlockDataSubprogramNode extends ScopingNode
{
    ASTBlockDataSubprogramNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
    
    @Override protected void visitThisNodeUsing(ASTVisitor visitor)
    {
        visitor.visitASTBlockDataSubprogramNode(this);
    }

    public ASTBlockDataStmtNode getBlockDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTBlockDataStmtNode)getChild(0);
        else if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_33)
            return (ASTBlockDataStmtNode)getChild(0);
        else
            return null;
    }

    public ASTBlockDataBodyNode getBlockDataBody()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTBlockDataBodyNode)getChild(1);
        else
            return null;
    }

    public ASTEndBlockDataStmtNode getEndBlockDataStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_32)
            return (ASTEndBlockDataStmtNode)getChild(2);
        else if (getProduction() == Production.BLOCK_DATA_SUBPROGRAM_33)
            return (ASTEndBlockDataStmtNode)getChild(1);
        else
            return null;
    }
}
