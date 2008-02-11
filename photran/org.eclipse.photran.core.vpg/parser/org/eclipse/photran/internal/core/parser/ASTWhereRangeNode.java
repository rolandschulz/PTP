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

class ASTWhereRangeNode extends InteriorNode
{
    ASTWhereRangeNode(Production production, List<CSTNode> childNodes, List<CSTNode> discardedSymbols)
    {
         super(production);
         
         for (Object o : childNodes)
             addChild((CSTNode)o);
         constructionFinished();
    }
        
    @Override public InteriorNode getASTParent()
    {
        InteriorNode actualParent = super.getParent();
        
        // If a node has been pulled up in an ACST, its physical parent in
        // the CST is not its logical parent in the ACST
        if (actualParent != null && actualParent.childIsPulledUp(actualParent.findChild(this)))
            return actualParent.getParent();
        else 
            return actualParent;
    }

    public ASTEndWhereStmtNode getEndWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_601)
            return (ASTEndWhereStmtNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_602)
            return (ASTEndWhereStmtNode)getChild(1);
        else
            return null;
    }

    public boolean hasEndWhereStmt()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_601)
            return getChild(0) != null;
        else if (getProduction() == Production.WHERE_RANGE_602)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTWhereBodyConstructBlockNode getWhereBodyConstructBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_602)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_604)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_606)
            return (ASTWhereBodyConstructBlockNode)getChild(0);
        else
            return null;
    }

    public boolean hasWhereBodyConstructBlock()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_602)
            return getChild(0) != null;
        else if (getProduction() == Production.WHERE_RANGE_604)
            return getChild(0) != null;
        else if (getProduction() == Production.WHERE_RANGE_606)
            return getChild(0) != null;
        else
            return false;
    }

    public ASTMaskedElseWhereConstructNode getMaskedElseWhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_603)
            return (ASTMaskedElseWhereConstructNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_604)
            return (ASTMaskedElseWhereConstructNode)getChild(1);
        else
            return null;
    }

    public boolean hasMaskedElseWhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_603)
            return getChild(0) != null;
        else if (getProduction() == Production.WHERE_RANGE_604)
            return getChild(1) != null;
        else
            return false;
    }

    public ASTElseWhereConstructNode getElseWhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_605)
            return (ASTElseWhereConstructNode)getChild(0);
        else if (getProduction() == Production.WHERE_RANGE_606)
            return (ASTElseWhereConstructNode)getChild(1);
        else
            return null;
    }

    public boolean hasElseWhereConstruct()
    {
        if (treeHasBeenModified()) throw new IllegalStateException("Accessor methods cannot be called on the nodes of a CST after it has been modified");

        if (getProduction() == Production.WHERE_RANGE_605)
            return getChild(0) != null;
        else if (getProduction() == Production.WHERE_RANGE_606)
            return getChild(1) != null;
        else
            return false;
    }
}
